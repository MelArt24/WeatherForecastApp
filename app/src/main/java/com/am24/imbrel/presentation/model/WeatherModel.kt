package com.am24.imbrel.presentation.model

data class WeatherModel(
    val city: String,
    val time: String,
    val condition: String,
    val currentTemperature: String,
    val minimumTemperature: String,
    val maximumTemperature: String,
    val imageURL: String,
    val hourlyWeather: List<WeatherModel>,
)
