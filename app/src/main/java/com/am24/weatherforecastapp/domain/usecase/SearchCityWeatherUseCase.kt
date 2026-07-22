package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.error.ApiErrorReason
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import com.am24.weatherforecastapp.domain.CityWeatherResult
import com.am24.weatherforecastapp.data.network.NetworkMonitor
import com.am24.weatherforecastapp.data.network.isOnlineOrDomainFailure
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import com.am24.weatherforecastapp.domain.repository.WeatherRepository

class SearchCityWeatherUseCase(
    private val weatherRepository: WeatherRepository,
    private val geocodingRepository: GeocodingRepository,
    private val networkMonitor: NetworkMonitor = NetworkMonitor { true }
) {
    suspend operator fun invoke(city: String): CityWeatherResult {
        val normalizedQuery = city.trim().replace(Regex("\\s+"), " ")
        if (normalizedQuery.isEmpty()) throw DomainFailureException(
            DomainError.Api(ApiErrorReason.RequestFailed)
        )

        if (!networkMonitor.isOnlineOrDomainFailure()) {
            return CityWeatherResult(
                forecast = weatherRepository.getWeatherData(city = normalizedQuery),
                city = normalizedQuery
            )
        }

        val location = geocodingRepository.searchLocation(normalizedQuery)
            ?: throw DomainFailureException(DomainError.Api(ApiErrorReason.NotFound))
        val forecast = weatherRepository.getWeatherData(
            lat = location.latitude.toString(),
            lon = location.longitude.toString(),
            city = normalizedQuery
        )
        return CityWeatherResult(
            forecast = forecast,
            city = location.localizedName?.takeIf { it.isNotBlank() }
                ?: normalizedQuery
        )
    }
}
