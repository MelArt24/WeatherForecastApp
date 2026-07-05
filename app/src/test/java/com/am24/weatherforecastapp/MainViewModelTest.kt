package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.data.remote.AllDayData
import com.am24.weatherforecastapp.data.remote.CurrentWeather
import com.am24.weatherforecastapp.data.remote.DailyData
import com.am24.weatherforecastapp.data.remote.DailyForecast
import com.am24.weatherforecastapp.data.remote.HourlyData
import com.am24.weatherforecastapp.data.remote.HourlyForecast
import com.am24.weatherforecastapp.data.remote.WeatherResponse
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
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
    fun requestWeatherData_usesRepositoryAndUpdatesWeatherState() = runTest(testDispatcher) {
        val repository = FakeWeatherRepository(successResponse())
        val viewModel = MainViewModel(repository)

        viewModel.requestWeatherData(city = "Kyiv")
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
        private val response: WeatherResponse
    ) : WeatherRepository {
        var lastCity: String? = null
            private set

        override suspend fun getWeatherData(
            lat: String?,
            lon: String?,
            city: String?
        ): WeatherResponse {
            lastCity = city
            return response
        }
    }

    private fun successResponse() = WeatherResponse(
        lat = "50.45",
        lon = "30.52",
        timezone = "UTC",
        units = "metric",
        placeId = "Kyiv",
        current = CurrentWeather(
            icon = "clear",
            iconNum = 1,
            summary = "Clear",
            temperature = 21.4
        ),
        hourly = HourlyForecast(
            data = listOf(
                HourlyData(
                    date = "2026-07-05T12:00:00",
                    weather = "clear",
                    icon = 1,
                    summary = "Clear",
                    temperature = 21.4
                )
            )
        ),
        daily = DailyForecast(
            data = listOf(
                DailyData(
                    day = "2026-07-05",
                    weather = "clear",
                    icon = 1,
                    summary = "Clear",
                    allDay = AllDayData(
                        weather = "clear",
                        icon = 1,
                        temperature = 20.0,
                        temperatureMin = 18.0,
                        temperatureMax = 24.0
                    )
                )
            )
        )
    )
}
