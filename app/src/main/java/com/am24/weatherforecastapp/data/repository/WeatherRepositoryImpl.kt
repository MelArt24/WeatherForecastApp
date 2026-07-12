package com.am24.weatherforecastapp.data.repository

import com.am24.weatherforecastapp.data.mapper.toDomain
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import java.time.ZoneId

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService,
    private val apiKey: String,
    private val timezone: String = ZoneId.systemDefault().id
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
            timezone = timezone,
            apiKey = apiKey
        )
        return response.toDomain()
    }
}
