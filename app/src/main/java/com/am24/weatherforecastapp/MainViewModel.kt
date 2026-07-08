package com.am24.weatherforecastapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.am24.weatherforecastapp.domain.usecase.GetCurrentWeatherUseCase
import com.am24.weatherforecastapp.domain.usecase.MapWeatherForecastToPresentationUseCase
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _dataCurrent = MutableStateFlow<WeatherModel?>(null)
    val dataCurrent: StateFlow<WeatherModel?> = _dataCurrent.asStateFlow()

    private val _dataList = MutableStateFlow<List<WeatherModel>>(emptyList())
    val dataList: StateFlow<List<WeatherModel>> = _dataList.asStateFlow()

    private val _errorFlow = MutableSharedFlow<Int?>()
    val errorFlow: SharedFlow<Int?> = _errorFlow.asSharedFlow()

    fun setSelectedDay(item: WeatherModel) {
        _dataCurrent.value = item
    }

    fun requestCurrentLocationWeather(
        lat: String? = null,
        lon: String? = null
    ) {
        viewModelScope.launch {
            try {
                val response = getCurrentWeatherUseCase(lat, lon)
                val weather = mapWeatherForecastToPresentationUseCase(response, null)
                _dataList.value = weather.daily
                weather.current?.let { _dataCurrent.value = it }
                _errorFlow.emit(null)
            } catch (e: Exception) {
                _errorFlow.emit(R.string.location_error)
            }
        }
    }

    fun requestCityWeather(city: String) {
        viewModelScope.launch {
            try {
                val result = searchCityWeatherUseCase(city)
                val weather = mapWeatherForecastToPresentationUseCase(result.forecast, result.city)
                _dataList.value = weather.daily
                weather.current?.let { _dataCurrent.value = it }
                _errorFlow.emit(null)
            } catch (e: HttpException) {
                _errorFlow.emit(R.string.city_not_found)
            } catch (e: Exception) {
                _errorFlow.emit(R.string.location_error)
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
                        viewModelScope.launch { _errorFlow.emit(R.string.location_error) }
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch { _errorFlow.emit(R.string.location_error) }
                }
        } catch (e: SecurityException) {
            viewModelScope.launch { _errorFlow.emit(R.string.location_permission_denied) }
        }
    }
}
