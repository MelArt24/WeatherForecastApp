package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.utils.TransliterationUtils
import retrofit2.HttpException

class SearchCityWeatherUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(city: String): CityWeatherResult {
        return try {
            CityWeatherResult(
                forecast = requestWeather(city),
                city = city
            )
        } catch (e: HttpException) {
            if (e.code() == 400) {
                val transliteratedCity = TransliterationUtils.transliterate(city)
                CityWeatherResult(
                    forecast = requestWeather(transliteratedCity),
                    city = transliteratedCity
                )
            } else {
                throw e
            }
        }
    }

    private suspend fun requestWeather(city: String): WeatherForecast {
        return weatherRepository.getWeatherData(city = city)
    }
}
