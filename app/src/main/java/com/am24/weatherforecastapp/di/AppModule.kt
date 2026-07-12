package com.am24.weatherforecastapp.di

import com.am24.weatherforecastapp.BuildConfig
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.data.remote.RetrofitClient
import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.data.repository.WeatherRepositoryImpl
import com.am24.weatherforecastapp.data.repository.LocationRepositoryImpl
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<LocationRepository> { LocationRepositoryImpl(get()) }
    single<WeatherApiService> { RetrofitClient.weatherApiService }

    single<WeatherRepository> {
        WeatherRepositoryImpl(
            apiService = get(),
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
