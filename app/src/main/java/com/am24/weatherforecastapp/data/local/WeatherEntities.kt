package com.am24.weatherforecastapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "cached_weather", primaryKeys = ["cacheKey"])
data class CachedWeatherEntity(
    val cacheKey: String,
    val cityName: String?,
    val currentSummary: String,
    val currentTemperature: Double,
    val currentIconCode: Int,
    val cachedAtMillis: Long
)

@Entity(
    tableName = "cached_hourly_weather",
    primaryKeys = ["cacheKey", "position"],
    foreignKeys = [ForeignKey(
        entity = CachedWeatherEntity::class,
        parentColumns = ["cacheKey"],
        childColumns = ["cacheKey"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cacheKey")]
)
data class CachedHourlyWeatherEntity(
    val cacheKey: String,
    val position: Int,
    val date: String,
    val summary: String,
    val temperature: Double,
    val iconCode: Int
)

@Entity(
    tableName = "cached_daily_weather",
    primaryKeys = ["cacheKey", "position"],
    foreignKeys = [ForeignKey(
        entity = CachedWeatherEntity::class,
        parentColumns = ["cacheKey"],
        childColumns = ["cacheKey"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cacheKey")]
)
data class CachedDailyWeatherEntity(
    val cacheKey: String,
    val position: Int,
    val day: String,
    val summary: String,
    val iconCode: Int,
    val temperatureMin: Double,
    val temperatureMax: Double
)
