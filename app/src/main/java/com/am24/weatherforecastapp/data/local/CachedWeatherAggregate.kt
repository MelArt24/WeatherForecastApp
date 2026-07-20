package com.am24.weatherforecastapp.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class CachedWeatherAggregate(
    @Embedded val weather: CachedWeatherEntity,
    @Relation(parentColumn = "cacheKey", entityColumn = "cacheKey")
    val hourly: List<CachedHourlyWeatherEntity>,
    @Relation(parentColumn = "cacheKey", entityColumn = "cacheKey")
    val daily: List<CachedDailyWeatherEntity>
)
