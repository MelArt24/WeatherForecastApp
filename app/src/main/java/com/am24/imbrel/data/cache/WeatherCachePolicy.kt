package com.am24.imbrel.data.cache

import java.time.Clock

fun interface TimeProvider {
    fun currentTimeMillis(): Long
}

class ClockTimeProvider(
    private val clock: Clock = Clock.systemUTC(),
) : TimeProvider {
    override fun currentTimeMillis(): Long = clock.millis()
}

/**
 * Defines two independent cache windows.
 *
 * Entries at either age limit remain valid because both boundaries are inclusive. A timestamp
 * later than [TimeProvider.currentTimeMillis], or an age that cannot be represented by [Long], is
 * rejected instead of being treated as fresh. [ttlMillis] is the normal freshness window, while
 * [maxOfflineAgeMillis] is the longer fallback window used when the network or remote request is
 * unavailable.
 */
class WeatherCachePolicy(
    val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    val maxOfflineAgeMillis: Long = DEFAULT_MAX_OFFLINE_AGE_MILLIS,
) {
    init {
        require(ttlMillis >= 0) { "Cache TTL must not be negative" }
        require(maxOfflineAgeMillis >= 0) { "Maximum offline age must not be negative" }
    }

    fun isFresh(
        cachedAtMillis: Long,
        nowMillis: Long,
    ): Boolean = cacheAgeMillis(cachedAtMillis, nowMillis)?.let { it <= ttlMillis } == true

    fun isUsableOffline(
        cachedAtMillis: Long,
        nowMillis: Long,
    ): Boolean = cacheAgeMillis(cachedAtMillis, nowMillis)?.let { it <= maxOfflineAgeMillis } == true

    private fun cacheAgeMillis(
        cachedAtMillis: Long,
        nowMillis: Long,
    ): Long? {
        if (nowMillis < cachedAtMillis) return null

        return try {
            Math.subtractExact(nowMillis, cachedAtMillis)
        } catch (_: ArithmeticException) {
            null
        }
    }

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 20 * 60 * 1_000L
        const val DEFAULT_MAX_OFFLINE_AGE_MILLIS: Long = 24 * 60 * 60 * 1_000L
    }
}
