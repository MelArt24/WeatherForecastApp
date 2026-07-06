package com.am24.weatherforecastapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    @SerialName("lat") val lat: String,
    @SerialName("lon") val lon: String,
    @SerialName("timezone") val timezone: String,
    @SerialName("units") val units: String,
    @SerialName("current") val current: CurrentWeatherDto,
    @SerialName("hourly") val hourly: HourlyForecastDto,
    @SerialName("daily") val daily: DailyForecastDto,
    @SerialName("place_id") val placeId: String? = null
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("icon") val icon: String,
    @SerialName("icon_num") val iconNum: Int,
    @SerialName("summary") val summary: String,
    @SerialName("temperature") val temperature: Double
)

@Serializable
data class HourlyForecastDto(
    @SerialName("data") val data: List<HourlyDataDto>
)

@Serializable
data class HourlyDataDto(
    @SerialName("date") val date: String,
    @SerialName("weather") val weather: String,
    @SerialName("icon") val icon: Int,
    @SerialName("summary") val summary: String,
    @SerialName("temperature") val temperature: Double
)

@Serializable
data class DailyForecastDto(
    @SerialName("data") val data: List<DailyDataDto>
)

@Serializable
data class DailyDataDto(
    @SerialName("day") val day: String,
    @SerialName("weather") val weather: String,
    @SerialName("icon") val icon: Int,
    @SerialName("summary") val summary: String,
    @SerialName("all_day") val allDay: AllDayDataDto
)

@Serializable
data class AllDayDataDto(
    @SerialName("weather") val weather: String,
    @SerialName("icon") val icon: Int,
    @SerialName("temperature") val temperature: Double? = null,
    @SerialName("temperature_min") val temperatureMin: Double,
    @SerialName("temperature_max") val temperatureMax: Double
)
