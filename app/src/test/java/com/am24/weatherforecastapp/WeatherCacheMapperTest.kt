package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.data.local.CachedDailyWeatherEntity
import com.am24.weatherforecastapp.data.local.CachedHourlyWeatherEntity
import com.am24.weatherforecastapp.data.local.toCachedAggregate
import com.am24.weatherforecastapp.data.local.toCachedWeather
import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherCondition
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherCacheMapperTest {
    @Test
    fun domainToCacheToDomain_preservesForecastAndTimestamp() {
        val forecast = forecast(cityName = "Kyiv")

        val cached = forecast.toCachedAggregate("city:kyiv", 123_456L).toCachedWeather()

        assertEquals(forecast, cached.forecast)
        assertEquals(123_456L, cached.cachedAtMillis)
    }

    @Test
    fun cacheToDomain_restoresHourlyAndDailyPositionOrder() {
        val aggregate = forecast("Kyiv").toCachedAggregate("city:kyiv", 1L)
        val shuffled = aggregate.copy(
            hourly = aggregate.hourly.reversed(),
            daily = aggregate.daily.reversed()
        )

        val restored = shuffled.toCachedWeather().forecast

        assertEquals(listOf("hour-0", "hour-1"), restored.hourly.map { it.date })
        assertEquals(listOf("day-0", "day-1"), restored.daily.map { it.day })
    }

    @Test
    fun nullableCityNameAndEmptyCollections_arePreserved() {
        val forecast = forecast(cityName = null).copy(hourly = emptyList(), daily = emptyList())

        val cached = forecast.toCachedAggregate("lat:1.0000|lon:2.0000", 99L)
            .toCachedWeather()

        assertNull(cached.forecast.cityName)
        assertEquals(emptyList<HourlyWeather>(), cached.forecast.hourly)
        assertEquals(emptyList<DailyWeather>(), cached.forecast.daily)
        assertEquals(forecast.current, cached.forecast.current)
    }

    @Test
    fun conversion_preservesImportantWeatherFields() {
        val forecast = forecast("Lviv")
        val aggregate = forecast.toCachedAggregate("city:lviv", 7L)

        assertEquals("Current summary", aggregate.weather.currentSummary)
        assertEquals(21.75, aggregate.weather.currentTemperature, 0.0)
        assertEquals(2, aggregate.weather.currentIconCode)
        assertEquals(
            CachedHourlyWeatherEntity(
                "city:lviv", 0, "hour-0", "Hourly clear", 20.25, 2
            ),
            aggregate.hourly.first()
        )
        assertEquals(
            CachedDailyWeatherEntity(
                "city:lviv", 0, "day-0", "Daily rain", 11, 10.5, 18.75
            ),
            aggregate.daily.first()
        )
    }

    private fun forecast(cityName: String?) = WeatherForecast(
        cityName = cityName,
        current = CurrentWeather(
            summary = "Current summary",
            temperature = 21.75,
            iconCode = 2,
            condition = WeatherCondition.Clear
        ),
        hourly = listOf(
            HourlyWeather("hour-0", "Hourly clear", 20.25, 2, WeatherCondition.Clear),
            HourlyWeather("hour-1", "Hourly rain", 17.5, 11, WeatherCondition.Rain)
        ),
        daily = listOf(
            DailyWeather("day-0", "Daily rain", 11, 10.5, 18.75, WeatherCondition.Rain),
            DailyWeather("day-1", "Daily snow", 16, -2.5, 1.25, WeatherCondition.Snow)
        )
    )
}
