package com.am24.imbrel.domain.model

data class DailyWeather(
    val day: String,
    val summary: String,
    val iconCode: Int,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val condition: WeatherCondition = WeatherCondition.Unknown,
)
