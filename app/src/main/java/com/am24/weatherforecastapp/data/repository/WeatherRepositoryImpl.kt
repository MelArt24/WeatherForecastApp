package com.am24.weatherforecastapp.data.repository

import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.data.remote.WeatherResponse
import com.am24.weatherforecastapp.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService,
    private val apiKey: String
) : WeatherRepository {
    override suspend fun getWeatherData(
        lat: String?,
        lon: String?,
        city: String?
    ): WeatherResponse {
        return apiService.getWeatherData(
            lat = lat,
            lon = lon,
            placeId = city,
            apiKey = apiKey
        )
    }
}