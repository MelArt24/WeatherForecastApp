package com.am24.weatherforecastapp.data.repository

import com.am24.weatherforecastapp.data.mapper.toDomain
import com.am24.weatherforecastapp.data.error.toWeatherDomainError
import com.am24.weatherforecastapp.data.cache.TimeProvider
import com.am24.weatherforecastapp.data.cache.WeatherCacheKeyFactory
import com.am24.weatherforecastapp.data.cache.WeatherCachePolicy
import com.am24.weatherforecastapp.data.local.WeatherLocalDataSource
import com.am24.weatherforecastapp.data.network.NetworkMonitor
import com.am24.weatherforecastapp.data.network.isOnlineOrDomainFailure
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.ApiErrorReason
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService,
    private val localDataSource: WeatherLocalDataSource,
    private val timeProvider: TimeProvider,
    private val cachePolicy: WeatherCachePolicy,
    private val networkMonitor: NetworkMonitor = NetworkMonitor { true },
    private val apiKey: String,
    private val timezone: String = ZoneId.systemDefault().id
) : WeatherRepository {
    override suspend fun getWeatherData(
        lat: String?,
        lon: String?,
        city: String?
    ): WeatherForecast {
        val cacheKey = try {
            WeatherCacheKeyFactory.create(lat, lon, city)
        } catch (_: Exception) {
            throw DomainFailureException(DomainError.Api(ApiErrorReason.RequestFailed))
        }

        return mutexFor(cacheKey).withLock {
            val cached = readCache(cacheKey)

            if (!networkMonitor.isOnlineOrDomainFailure()) {
                return@withLock cached?.forecast ?: throw DomainFailureException(
                    DomainError.Network(NetworkErrorReason.Offline)
                )
            }

            val now = timeProvider.currentTimeMillis()

            if (cached != null && cachePolicy.isFresh(cached.cachedAtMillis, now)) {
                return@withLock cached.forecast
            }

            try {
                val forecast = apiService.getWeatherData(
                    lat = lat,
                    lon = lon,
                    placeId = city?.takeIf { lat == null && lon == null },
                    timezone = timezone,
                    apiKey = apiKey
                ).toDomain()

                cacheKeysFor(lat, lon, city).forEach { key -> writeCache(key, forecast, now) }

                forecast
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (remoteFailure: Exception) {
                cached?.forecast ?: throw DomainFailureException(remoteFailure.toWeatherDomainError())
            }
        }
    }

    private suspend fun readCache(cacheKey: String) = try {
        localDataSource.getWeather(cacheKey)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }

    private suspend fun writeCache(cacheKey: String, forecast: WeatherForecast, now: Long) {
        try {
            localDataSource.saveWeather(cacheKey, forecast, now)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // The remote response remains usable even if persistence is unavailable.
        }
    }

    private fun mutexFor(cacheKey: String): Mutex =
        requestMutexes.computeIfAbsent(cacheKey) { Mutex() }

    private fun cacheKeysFor(lat: String?, lon: String?, city: String?): Set<String> = buildSet {
        add(WeatherCacheKeyFactory.create(lat, lon, city))
        if (city != null && lat != null && lon != null) {
            add(WeatherCacheKeyFactory.create(lat = null, lon = null, city = city))
        }
    }

    private companion object {
        val requestMutexes = ConcurrentHashMap<String, Mutex>()
    }
}
