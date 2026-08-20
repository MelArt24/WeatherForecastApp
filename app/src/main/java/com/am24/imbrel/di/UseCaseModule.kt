package com.am24.imbrel.di

import com.am24.imbrel.domain.usecase.GetCurrentLocationUseCase
import com.am24.imbrel.domain.usecase.GetCurrentWeatherUseCase
import com.am24.imbrel.domain.usecase.SearchCityWeatherUseCase
import com.am24.imbrel.presentation.mapper.WeatherPresentationMapper
import org.koin.dsl.module

val useCaseModule =
    module {
        factory {
            GetCurrentLocationUseCase(
                locationRepository = get(),
                networkMonitor = get(),
            )
        }

        factory {
            GetCurrentWeatherUseCase(
                weatherRepository = get(),
            )
        }

        factory {
            SearchCityWeatherUseCase(
                weatherRepository = get(),
                geocodingRepository = get(),
                networkMonitor = get(),
            )
        }

        factory {
            WeatherPresentationMapper(conditionLocalizer = get())
        }
    }
