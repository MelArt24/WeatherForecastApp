package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.presentation.model.WeatherModel

data class WeatherPresentationResult(
    val current: WeatherModel?,
    val daily: List<WeatherModel>
)
