package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.domain.usecase.GetCurrentWeatherUseCase
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun requestCityWeather_usesRepositoryAndUpdatesWeatherState() = runTest(testDispatcher) {
        val repository = FakeWeatherRepository(successForecast())
        val getCurrentWeatherUseCase = GetCurrentWeatherUseCase(repository)
        val searchCityWeatherUseCase = SearchCityWeatherUseCase(repository)
        val viewModel = MainViewModel(getCurrentWeatherUseCase, searchCityWeatherUseCase)

        viewModel.requestCityWeather(city = "Kyiv")
        advanceUntilIdle()

        assertEquals("Kyiv", repository.lastCity)
        assertEquals("Kyiv", viewModel.dataCurrent.value?.city)
        assertEquals("Now", viewModel.dataCurrent.value?.time)
        assertEquals("Clear", viewModel.dataCurrent.value?.condition)
        assertEquals("21°C", viewModel.dataCurrent.value?.currentTemperature)
        assertEquals(1, viewModel.dataList.value.size)
        assertEquals("2026-07-05", viewModel.dataList.value.first().time)
    }

    private class FakeWeatherRepository(
        private val response: WeatherForecast
    ) : WeatherRepository {
        var lastCity: String? = null
            private set

        override suspend fun getWeatherData(
            lat: String?,
            lon: String?,
            city: String?
        ): WeatherForecast {
            lastCity = city
            return response
        }
    }

    private fun successForecast() = WeatherForecast(
        cityName = "Kyiv",
        current = CurrentWeather(
            summary = "Clear",
            temperature = 21.4,
            iconCode = 1
        ),
        hourly = listOf(
            HourlyWeather(
                date = "2026-07-05T12:00:00",
                summary = "Clear",
                temperature = 21.4,
                iconCode = 1
            )
        ),
        daily = listOf(
            DailyWeather(
                day = "2026-07-05",
                summary = "Clear",
                iconCode = 1,
                temperatureMin = 18.0,
                temperatureMax = 24.0
            )
        )
    )
}
