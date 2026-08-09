package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherCondition
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.presentation.mapper.WeatherPresentationMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MapWeatherForecastToPresentationUseCaseTest {
    private val useCase =
        WeatherPresentationMapper(
            conditionLocalizer = { condition, fallback ->
                if (condition == WeatherCondition.Unknown) fallback else "localized:$condition"
            },
            clock =
                Clock.fixed(
                    Instant.parse("2026-07-05T12:34:00Z"),
                    ZoneId.of("Europe/Kyiv"),
                ),
        )

    @Test
    fun invoke_mapsForecastToCurrentAndDailyWeatherModels() {
        val result = useCase(forecast(), city = "Kyiv")

        assertEquals("Kyiv", result.current?.city)
        assertEquals("15:34", result.current?.time)
        assertEquals("Clear", result.current?.condition)
        assertEquals("21\u00B0C", result.current?.currentTemperature)
        assertEquals("18", result.current?.minimumTemperature)
        assertEquals("24", result.current?.maximumTemperature)
        assertEquals("1", result.current?.imageURL)
        assertEquals(1, result.daily.size)
        assertEquals("2026-07-05", result.daily.first().time)
        assertEquals("", result.daily.first().currentTemperature)
    }

    @Test
    fun invoke_usesForecastCityNameWhenRequestedCityIsMissing() {
        val result = useCase(forecast(cityName = "Lviv"), city = null)

        assertEquals("Lviv", result.current?.city)
        assertEquals("Lviv", result.daily.first().city)
    }

    @Test
    fun invoke_usesDefaultCityNameWhenCitySourcesAreMissing() {
        val result = useCase(forecast(cityName = null), city = null)

        assertEquals("Your city", result.current?.city)
        assertEquals("Your city", result.daily.first().city)
    }

    @Test
    fun invoke_mapsHourlyWeather() {
        val result = useCase(forecast(), city = "Kyiv")
        val firstHour = result.current?.hourlyWeather?.first()

        assertEquals("12:00", firstHour?.time)
        assertEquals("Clear", firstHour?.condition)
        assertEquals("21°C", firstHour?.currentTemperature)
        assertEquals("1", firstHour?.imageURL)
    }

    @Test
    fun invoke_localizesStableConditionsAndFallsBackForUnknown() {
        val forecast =
            forecast().copy(
                current = forecast().current.copy(condition = WeatherCondition.Clear),
                hourly =
                    listOf(
                        forecast().hourly.first().copy(condition = WeatherCondition.Rain),
                        forecast().hourly.first().copy(summary = "Provider fallback"),
                    ),
            )

        val result = useCase(forecast, city = "Kyiv")
        val hours = result.current?.hourlyWeather.orEmpty()

        assertEquals("localized:Clear", result.current?.condition)
        assertEquals("localized:Rain", hours[0].condition)
        assertEquals("Provider fallback", hours[1].condition)
    }

    @Test
    fun invoke_doesNotCreateCurrentWeatherWhenDailyForecastIsEmpty() {
        val result = useCase(forecast(daily = emptyList()), city = "Kyiv")

        assertNull(result.current)
        assertEquals(emptyList<Any>(), result.daily)
    }

    private fun forecast(
        cityName: String? = "Kyiv",
        daily: List<DailyWeather> =
            listOf(
                DailyWeather(
                    day = "2026-07-05",
                    summary = "Clear",
                    iconCode = 1,
                    temperatureMin = 18.0,
                    temperatureMax = 24.0,
                ),
            ),
    ) = WeatherForecast(
        cityName = cityName,
        current =
            CurrentWeather(
                summary = "Clear",
                temperature = 21.4,
                iconCode = 1,
            ),
        hourly =
            listOf(
                HourlyWeather(
                    date = "2026-07-05T12:00:00",
                    summary = "Clear",
                    temperature = 21.4,
                    iconCode = 1,
                ),
            ),
        daily = daily,
    )
}
