package com.am24.weatherforecastapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.am24.weatherforecastapp.adapters.WeatherModel
import com.am24.weatherforecastapp.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import com.am24.weatherforecastapp.BuildConfig.WEATHER_API_KEY
import com.am24.weatherforecastapp.data.remote.HourlyData
import com.am24.weatherforecastapp.data.remote.WeatherResponse
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
class MainViewModel : ViewModel() {

    private val _dataCurrent = MutableStateFlow<WeatherModel?>(null)
    val dataCurrent: StateFlow<WeatherModel?> = _dataCurrent.asStateFlow()

    private val _dataList = MutableStateFlow<List<WeatherModel>>(emptyList())
    val dataList: StateFlow<List<WeatherModel>> = _dataList.asStateFlow()

    private val _errorFlow = MutableSharedFlow<Int?>()
    val errorFlow: SharedFlow<Int?> = _errorFlow.asSharedFlow()

    fun setSelectedDay(item: WeatherModel) {
        _dataCurrent.value = item
    }

    fun requestWeatherData(
        lat: String? = null,
        lon: String? = null,
        city: String? = null,
        isTransliterated: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.weatherApiService.getWeatherData(
                    lat = lat,
                    lon = lon,
                    placeId = city,
                    apiKey = WEATHER_API_KEY
                )
                parseWeatherData(response, city)
                _errorFlow.emit(null)
            } catch (e: HttpException) {
                if (!isTransliterated && e.code() == 400 && city != null) {
                    val transliteratedCity = TransliterationUtils.transliterate(city)
                    requestWeatherData(city = transliteratedCity, isTransliterated = true)
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
                        requestWeatherData(lat = location.latitude.toString(), lon = location.longitude.toString())
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

    private fun parseWeatherData(response: WeatherResponse, city: String?) {
        val cityName = city ?: response.placeId ?: "Your city"
        val list = ArrayList<WeatherModel>()

        response.daily.data.forEach { day ->
            val item = WeatherModel(
                cityName,
                day.day,
                day.summary,
                "",
                day.allDay.temperatureMax.toInt().toString(),
                day.allDay.temperatureMin.toInt().toString(),
                day.icon.toString(),
                createHourlyJson(response.hourly.data)
            )
            list.add(item)
        }
        _dataList.value = list

        if (list.isNotEmpty()) {
            val currentItem = WeatherModel(
                cityName,
                "Now",
                response.current.summary,
                response.current.temperature.toInt().toString() + "°C",
                list[0].maximumTemperature,
                list[0].minimumTemperature,
                response.current.iconNum.toString(),
                list[0].hours
            )
            _dataCurrent.value = currentItem
        }
    }

    private fun createHourlyJson(hourlyData: List<HourlyData>): String {
        val array = JSONArray()
        hourlyData.forEach { data ->
            val obj = org.json.JSONObject()
            obj.put("date", data.date)
            obj.put("summary", data.summary)
            obj.put("temperature", data.temperature)
            obj.put("icon", data.icon)
            array.put(obj)
        }
        return array.toString()
    }
}