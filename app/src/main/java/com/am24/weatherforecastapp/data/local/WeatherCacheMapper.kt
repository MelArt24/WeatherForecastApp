package com.am24.weatherforecastapp.data.local

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.model.weatherConditionFromIcon

fun WeatherForecast.toCachedAggregate(cacheKey: String, cachedAtMillis: Long) =
    CachedWeatherAggregate(
        weather = CachedWeatherEntity(
            cacheKey = cacheKey,
            cityName = cityName,
            currentSummary = current.summary,
            currentTemperature = current.temperature,
            currentIconCode = current.iconCode,
            cachedAtMillis = cachedAtMillis
        ),
        hourly = hourly.mapIndexed { index, item ->
            CachedHourlyWeatherEntity(
                cacheKey, index, item.date, item.summary, item.temperature, item.iconCode
            )
        },
        daily = daily.mapIndexed { index, item ->
            CachedDailyWeatherEntity(
                cacheKey, index, item.day, item.summary, item.iconCode,
                item.temperatureMin, item.temperatureMax
            )
        }
    )

fun CachedWeatherAggregate.toCachedWeather(): CachedWeather = CachedWeather(
    forecast = WeatherForecast(
        cityName = weather.cityName,
        current = CurrentWeather(
            weather.currentSummary,
            weather.currentTemperature,
            weather.currentIconCode,
            weatherConditionFromIcon(weather.currentIconCode)
        ),
        hourly = hourly.sortedBy { it.position }.map {
            HourlyWeather(
                it.date, it.summary, it.temperature, it.iconCode,
                weatherConditionFromIcon(it.iconCode)
            )
        },
        daily = daily.sortedBy { it.position }.map {
            DailyWeather(
                it.day, it.summary, it.iconCode, it.temperatureMin, it.temperatureMax,
                weatherConditionFromIcon(it.iconCode)
            )
        }
    ),
    cachedAtMillis = weather.cachedAtMillis
)
