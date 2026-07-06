package com.am24.weatherforecastapp.domain.model

data class CurrentWeather(
    val summary: String,
    val temperature: Double,
    val iconCode: Int
)