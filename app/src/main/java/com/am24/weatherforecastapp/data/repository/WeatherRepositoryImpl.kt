package com.am24.weatherforecastapp.data.repository

import com.am24.weatherforecastapp.data.mapper.toDomain
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService,
    private val apiKey: String
) : WeatherRepository {
    override suspend fun getWeatherData(
        lat: String?,
        lon: String?,
        city: String?
    ): WeatherForecast {
        val response = apiService.getWeatherData(
            lat = lat,
            lon = lon,
            placeId = city,
            apiKey = apiKey
        )
        return response.toDomain()
    }
}