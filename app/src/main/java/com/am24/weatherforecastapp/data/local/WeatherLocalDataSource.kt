package com.am24.weatherforecastapp.data.local

import com.am24.weatherforecastapp.domain.model.WeatherForecast

data class CachedWeather(
    val forecast: WeatherForecast,
    val cachedAtMillis: Long
)

interface WeatherLocalDataSource {
    suspend fun getWeather(cacheKey: String): CachedWeather?
    suspend fun saveWeather(cacheKey: String, forecast: WeatherForecast, cachedAtMillis: Long)
}

class RoomWeatherLocalDataSource(
    private val weatherDao: WeatherDao
) : WeatherLocalDataSource {

    override suspend fun getWeather(cacheKey: String): CachedWeather? =
        weatherDao.getWeather(cacheKey)?.toCachedWeather()

    override suspend fun saveWeather(
        cacheKey: String,
        forecast: WeatherForecast,
        cachedAtMillis: Long
    ) {
        weatherDao.replaceWeather(forecast.toCachedAggregate(cacheKey, cachedAtMillis))
    }
}
