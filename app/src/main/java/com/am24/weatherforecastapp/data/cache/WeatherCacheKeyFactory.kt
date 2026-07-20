package com.am24.weatherforecastapp.data.cache

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object WeatherCacheKeyFactory {
    private const val COORDINATE_SCALE = 4

    fun create(lat: String?, lon: String?, city: String?): String {
        if (lat != null || lon != null) {
            require(lat != null && lon != null) { "Both latitude and longitude are required" }
            return "lat:${normalizeCoordinate(lat)}|lon:${normalizeCoordinate(lon)}"
        }

        val normalizedCity = city?.trim()?.replace(Regex("\\s+"), " ")
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }
        requireNotNull(normalizedCity) { "Coordinates or city are required" }
        return "city:$normalizedCity"
    }

    private fun normalizeCoordinate(value: String): String =
        BigDecimal(value.trim())
            .setScale(COORDINATE_SCALE, RoundingMode.HALF_UP)
            .toPlainString()
}
