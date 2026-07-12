package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import com.am24.weatherforecastapp.presentation.WeatherConditionLocalizer
import org.json.JSONArray
import org.json.JSONObject
import java.time.Clock
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MapWeatherForecastToPresentationUseCase(
    private val conditionLocalizer: WeatherConditionLocalizer,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    operator fun invoke(
        forecast: WeatherForecast,
        city: String?
    ): WeatherPresentationResult {
        val cityName = city ?: forecast.cityName ?: "Your city"
        val hourlyJson = createHourlyJson(forecast.hourly)
        val daily = forecast.daily.map { day ->
            WeatherModel(
                city = cityName,
                time = day.day,
                condition = conditionLocalizer.localize(day.condition, day.summary),
                currentTemperature = "",
                minimumTemperature = day.temperatureMin.toInt().toString(),
                maximumTemperature = day.temperatureMax.toInt().toString(),
                imageURL = day.iconCode.toString(),
                hours = hourlyJson
            )
        }

        val current = daily.firstOrNull()?.let { firstDay ->
            WeatherModel(
                city = cityName,
                time = LocalTime.now(clock).format(CURRENT_TIME_FORMATTER),
                condition = conditionLocalizer.localize(
                    forecast.current.condition,
                    forecast.current.summary
                ),
                currentTemperature = forecast.current.temperature.toInt().toString() + "\u00B0C",
                minimumTemperature = firstDay.minimumTemperature,
                maximumTemperature = firstDay.maximumTemperature,
                imageURL = forecast.current.iconCode.toString(),
                hours = firstDay.hours
            )
        }

        return WeatherPresentationResult(
            current = current,
            daily = daily
        )
    }

    private fun createHourlyJson(hourlyData: List<HourlyWeather>): String {
        val array = JSONArray()
        hourlyData.forEach { data ->
            val obj = JSONObject()
            obj.put("date", data.date)
            obj.put("summary", conditionLocalizer.localize(data.condition, data.summary))
            obj.put("temperature", data.temperature)
            obj.put("icon", data.iconCode)
            array.put(obj)
        }
        return array.toString()
    }

    private companion object {
        val CURRENT_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
