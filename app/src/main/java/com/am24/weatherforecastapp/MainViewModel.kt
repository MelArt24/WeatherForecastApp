package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import kotlinx.coroutines.launch
import com.am24.weatherforecastapp.domain.usecase.GetCurrentWeatherUseCase
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import com.am24.weatherforecastapp.utils.TransliterationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import retrofit2.HttpException

/**
 * ViewModel — це "спільна пам'ять" для всіх фрагментів додатка.
 * Вона виживає при повороті екрана та зміні конфігурації.
 */
class MainViewModel(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val searchCityWeatherUseCase: SearchCityWeatherUseCase
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
                parseWeatherData(response, null)
                _errorFlow.emit(null)
            } catch (e: Exception) {
                _errorFlow.emit(R.string.location_error)
            }
        }
    }

    fun requestCityWeather(
        city: String,
        isTransliterated: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val response = searchCityWeatherUseCase(city)
                parseWeatherData(response, city)
                _errorFlow.emit(null)
            } catch (e: HttpException) {
                if (!isTransliterated && e.code() == 400) {
                    val transliteratedCity = TransliterationUtils.transliterate(city)

                    requestCityWeather(
                        city = transliteratedCity,
                        isTransliterated = true
                    )
                } else {
                    _errorFlow.emit(R.string.city_not_found)
                }
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
                        requestCurrentLocationWeather(lat = location.latitude.toString(), lon = location.longitude.toString())
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

    private fun parseWeatherData(response: WeatherForecast, city: String?) {
        val cityName = city ?: response.cityName ?: "Your city"
        val list = ArrayList<WeatherModel>()

        response.daily.forEach { day ->
            val item = WeatherModel(
                city = cityName,
                time = day.day,
                condition = day.summary,
                currentTemperature = "",
                minimumTemperature = day.temperatureMin.toInt().toString(),
                maximumTemperature = day.temperatureMax.toInt().toString(),
                imageURL = day.iconCode.toString(),
                hours = createHourlyJson(response.hourly)
            )
            list.add(item)
        }
        _dataList.value = list

        if (list.isNotEmpty()) {
            val currentItem = WeatherModel(
                city = cityName,
                time = "Now",
                condition = response.current.summary,
                currentTemperature = response.current.temperature.toInt().toString() + "°C",
                minimumTemperature = list[0].minimumTemperature,
                maximumTemperature = list[0].maximumTemperature,
                imageURL = response.current.iconCode.toString(),
                hours = list[0].hours
            )
            _dataCurrent.value = currentItem
        }
    }

    private fun createHourlyJson(hourlyData: List<HourlyWeather>): String {
        val array = JSONArray()
        hourlyData.forEach { data ->
            val obj = org.json.JSONObject()
            obj.put("date", data.date)
            obj.put("summary", data.summary)
            obj.put("temperature", data.temperature)
            obj.put("icon", data.iconCode)
            array.put(obj)
        }
        return array.toString()
    }
}