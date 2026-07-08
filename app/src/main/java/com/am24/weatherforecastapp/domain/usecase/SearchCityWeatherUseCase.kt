package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.repository.WeatherRepository

class SearchCityWeatherUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(city: String): WeatherForecast {
        return weatherRepository.getWeatherData(
            city = city
        )
    }
}