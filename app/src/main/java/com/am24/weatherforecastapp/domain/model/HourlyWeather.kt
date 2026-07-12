package com.am24.weatherforecastapp.domain.model

data class HourlyWeather(
    val date: String,
    val summary: String,
    val temperature: Double,
    val iconCode: Int,
    val condition: WeatherCondition = WeatherCondition.Unknown
)
