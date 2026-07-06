package com.am24.weatherforecastapp.data.mapper

import com.am24.weatherforecastapp.data.remote.CurrentWeatherDto
import com.am24.weatherforecastapp.data.remote.DailyDataDto
import com.am24.weatherforecastapp.data.remote.DailyForecastDto
import com.am24.weatherforecastapp.data.remote.HourlyDataDto
import com.am24.weatherforecastapp.data.remote.HourlyForecastDto
import com.am24.weatherforecastapp.data.remote.WeatherResponseDto
import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast

fun WeatherResponseDto.toDomain(): WeatherForecast {
    return WeatherForecast(
        cityName = this.placeId,
        current = current.toDomain(),
        daily = daily.toDomain(),
        hourly = hourly.toDomain()
    )
}

fun CurrentWeatherDto.toDomain(): CurrentWeather {
    return CurrentWeather(
        summary = this.summary,
        temperature = this.temperature,
        iconCode = this.iconNum
    )
}

fun DailyForecastDto.toDomain(): List<DailyWeather> {
    return data.map { it.toDomain() }
}

fun DailyDataDto.toDomain(): DailyWeather {
    return DailyWeather(
        day = this.day,
        summary = this.summary,
        iconCode = this.icon,
        temperatureMin = this.allDay.temperatureMin,
        temperatureMax = this.allDay.temperatureMax
    )
}

fun HourlyForecastDto.toDomain(): List<HourlyWeather> {
    return data.map { it.toDomain() }
}

fun HourlyDataDto.toDomain(): HourlyWeather {
    return HourlyWeather(
        date = this.date,
        summary = this.summary,
        temperature = this.temperature,
        iconCode = this.icon
    )
}