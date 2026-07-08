package com.am24.weatherforecastapp

import android.app.Application
import com.am24.weatherforecastapp.di.appModule
import com.am24.weatherforecastapp.di.useCaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WeatherForecastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WeatherForecastApplication)
            modules(appModule, useCaseModule)
        }
    }
}
