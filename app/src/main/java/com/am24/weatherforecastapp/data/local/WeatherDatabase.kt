package com.am24.weatherforecastapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedWeatherEntity::class,
        CachedHourlyWeatherEntity::class,
        CachedDailyWeatherEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
