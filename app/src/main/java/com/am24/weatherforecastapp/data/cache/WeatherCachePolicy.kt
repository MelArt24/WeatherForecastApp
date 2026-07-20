package com.am24.weatherforecastapp.data.cache

import java.time.Clock

fun interface TimeProvider {
    fun currentTimeMillis(): Long
}

class ClockTimeProvider(
    private val clock: Clock = Clock.systemUTC()
) : TimeProvider {
    override fun currentTimeMillis(): Long = clock.millis()
}

class WeatherCachePolicy(
    val ttlMillis: Long = DEFAULT_TTL_MILLIS
) {
    init {
        require(ttlMillis >= 0) { "Cache TTL must not be negative" }
    }

    fun isFresh(cachedAtMillis: Long, nowMillis: Long): Boolean =
        nowMillis >= cachedAtMillis && nowMillis - cachedAtMillis <= ttlMillis

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 20 * 60 * 1_000L
    }
}
