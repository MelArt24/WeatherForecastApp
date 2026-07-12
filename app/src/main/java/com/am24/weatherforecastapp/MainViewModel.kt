package com.am24.weatherforecastapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.am24.weatherforecastapp.domain.usecase.GetCurrentWeatherUseCase
import com.am24.weatherforecastapp.domain.usecase.MapWeatherForecastToPresentationUseCase
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import com.am24.weatherforecastapp.presentation.WeatherUiError
import com.am24.weatherforecastapp.presentation.WeatherUiEvent
import com.am24.weatherforecastapp.presentation.WeatherUiState
import com.am24.weatherforecastapp.presentation.WeatherUiStatus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ViewModel — це "спільна пам'ять" для всіх фрагментів додатка.
 * Вона виживає при повороті екрана та зміні конфігурації.
 */
class MainViewModel(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val searchCityWeatherUseCase: SearchCityWeatherUseCase,
    private val mapWeatherForecastToPresentationUseCase: MapWeatherForecastToPresentationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _events = Channel<WeatherUiEvent>(Channel.BUFFERED)
    val events: Flow<WeatherUiEvent> = _events.receiveAsFlow()

    fun setSelectedDay(item: WeatherModel) {
        _uiState.update { it.copy(selectedDay = item) }
    }

    fun requestCurrentLocationWeather(
        lat: String? = null,
        lon: String? = null
    ) {
        viewModelScope.launch {
            startLoading()
            try {
                val response = getCurrentWeatherUseCase(lat, lon)
                val weather = mapWeatherForecastToPresentationUseCase(response, null)
                showWeather(weather.current, weather.daily)
            } catch (e: Exception) {
                showError(WeatherUiError.Location, R.string.location_error)
            }
        }
    }

    fun requestCityWeather(city: String) {
        viewModelScope.launch {
            startLoading()
            try {
                val result = searchCityWeatherUseCase(city)
                val weather = mapWeatherForecastToPresentationUseCase(result.forecast, result.city)
                showWeather(weather.current, weather.daily)
            } catch (e: HttpException) {
                showError(WeatherUiError.CityNotFound, R.string.city_not_found)
            } catch (e: Exception) {
                showError(WeatherUiError.Location, R.string.location_error)
            }
        }
    }

    fun getLocation(fLocalProviderClient: FusedLocationProviderClient) {
        val cancellationToken = CancellationTokenSource()
        try {
            fLocalProviderClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
                .addOnCompleteListener { task ->
                    val location = task.result
                    if (location != null) {
                        requestCurrentLocationWeather(
                            lat = location.latitude.toString(),
                            lon = location.longitude.toString()
                        )
                    } else {
                        emitError(WeatherUiError.Location, R.string.location_error)
                    }
                }
                .addOnFailureListener {
                    emitError(WeatherUiError.Location, R.string.location_error)
                }
        } catch (e: SecurityException) {
            emitError(WeatherUiError.LocationPermissionDenied, R.string.location_permission_denied)
        }
    }

    private fun startLoading() {
        _uiState.update { it.copy(status = WeatherUiStatus.Loading, error = null) }
    }

    private fun showWeather(current: WeatherModel?, daily: List<WeatherModel>) {
        val status = if (current == null && daily.isEmpty()) {
            WeatherUiStatus.Empty
        } else {
            WeatherUiStatus.Success
        }
        _uiState.update {
            it.copy(
                status = status,
                currentWeather = current,
                dailyWeather = daily,
                selectedDay = null,
                error = null
            )
        }
    }

    private suspend fun showError(error: WeatherUiError, messageResId: Int) {
        _uiState.update { it.copy(status = WeatherUiStatus.Error, error = error) }
        _events.send(WeatherUiEvent.ShowError(messageResId))
    }

    private fun emitError(error: WeatherUiError, messageResId: Int) {
        viewModelScope.launch { showError(error, messageResId) }
    }
}
