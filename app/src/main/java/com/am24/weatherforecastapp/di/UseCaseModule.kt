package com.am24.weatherforecastapp.di

import com.am24.weatherforecastapp.domain.usecase.GetCurrentWeatherUseCase
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        GetCurrentWeatherUseCase(
            weatherRepository = get()
        )
    }

    factory {
        SearchCityWeatherUseCase(
            weatherRepository = get()
        )
    }
}