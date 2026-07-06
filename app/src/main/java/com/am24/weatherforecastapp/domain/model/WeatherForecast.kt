package com.am24.weatherforecastapp.domain.model

data class WeatherForecast(
    val cityName: String?,
    val current: CurrentWeather,
    val daily: List<DailyWeather>,
    val hourly: List<HourlyWeather>
)