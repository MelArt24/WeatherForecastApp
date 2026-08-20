package com.am24.imbrel.domain.model

data class CityWeatherResult(
    val forecast: WeatherForecast,
    val city: String,
)
