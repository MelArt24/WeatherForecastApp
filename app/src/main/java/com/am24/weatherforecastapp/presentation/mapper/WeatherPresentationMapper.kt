package com.am24.weatherforecastapp.presentation.mapper

import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.presentation.WeatherConditionLocalizer
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import java.time.Clock
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WeatherPresentationMapper(
    private val conditionLocalizer: WeatherConditionLocalizer,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    operator fun invoke(
        forecast: WeatherForecast,
        city: String?,
    ): WeatherPresentationResult {
        val cityName = city ?: forecast.cityName ?: "Your city"
        val hourlyWeather =
            forecast.hourly.map { hour ->
                WeatherModel(
                    city = cityName,
                    time = hour.date.substringAfterLast('T').take(5),
                    condition = conditionLocalizer.localize(hour.condition, hour.summary),
                    currentTemperature = hour.temperature.toInt().toString() + "\u00B0C",
                    minimumTemperature = "",
                    maximumTemperature = "",
                    imageURL = hour.iconCode.toString(),
                    hourlyWeather = emptyList(),
                )
            }
        val daily =
            forecast.daily.map { day ->
                WeatherModel(
                    city = cityName,
                    time = day.day,
                    condition = conditionLocalizer.localize(day.condition, day.summary),
                    currentTemperature = "",
                    minimumTemperature = day.temperatureMin.toInt().toString(),
                    maximumTemperature = day.temperatureMax.toInt().toString(),
                    imageURL = day.iconCode.toString(),
                    hourlyWeather = hourlyWeather,
                )
            }
        val current =
            daily.firstOrNull()?.let { firstDay ->
                WeatherModel(
                    city = cityName,
                    time = LocalTime.now(clock).format(CURRENT_TIME_FORMATTER),
                    condition =
                        conditionLocalizer.localize(
                            forecast.current.condition,
                            forecast.current.summary,
                        ),
                    currentTemperature =
                        forecast.current.temperature
                            .toInt()
                            .toString() + "\u00B0C",
                    minimumTemperature = firstDay.minimumTemperature,
                    maximumTemperature = firstDay.maximumTemperature,
                    imageURL = forecast.current.iconCode.toString(),
                    hourlyWeather = firstDay.hourlyWeather,
                )
            }
        return WeatherPresentationResult(current = current, daily = daily)
    }

    private companion object {
        val CURRENT_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

data class WeatherPresentationResult(
    val current: WeatherModel?,
    val daily: List<WeatherModel>,
)
