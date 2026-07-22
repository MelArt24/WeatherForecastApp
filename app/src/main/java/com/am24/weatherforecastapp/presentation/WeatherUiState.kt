package com.am24.weatherforecastapp.presentation

import com.am24.weatherforecastapp.R
import com.am24.weatherforecastapp.domain.error.ApiErrorReason
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.LocationErrorReason
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
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

sealed interface WeatherUiError {
    fun messageResource(): Int = when {
        cause == DomainError.Network(NetworkErrorReason.Offline) -> R.string.offline_error
        this is WeatherUiError.CitySearch &&
                (cause as? DomainError.Api)?.reason == ApiErrorReason.NotFound ->
            R.string.city_not_found
        this is WeatherUiError.Location &&
                cause == DomainError.Location(LocationErrorReason.PermissionDenied) ->
            R.string.location_permission_denied
        this is WeatherUiError.Location -> R.string.location_error
        this is WeatherUiError.Weather -> R.string.weather_error
        else -> R.string.location_error
    }

    val cause: DomainError

    data class CitySearch(override val cause: DomainError) : WeatherUiError
    data class Location(override val cause: DomainError) : WeatherUiError
    data class Weather(override val cause: DomainError) : WeatherUiError
}

sealed interface WeatherUiEvent {
    data class ShowError(val error: WeatherUiError) : WeatherUiEvent
}