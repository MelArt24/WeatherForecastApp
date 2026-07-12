package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.model.LocationCoordinates
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.am24.weatherforecastapp.domain.usecase.GetCurrentLocationUseCase
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.domain.usecase.GetCurrentWeatherUseCase
import com.am24.weatherforecastapp.domain.usecase.MapWeatherForecastToPresentationUseCase
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import com.am24.weatherforecastapp.presentation.WeatherUiError
import com.am24.weatherforecastapp.presentation.WeatherUiEvent
import com.am24.weatherforecastapp.presentation.WeatherUiState
import com.am24.weatherforecastapp.presentation.WeatherUiStatus
import com.am24.weatherforecastapp.presentation.model.WeatherModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initialState_isExplicitAndEmpty() {
        val viewModel = viewModel(FakeWeatherRepository())

        assertEquals(WeatherUiState(), viewModel.uiState.value)
        assertEquals(WeatherUiStatus.Initial, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun currentLocationRequest_isLoadingThenSucceeds() = runTest(testDispatcher) {
        val response = CompletableDeferred<WeatherForecast>()
        val repository = FakeWeatherRepository(response = { response.await() })
        val viewModel = viewModel(repository, FakeLocationRepository())

        viewModel.requestCurrentLocationWeather()
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)

        response.complete(successForecast())
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals(WeatherUiStatus.Success, status)
            assertFalse(isLoading)
            assertEquals("Kyiv", currentWeather?.city)
            assertEquals(1, dailyWeather.size)
            assertNull(selectedDay)
        }
        assertEquals("50.45", repository.lastLat)
        assertEquals("30.52", repository.lastLon)
    }

    @Test
    fun cityRequest_mapsRequestedCityAndSucceeds() = runTest(testDispatcher) {
        val repository = FakeWeatherRepository(response = { successForecast() })
        val viewModel = viewModel(repository)

        viewModel.requestCityWeather("Kyiv")
        advanceUntilIdle()

        assertEquals("Kyiv", repository.lastCity)
        assertEquals("Kyiv", viewModel.uiState.value.currentWeather?.city)
        assertEquals("Now", viewModel.uiState.value.currentWeather?.time)
        assertEquals("Clear", viewModel.uiState.value.currentWeather?.condition)
        assertEquals("21\u00B0C", viewModel.uiState.value.currentWeather?.currentTemperature)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun emptyForecast_hasExplicitEmptyState() = runTest(testDispatcher) {
        val viewModel = viewModel(FakeWeatherRepository(response = { emptyForecast() }), FakeLocationRepository())

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        assertEquals(WeatherUiStatus.Empty, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun failedRequest_setsErrorStopsLoadingAndEmitsOneEvent() = runTest(testDispatcher) {
        val viewModel = viewModel(FakeWeatherRepository(response = { throw IllegalStateException() }))
        val event = async { viewModel.events.first() }

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        assertEquals(WeatherUiStatus.Error, viewModel.uiState.value.status)
        assertEquals(WeatherUiError.Location, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(WeatherUiEvent.ShowError(R.string.location_error), event.await())
    }

    @Test
    fun missingLocationPermission_setsPermissionError() = runTest(testDispatcher) {
        val locationRepository = object : LocationRepository {
            override suspend fun getCurrentLocation(): LocationCoordinates {
                throw SecurityException("Permission denied")
            }
        }
        val viewModel = viewModel(FakeWeatherRepository(), locationRepository)
        val event = async { viewModel.events.first() }

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        assertEquals(WeatherUiStatus.Error, viewModel.uiState.value.status)
        assertEquals(WeatherUiError.LocationPermissionDenied, viewModel.uiState.value.error)
        assertEquals(
            WeatherUiEvent.ShowError(R.string.location_permission_denied),
            event.await()
        )
    }

    @Test
    fun errorEvent_isConsumedOnlyOncePerFailure() = runTest(testDispatcher) {
        val viewModel = viewModel(FakeWeatherRepository(response = { throw IllegalStateException() }))

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        assertEquals(
            WeatherUiEvent.ShowError(R.string.location_error),
            viewModel.events.first()
        )
        val secondCollector = async { viewModel.events.first() }
        runCurrent()
        assertFalse(secondCollector.isCompleted)
        secondCollector.cancel()
    }

    @Test
    fun selectedDay_updatesOnlySelection() {
        val viewModel = viewModel(FakeWeatherRepository())
        val selected = weatherModel("Tomorrow")

        viewModel.setSelectedDay(selected)

        assertSame(selected, viewModel.uiState.value.selectedDay)
        assertSame(selected, viewModel.uiState.value.displayedWeather)
        assertEquals(WeatherUiStatus.Initial, viewModel.uiState.value.status)
    }

    private fun viewModel(
        repository: WeatherRepository,
        locationRepository: LocationRepository = FakeLocationRepository()
    ) = MainViewModel(
        GetCurrentWeatherUseCase(repository),
        GetCurrentLocationUseCase(locationRepository),
        SearchCityWeatherUseCase(repository),
        MapWeatherForecastToPresentationUseCase()
    )

    private class FakeLocationRepository(
        private val coordinates: LocationCoordinates = LocationCoordinates(50.45, 30.52)
    ) : LocationRepository {
        override suspend fun getCurrentLocation(): LocationCoordinates = coordinates
    }

    private class FakeWeatherRepository(
        private val response: suspend () -> WeatherForecast = { successForecast() }
    ) : WeatherRepository {
        var lastCity: String? = null
            private set
        var lastLat: String? = null
            private set
        var lastLon: String? = null
            private set

        override suspend fun getWeatherData(
            lat: String?,
            lon: String?,
            city: String?
        ): WeatherForecast {
            lastLat = lat
            lastLon = lon
            lastCity = city
            return response()
        }
    }

    companion object {
        private fun successForecast() = WeatherForecast(
            cityName = "Kyiv",
            current = CurrentWeather("Clear", 21.4, 1),
            hourly = listOf(HourlyWeather("2026-07-05T12:00:00", "Clear", 21.4, 1)),
            daily = listOf(DailyWeather("2026-07-05", "Clear", 1, 18.0, 24.0))
        )

        private fun emptyForecast() = WeatherForecast(
            cityName = "Kyiv",
            current = CurrentWeather("Clear", 21.4, 1),
            hourly = emptyList(),
            daily = emptyList()
        )

        private fun weatherModel(time: String) = WeatherModel(
            city = "Kyiv",
            time = time,
            condition = "Clear",
            currentTemperature = "21\u00B0C",
            minimumTemperature = "18",
            maximumTemperature = "24",
            imageURL = "1",
            hours = "[]"
        )
    }
}
