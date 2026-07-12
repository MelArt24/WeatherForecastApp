package com.am24.weatherforecastapp.presentation

import android.content.Context
import com.am24.weatherforecastapp.R
import com.am24.weatherforecastapp.domain.model.WeatherCondition

fun interface WeatherConditionLocalizer {
    fun localize(condition: WeatherCondition, fallback: String): String
}

class AndroidWeatherConditionLocalizer(
    private val context: Context
) : WeatherConditionLocalizer {
    override fun localize(condition: WeatherCondition, fallback: String): String {
        val resource = when (condition) {
            WeatherCondition.Clear -> R.string.weather_clear
            WeatherCondition.MostlyClear -> R.string.weather_mostly_clear
            WeatherCondition.PartlyCloudy -> R.string.weather_partly_cloudy
            WeatherCondition.Cloudy -> R.string.weather_cloudy
            WeatherCondition.Overcast -> R.string.weather_overcast
            WeatherCondition.Fog -> R.string.weather_fog
            WeatherCondition.Drizzle -> R.string.weather_drizzle
            WeatherCondition.Rain -> R.string.weather_rain
            WeatherCondition.HeavyRain -> R.string.weather_heavy_rain
            WeatherCondition.Snow -> R.string.weather_snow
            WeatherCondition.Sleet -> R.string.weather_sleet
            WeatherCondition.FreezingRain -> R.string.weather_freezing_rain
            WeatherCondition.Hail -> R.string.weather_hail
            WeatherCondition.Thunderstorm -> R.string.weather_thunderstorm
            WeatherCondition.Unknown -> return fallback
        }
        return context.getString(resource)
    }
}
