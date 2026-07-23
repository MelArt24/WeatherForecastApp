package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.data.mapper.toDomain
import com.am24.weatherforecastapp.data.error.InvalidWeatherResponseException
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.data.remote.AllDayDataDto
import com.am24.weatherforecastapp.data.remote.CurrentWeatherDto
import com.am24.weatherforecastapp.data.remote.DailyDataDto
import com.am24.weatherforecastapp.data.remote.DailyForecastDto
import com.am24.weatherforecastapp.data.remote.HourlyDataDto
import com.am24.weatherforecastapp.data.remote.HourlyForecastDto
import com.am24.weatherforecastapp.data.remote.WeatherResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WeatherMapperTest {

    @Test
    fun `CurrentWeatherDto toDomain maps dto to CurrentWeather`() {
        val dto = CurrentWeatherDto(
            summary = "Clear sky",
            temperature = 21.5,
            iconNum = 2,
            icon = ""
        )

        val result = dto.toDomain()

        assertEquals("Clear sky", result.summary)
        assertEquals(21.5, result.temperature, 0.0)
        assertEquals(2, result.iconCode)
    }

    @Test
    fun `DailyDataDto toDomain maps dto to DailyWeather`() {
        val dto = DailyDataDto(
            day = "2026-07-06",
            weather = "clear",
            icon = 1,
            summary = "Sunny day",
            allDay = AllDayDataDto(
                weather = "clear",
                icon = 1,
                temperature = 24.0,
                temperatureMin = 18.5,
                temperatureMax = 27.3
            )
        )

        val result = dto.toDomain()

        assertEquals("2026-07-06", result.day)
        assertEquals("Sunny day", result.summary)
        assertEquals(1, result.iconCode)
        assertEquals(18.5, result.temperatureMin, 0.0)
        assertEquals(27.3, result.temperatureMax, 0.0)
    }

    @Test
    fun `DailyForecastDto toDomain maps all daily items`() {
        val dto = DailyForecastDto(
            data = listOf(
                DailyDataDto(
                    day = "2026-07-06",
                    weather = "clear",
                    icon = 1,
                    summary = "Sunny",
                    allDay = AllDayDataDto(
                        weather = "clear",
                        icon = 1,
                        temperature = 24.0,
                        temperatureMin = 18.0,
                        temperatureMax = 28.0
                    )
                ),
                DailyDataDto(
                    day = "2026-07-07",
                    weather = "rain",
                    icon = 5,
                    summary = "Rainy",
                    allDay = AllDayDataDto(
                        weather = "rain",
                        icon = 5,
                        temperature = 19.0,
                        temperatureMin = 15.0,
                        temperatureMax = 21.0
                    )
                )
            )
        )

        val result = dto.toDomain()

        assertEquals(2, result.size)

        assertEquals("2026-07-06", result[0].day)
        assertEquals("Sunny", result[0].summary)
        assertEquals(1, result[0].iconCode)
        assertEquals(18.0, result[0].temperatureMin, 0.0)
        assertEquals(28.0, result[0].temperatureMax, 0.0)

        assertEquals("2026-07-07", result[1].day)
        assertEquals("Rainy", result[1].summary)
        assertEquals(5, result[1].iconCode)
        assertEquals(15.0, result[1].temperatureMin, 0.0)
        assertEquals(21.0, result[1].temperatureMax, 0.0)
    }

    @Test
    fun `HourlyDataDto toDomain maps dto to HourlyWeather`() {
        val dto = HourlyDataDto(
            date = "2026-07-06T15:00:00",
            weather = "cloudy",
            icon = 3,
            summary = "Partly cloudy",
            temperature = 22.7
        )

        val result = dto.toDomain()

        assertEquals("2026-07-06T15:00:00", result.date)
        assertEquals("Partly cloudy", result.summary)
        assertEquals(22.7, result.temperature, 0.0)
        assertEquals(3, result.iconCode)
    }

    @Test
    fun `HourlyForecastDto toDomain maps all hourly items`() {
        val dto = HourlyForecastDto(
            data = listOf(
                HourlyDataDto(
                    date = "2026-07-06T12:00:00",
                    weather = "clear",
                    icon = 1,
                    summary = "Sunny",
                    temperature = 23.0
                ),
                HourlyDataDto(
                    date = "2026-07-06T15:00:00",
                    weather = "rain",
                    icon = 5,
                    summary = "Light rain",
                    temperature = 20.0
                )
            )
        )

        val result = dto.toDomain()

        assertEquals(2, result.size)

        assertEquals("2026-07-06T12:00:00", result[0].date)
        assertEquals("Sunny", result[0].summary)
        assertEquals(23.0, result[0].temperature, 0.0)
        assertEquals(1, result[0].iconCode)

        assertEquals("2026-07-06T15:00:00", result[1].date)
        assertEquals("Light rain", result[1].summary)
        assertEquals(20.0, result[1].temperature, 0.0)
        assertEquals(5, result[1].iconCode)
    }

    @Test
    fun `WeatherResponseDto toDomain maps full response to WeatherForecast`() {
        val dto = WeatherResponseDto(
            lat = "50.45",
            lon = "30.52",
            timezone = "Europe/Kyiv",
            units = "metric",
            placeId = "Kyiv",
            current = CurrentWeatherDto(
                summary = "Clear sky",
                temperature = 21.5,
                iconNum = 2,
                icon = ""
            ),
            daily = DailyForecastDto(
                data = listOf(
                    DailyDataDto(
                        day = "2026-07-06",
                        weather = "clear",
                        icon = 1,
                        summary = "Sunny day",
                        allDay = AllDayDataDto(
                            weather = "clear",
                            icon = 1,
                            temperature = 24.0,
                            temperatureMin = 18.5,
                            temperatureMax = 27.3
                        )
                    )
                )
            ),
            hourly = HourlyForecastDto(
                data = listOf(
                    HourlyDataDto(
                        date = "2026-07-06T15:00:00",
                        weather = "cloudy",
                        icon = 3,
                        summary = "Partly cloudy",
                        temperature = 22.7
                    )
                )
            )
        )

        val result = dto.toDomain()

        assertEquals("Kyiv", result.cityName)

        assertEquals("Clear sky", result.current.summary)
        assertEquals(21.5, result.current.temperature, 0.0)
        assertEquals(2, result.current.iconCode)

        assertEquals(1, result.daily.size)
        assertEquals("2026-07-06", result.daily[0].day)
        assertEquals("Sunny day", result.daily[0].summary)
        assertEquals(1, result.daily[0].iconCode)
        assertEquals(18.5, result.daily[0].temperatureMin, 0.0)
        assertEquals(27.3, result.daily[0].temperatureMax, 0.0)

        assertEquals(1, result.hourly.size)
        assertEquals("2026-07-06T15:00:00", result.hourly[0].date)
        assertEquals("Partly cloudy", result.hourly[0].summary)
        assertEquals(22.7, result.hourly[0].temperature, 0.0)
        assertEquals(3, result.hourly[0].iconCode)
    }

    @Test
    fun `WeatherResponseDto toDomain uses empty cityName when placeId is null`() {
        val dto = WeatherResponseDto(
            lat = "50.45",
            lon = "30.52",
            timezone = "Europe/Kyiv",
            units = "metric",
            placeId = null,
            current = CurrentWeatherDto(
                summary = "Clear sky",
                temperature = 21.5,
                iconNum = 2,
                icon = ""
            ),
            daily = DailyForecastDto(data = emptyList()),
            hourly = HourlyForecastDto(data = emptyList())
        )

        val result = dto.toDomain()

        assertEquals(null, result.cityName)
        assertEquals(emptyList<DailyWeather>(), result.daily)
        assertEquals(emptyList<HourlyWeather>(), result.hourly)
    }

    @Test
    fun `WeatherResponseDto toDomain rejects blank required fields`() {
        val valid = validResponse()
        val invalidResponses = listOf(
            valid.copy(lat = ""),
            valid.copy(lon = " "),
            valid.copy(timezone = ""),
            valid.copy(units = " "),
            valid.copy(current = valid.current.copy(summary = ""))
        )

        invalidResponses.forEach { dto ->
            assertThrows(InvalidWeatherResponseException::class.java) { dto.toDomain() }
        }
    }

    @Test
    fun `WeatherResponseDto toDomain rejects non-finite current temperatures`() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { value ->
            val dto = validResponse().let { valid ->
                valid.copy(current = valid.current.copy(temperature = value))
            }

            assertThrows(InvalidWeatherResponseException::class.java) { dto.toDomain() }
        }
    }

    @Test
    fun `WeatherResponseDto toDomain supports empty forecast collections`() {
        val result = validResponse().copy(
            hourly = HourlyForecastDto(emptyList()),
            daily = DailyForecastDto(emptyList())
        ).toDomain()

        assertEquals(emptyList<HourlyWeather>(), result.hourly)
        assertEquals(emptyList<DailyWeather>(), result.daily)
    }

    private fun validResponse() = WeatherResponseDto(
        lat = "50.45",
        lon = "30.52",
        timezone = "Europe/Kyiv",
        units = "metric",
        placeId = "Kyiv",
        current = CurrentWeatherDto(
            summary = "Clear sky",
            temperature = 21.5,
            iconNum = 2,
            icon = "clear"
        ),
        daily = DailyForecastDto(emptyList()),
        hourly = HourlyForecastDto(emptyList())
    )
}
