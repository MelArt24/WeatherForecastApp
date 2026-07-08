package com.am24.weatherforecastapp.di

import com.am24.weatherforecastapp.BuildConfig
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.data.remote.RetrofitClient
import com.am24.weatherforecastapp.data.remote.WeatherApiService
import com.am24.weatherforecastapp.data.repository.WeatherRepositoryImpl
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
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
            searchCityWeatherUseCase = get())
    }
}
