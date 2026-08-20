package com.am24.imbrel.domain.repository

import com.am24.imbrel.domain.model.WeatherForecast

interface WeatherRepository {
    suspend fun getWeatherData(
        lat: String? = null,
        lon: String? = null,
        city: String? = null,
    ): WeatherForecast
}
