package com.am24.weatherforecastapp.domain.model

data class CityWeatherResult(
    val forecast: WeatherForecast,
    val city: String,
)
