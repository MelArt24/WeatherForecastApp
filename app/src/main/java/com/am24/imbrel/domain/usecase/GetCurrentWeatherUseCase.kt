package com.am24.imbrel.domain.usecase

import com.am24.imbrel.domain.model.WeatherForecast
import com.am24.imbrel.domain.repository.WeatherRepository

class GetCurrentWeatherUseCase(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(
        lat: String?,
        lon: String?,
    ): WeatherForecast =
        weatherRepository.getWeatherData(
            lat = lat,
            lon = lon,
            city = null,
        )
}
