package com.am24.weatherforecastapp.domain

import com.am24.weatherforecastapp.domain.model.WeatherForecast

data class CityWeatherResult(
    val forecast: WeatherForecast,
    val city: String
)