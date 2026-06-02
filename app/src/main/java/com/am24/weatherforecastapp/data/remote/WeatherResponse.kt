package com.am24.weatherforecastapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("lat") val lat: String,
    @SerialName("lon") val lon: String,
    @SerialName("timezone") val timezone: String,
    @SerialName("units") val units: String,
    @SerialName("current") val current: CurrentWeather,
    @SerialName("hourly") val hourly: HourlyForecast,
    @SerialName("daily") val daily: DailyForecast,
    @SerialName("place_id") val placeId: String? = null
)

@Serializable
data class CurrentWeather(
    @SerialName("icon") val icon: String,
    @SerialName("icon_num") val iconNum: Int,
    @SerialName("summary") val summary: String,
    @SerialName("temperature") val temperature: Double
)

@Serializable
data class HourlyForecast(
    @SerialName("data") val data: List<HourlyData>
)

@Serializable
data class HourlyData(
    @SerialName("date") val date: String,
    @SerialName("weather") val weather: String,
    @SerialName("icon") val icon: Int,
    @SerialName("summary") val summary: String,
    @SerialName("temperature") val temperature: Double
)

@Serializable
data class DailyForecast(
    @SerialName("data") val data: List<DailyData>
)

@Serializable
data class DailyData(
    @SerialName("day") val day: String,
    @SerialName("weather") val weather: String,
    @SerialName("icon") val icon: Int,
    @SerialName("summary") val summary: String,
    @SerialName("all_day") val allDay: AllDayData
)

@Serializable
data class AllDayData(
    @SerialName("weather") val weather: String,
    @SerialName("icon") val icon: Int,
    @SerialName("temperature") val temperature: Double? = null,
    @SerialName("temperature_min") val temperatureMin: Double,
    @SerialName("temperature_max") val temperatureMax: Double
)
