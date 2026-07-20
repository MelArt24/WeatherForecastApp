package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.data.cache.TimeProvider
import com.am24.weatherforecastapp.data.cache.WeatherCachePolicy
import com.am24.weatherforecastapp.data.local.CachedWeather
import com.am24.weatherforecastapp.data.local.WeatherLocalDataSource
import com.am24.weatherforecastapp.data.network.NetworkMonitor
import com.am24.weatherforecastapp.data.remote.CurrentWeatherDto
import com.am24.weatherforecastapp.data.remote.DailyForecastDto
import com.am24.weatherforecastapp.data.remote.HourlyForecastDto
import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.data.remote.WeatherResponseDto
import com.am24.weatherforecastapp.data.repository.WeatherRepositoryImpl
import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.OfflineException
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.model.weatherConditionFromIcon
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class WeatherRepositoryImplTest {
    private val now = 1_000_000L
    private val remoteForecast = forecast("remote", 20.0)
    private val cachedForecast = forecast("cached", 10.0)

    @Test
    fun remoteSuccess_returnsFreshDataAndUpdatesCorrectNormalizedKey() = runTest {
        val local = FakeLocalDataSource()
        val repository = repository(FakeApi(dto("remote", 20.0)), local)

        val result = repository.getWeatherData("50.45001", "30.52340", null)

        assertEquals(remoteForecast, result)
        assertEquals("lat:50.4500|lon:30.5234", local.savedKey)
        assertEquals(remoteForecast, local.savedForecast)
        assertEquals(now, local.savedAt)
    }

    @Test
    fun freshCache_isReturnedWithoutRemoteRequest() = runTest {
        val api = FakeApi(dto("remote", 20.0))
        val local = FakeLocalDataSource(CachedWeather(cachedForecast, now - 1_000))

        val result = repository(api, local).getWeatherData("50.45", "30.5234", null)

        assertEquals(cachedForecast, result)
        assertEquals(0, api.calls)
        assertEquals("lat:50.4500|lon:30.5234", local.readKey)
    }

    @Test
    fun offlineWithStaleCache_returnsCacheWithoutRemoteRequest() = runTest {
        val api = FakeApi(dto("remote", 20.0))
        val local = FakeLocalDataSource(CachedWeather(cachedForecast, 0))

        val result = repository(api, local, online = false).getWeatherData("1", "2", null)

        assertEquals(cachedForecast, result)
        assertEquals(0, api.calls)
    }

    @Test(expected = OfflineException::class)
    fun offlineWithoutCache_failsWithoutRemoteRequest() = runTest {
        val api = FakeApi(dto("remote", 20.0))

        repository(api, FakeLocalDataSource(), online = false).getWeatherData("1", "2", null)

        assertEquals(0, api.calls)
    }

    @Test
    fun staleCache_triggersRemoteRefresh() = runTest {
        val api = FakeApi(dto("remote", 20.0))
        val local = FakeLocalDataSource(CachedWeather(cachedForecast, now - 20_001))

        val result = repository(api, local, ttl = 20_000).getWeatherData("1", "2", null)

        assertEquals(remoteForecast, result)
        assertEquals(1, api.calls)
        assertEquals(remoteForecast, local.savedForecast)
    }

    @Test
    fun remoteFailure_returnsMatchingStaleCache() = runTest {
        val local = FakeLocalDataSource(CachedWeather(cachedForecast, 0))

        val result = repository(FakeApi(failure = IOException("offline")), local)
            .getWeatherData("1", "2", null)

        assertEquals(cachedForecast, result)
        assertEquals("lat:1.0000|lon:2.0000", local.readKey)
    }

    @Test
    fun remoteFailureWithoutCache_preservesOriginalFailure() = runTest {
        val failure = IOException("offline")
        var thrown: Throwable? = null
        try {
            repository(FakeApi(failure = failure), FakeLocalDataSource())
                .getWeatherData("1", "2", null)
        } catch (error: Throwable) {
            thrown = error
        }

        assertSame(failure, thrown)
    }

    @Test
    fun cacheWriteFailure_doesNotHideRemoteSuccess() = runTest {
        val local = FakeLocalDataSource().apply { writeFailure = IllegalStateException("db") }

        val result = repository(FakeApi(dto("remote", 20.0)), local)
            .getWeatherData("1", "2", null)

        assertEquals(remoteForecast, result)
    }

    @Test
    fun cacheReadFailure_doesNotPreventRemoteSuccess() = runTest {
        val local = FakeLocalDataSource().apply { readFailure = IllegalStateException("db") }

        val result = repository(FakeApi(dto("remote", 20.0)), local)
            .getWeatherData("1", "2", null)

        assertEquals(remoteForecast, result)
    }

    @Test
    fun cancellationFromRemote_isPropagatedWithoutFallback() = runTest {
        val cancellation = CancellationException("cancel")
        val local = FakeLocalDataSource(CachedWeather(cachedForecast, 0))

        var thrown: Throwable? = null
        try {
            repository(FakeApi(failure = cancellation), local)
                .getWeatherData("1", "2", null)
        } catch (error: Throwable) {
            thrown = error
        }

        assertSame(cancellation, thrown)
    }

    private fun repository(
        api: FakeApi,
        local: FakeLocalDataSource,
        ttl: Long = 20_000,
        online: Boolean = true
    ) = WeatherRepositoryImpl(
        apiService = api,
        localDataSource = local,
        timeProvider = TimeProvider { now },
        cachePolicy = WeatherCachePolicy(ttl),
        networkMonitor = NetworkMonitor { online },
        apiKey = "key",
        timezone = "UTC"
    )

    private class FakeLocalDataSource(var cached: CachedWeather? = null) : WeatherLocalDataSource {
        var readKey: String? = null
        var savedKey: String? = null
        var savedForecast: WeatherForecast? = null
        var savedAt: Long? = null
        var readFailure: Exception? = null
        var writeFailure: Exception? = null

        override suspend fun getWeather(cacheKey: String): CachedWeather? {
            readKey = cacheKey
            readFailure?.let { throw it }
            return cached
        }

        override suspend fun saveWeather(cacheKey: String, forecast: WeatherForecast, cachedAtMillis: Long) {
            writeFailure?.let { throw it }
            savedKey = cacheKey
            savedForecast = forecast
            savedAt = cachedAtMillis
        }
    }

    private class FakeApi(
        private val response: WeatherResponseDto? = null,
        private val failure: Exception? = null
    ) : WeatherApiService {
        var calls = 0
        override suspend fun getWeatherData(
            lat: String?, lon: String?, placeId: String?, sections: String,
            timezone: String, language: String, units: String, apiKey: String
        ): WeatherResponseDto {
            calls++
            failure?.let { throw it }
            return requireNotNull(response)
        }
    }

    private fun dto(summary: String, temperature: Double) = WeatherResponseDto(
        lat = "1", lon = "2", timezone = "UTC", units = "metric", placeId = null,
        current = CurrentWeatherDto("", 2, summary, temperature),
        hourly = HourlyForecastDto(emptyList()),
        daily = DailyForecastDto(emptyList())
    )

    private fun forecast(summary: String, temperature: Double) = WeatherForecast(
        cityName = null,
        current = CurrentWeather(summary, temperature, 2, weatherConditionFromIcon(2)),
        hourly = emptyList(),
        daily = emptyList()
    )
}
