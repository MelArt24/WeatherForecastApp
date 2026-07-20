package com.am24.weatherforecastapp.presentation

import com.am24.weatherforecastapp.presentation.model.WeatherModel

data class WeatherUiState(
    val status: WeatherUiStatus = WeatherUiStatus.Initial,
    val currentWeather: WeatherModel? = null,
    val dailyWeather: List<WeatherModel> = emptyList(),
    val selectedDay: WeatherModel? = null,
    val error: WeatherUiError? = null
) {
    val isLoading: Boolean
        get() = status == WeatherUiStatus.Loading

    val displayedWeather: WeatherModel?
        get() = selectedDay ?: currentWeather
}

enum class WeatherUiStatus {
    Initial,
    Loading,
    Success,
    Empty,
    Error
}

enum class WeatherUiError {
    Location,
    Weather,
    Offline,
    CityNotFound,
    LocationPermissionDenied
}

sealed interface WeatherUiEvent {
    data class ShowError(val messageResId: Int) : WeatherUiEvent
}
