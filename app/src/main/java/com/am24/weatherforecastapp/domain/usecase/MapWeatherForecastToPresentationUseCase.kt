package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import org.json.JSONArray
import org.json.JSONObject

class MapWeatherForecastToPresentationUseCase {
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
                condition = day.summary,
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
                time = "Now",
                condition = forecast.current.summary,
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
            obj.put("summary", data.summary)
            obj.put("temperature", data.temperature)
            obj.put("icon", data.iconCode)
            array.put(obj)
        }
        return array.toString()
    }
}
