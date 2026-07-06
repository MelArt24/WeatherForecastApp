package com.am24.weatherforecastapp.domain.repository

import com.am24.weatherforecastapp.domain.model.WeatherForecast


interface WeatherRepository {
    suspend fun getWeatherData(
        lat: String? = null,
        lon: String? = null,
        city: String? = null
    ): WeatherForecast
}