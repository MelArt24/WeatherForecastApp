package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.data.cache.WeatherCacheKeyFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WeatherCacheKeyFactoryTest {
    @Test
    fun coordinates_areTrimmedAndRoundedToStableKeys() {
        assertEquals(
            "lat:50.4501|lon:30.5235",
            WeatherCacheKeyFactory.create(" 50.45005 ", "30.52345", city = null)
        )
    }

    @Test
    fun negativeCoordinates_areRoundedConsistently() {
        assertEquals(
            "lat:-33.8689|lon:151.2093",
            WeatherCacheKeyFactory.create("-33.86885", "151.20925", city = null)
        )
    }

    @Test
    fun cityName_isTrimmedCollapsedAndCaseNormalized() {
        assertEquals(
            "city:new york",
            WeatherCacheKeyFactory.create(lat = null, lon = null, city = "  New   YORK  ")
        )
    }

    @Test
    fun onlyOneCoordinate_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            WeatherCacheKeyFactory.create(lat = "50.45", lon = null, city = "Kyiv")
        }
    }

    @Test
    fun blankCityWithoutCoordinates_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            WeatherCacheKeyFactory.create(lat = null, lon = null, city = "   ")
        }
    }

    @Test
    fun missingCoordinatesAndCity_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            WeatherCacheKeyFactory.create(lat = null, lon = null, city = null)
        }
    }

    @Test
    fun unusableCoordinate_isRejected() {
        assertThrows(NumberFormatException::class.java) {
            WeatherCacheKeyFactory.create(lat = "north", lon = "30.52", city = null)
        }
    }
}
