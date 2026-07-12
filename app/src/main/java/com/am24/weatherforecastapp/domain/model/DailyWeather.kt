package com.am24.weatherforecastapp.domain.model

data class DailyWeather(
    val day: String,
    val summary: String,
    val iconCode: Int,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val condition: WeatherCondition = WeatherCondition.Unknown
)
