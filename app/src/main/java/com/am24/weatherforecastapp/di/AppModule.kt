package com.am24.weatherforecastapp.di

import androidx.room.Room
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.am24.weatherforecastapp.BuildConfig
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.data.remote.RetrofitClient
import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.data.cache.ClockTimeProvider
import com.am24.weatherforecastapp.data.cache.TimeProvider
import com.am24.weatherforecastapp.data.cache.WeatherCachePolicy
import com.am24.weatherforecastapp.data.network.AndroidNetworkMonitor
import com.am24.weatherforecastapp.data.network.NetworkMonitor
import com.am24.weatherforecastapp.data.local.RoomWeatherLocalDataSource
import com.am24.weatherforecastapp.data.local.WeatherDatabase
import com.am24.weatherforecastapp.data.local.WeatherLocalDataSource
import com.am24.weatherforecastapp.data.local.DataStoreSavedLocationLocalDataSource
import com.am24.weatherforecastapp.data.local.SavedLocationLocalDataSource
import com.am24.weatherforecastapp.data.repository.WeatherRepositoryImpl
import com.am24.weatherforecastapp.data.repository.LocationRepositoryImpl
import com.am24.weatherforecastapp.data.repository.GeocodingRepositoryImpl
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.presentation.AndroidWeatherConditionLocalizer
import com.am24.weatherforecastapp.presentation.WeatherConditionLocalizer
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private val Context.locationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "last_current_location"
)

val appModule = module {
    single<NetworkMonitor> { AndroidNetworkMonitor(get()) }
    single<DataStore<Preferences>> { get<Context>().locationDataStore }
    single<SavedLocationLocalDataSource> { DataStoreSavedLocationLocalDataSource(get()) }
    single<GeocodingRepository> { GeocodingRepositoryImpl(get()) }
    single<LocationRepository> {
        LocationRepositoryImpl(
            context = get(),
            geocodingRepository = get(),
            savedLocationLocalDataSource = get()
        )
    }
    single<WeatherConditionLocalizer> { AndroidWeatherConditionLocalizer(get()) }
    single<WeatherApiService> { RetrofitClient.weatherApiService }
    single {
        Room.databaseBuilder(
            get(),
            WeatherDatabase::class.java,
            "weather_cache.db"
        ).build()
    }
    single { get<WeatherDatabase>().weatherDao() }
    single<WeatherLocalDataSource> { RoomWeatherLocalDataSource(get()) }
    single<TimeProvider> { ClockTimeProvider() }
    single { WeatherCachePolicy() }

    single<WeatherRepository> {
        WeatherRepositoryImpl(
            apiService = get(),
            localDataSource = get(),
            timeProvider = get(),
            cachePolicy = get(),
            networkMonitor = get(),
            apiKey = BuildConfig.WEATHER_API_KEY
        )
    }

    viewModel {
        MainViewModel(
            getCurrentWeatherUseCase = get(),
            getCurrentLocationUseCase = get(),
            searchCityWeatherUseCase = get(),
            mapWeatherForecastToPresentationUseCase = get()
        )
    }
}
