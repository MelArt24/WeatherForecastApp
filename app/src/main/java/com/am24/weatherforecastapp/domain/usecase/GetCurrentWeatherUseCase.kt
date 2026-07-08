package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.repository.WeatherRepository

class GetCurrentWeatherUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(
        lat: String?,
        lon: String?
    ): WeatherForecast {
        return weatherRepository.getWeatherData(
            lat = lat,
            lon = lon,
            city = null
        )
    }
}