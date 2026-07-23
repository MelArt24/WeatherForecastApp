package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.data.cache.WeatherCachePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherCachePolicyTest {
    private val policy = WeatherCachePolicy(ttlMillis = 1_000)

    @Test
    fun entryAtTtlBoundary_isFresh() {
        assertTrue(policy.isFresh(cachedAtMillis = 1_000, nowMillis = 2_000))
    }

    @Test
    fun entryPastTtlBoundary_isExpired() {
        assertFalse(policy.isFresh(cachedAtMillis = 1_000, nowMillis = 2_001))
    }

    @Test
    fun recentEntry_isFresh() {
        assertTrue(policy.isFresh(cachedAtMillis = 1_500, nowMillis = 2_000))
    }

    @Test
    fun futureTimestamp_isNotFresh() {
        assertFalse(policy.isFresh(cachedAtMillis = 2_001, nowMillis = 2_000))
    }

    @Test
    fun negativeTtl_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            WeatherCachePolicy(ttlMillis = -1)
        }
    }
}
