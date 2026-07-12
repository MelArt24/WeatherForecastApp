package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.WeatherCondition
import com.am24.weatherforecastapp.domain.model.weatherConditionFromIcon
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionMappingTest {
    @Test
    fun allDocumentedIconCodes_mapToExpectedConditions() {
        assertCodes(WeatherCondition.Clear, 2, 26)
        assertCodes(WeatherCondition.MostlyClear, 3, 27)
        assertCodes(WeatherCondition.PartlyCloudy, 4, 28)
        assertCodes(WeatherCondition.Cloudy, 5, 6, 29, 30)
        assertCodes(WeatherCondition.Overcast, 7, 8, 31)
        assertCodes(WeatherCondition.Fog, 9)
        assertCodes(WeatherCondition.Drizzle, 10)
        assertCodes(WeatherCondition.Rain, 11, 12, 13, 32)
        assertCodes(WeatherCondition.Thunderstorm, 14, 15, 33)
        assertCodes(WeatherCondition.Snow, 16, 17, 18, 19, 34)
        assertCodes(WeatherCondition.Sleet, 20, 21, 22, 35)
        assertCodes(WeatherCondition.FreezingRain, 23, 24, 36)
        assertCodes(WeatherCondition.Hail, 25)
    }

    @Test
    fun unavailableAndUnknownCodes_mapToUnknown() {
        assertEquals(WeatherCondition.Unknown, weatherConditionFromIcon(1))
        assertEquals(WeatherCondition.Unknown, weatherConditionFromIcon(999))
    }

    private fun assertCodes(condition: WeatherCondition, vararg codes: Int) {
        codes.forEach { code -> assertEquals("icon $code", condition, weatherConditionFromIcon(code)) }
    }
}
