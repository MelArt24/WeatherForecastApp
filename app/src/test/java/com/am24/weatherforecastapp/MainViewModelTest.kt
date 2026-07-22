package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.DailyWeather
import com.am24.weatherforecastapp.domain.model.HourlyWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.model.GeocodedLocation
import com.am24.weatherforecastapp.domain.model.UserLocation
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.ApiErrorReason
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import com.am24.weatherforecastapp.domain.error.LocationErrorReason
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.am24.weatherforecastapp.domain.usecase.GetCurrentLocationUseCase
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import com.am24.weatherforecastapp.domain.usecase.GetCurrentWeatherUseCase
import com.am24.weatherforecastapp.presentation.mapper.WeatherPresentationMapper
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
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

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
    fun currentLocationRequest_usesResolvedPlaceName() = runTest(testDispatcher) {
        val response = CompletableDeferred<WeatherForecast>()
        val repository = FakeWeatherRepository(
            response = { response.await() }
        )
        val locationRepository = FakeLocationRepository(
            coordinates = UserLocation(
                latitude = 50.45,
                longitude = 30.52,
                placeName = "Brovary"
            )
        )
        val viewModel = viewModel(repository, locationRepository)

        viewModel.requestCurrentLocationWeather()
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        response.complete(successForecast())
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals(WeatherUiStatus.Success, status)
            assertEquals("Brovary", currentWeather?.city)
            assertFalse(isLoading)
        }

        assertEquals("50.45", repository.lastLat)
        assertEquals("30.52", repository.lastLon)
    }

    @Test
    fun currentLocationRequest_fallsBackWhenPlaceNameIsUnavailable() =
        runTest(testDispatcher) {
            val locationRepository = FakeLocationRepository(
                coordinates = UserLocation(
                    latitude = 50.45,
                    longitude = 30.52,
                    placeName = null
                )
            )

            val viewModel = viewModel(
                repository = FakeWeatherRepository(
                    response = { successForecast() }
                ),
                locationRepository = locationRepository
            )

            viewModel.requestCurrentLocationWeather()
            advanceUntilIdle()

            assertEquals(
                "Kyiv",
                viewModel.uiState.value.currentWeather?.city
            )
        }

    @Test
    fun cityRequest_mapsRequestedCityAndSucceeds() = runTest(testDispatcher) {
        val repository = FakeWeatherRepository(response = { successForecast() })
        val viewModel = viewModel(repository)

        viewModel.requestCityWeather("Kyiv")
        advanceUntilIdle()

        assertEquals("Kyiv", repository.lastCity)
        assertEquals("50.45", repository.lastLat)
        assertEquals("30.52", repository.lastLon)
        assertEquals("Kyiv", viewModel.uiState.value.currentWeather?.city)
        assertEquals("12:34", viewModel.uiState.value.currentWeather?.time)
        assertEquals("Clear", viewModel.uiState.value.currentWeather?.condition)
        assertEquals("21\u00B0C", viewModel.uiState.value.currentWeather?.currentTemperature)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun cityRequest_displaysLocalizedGeocoderName() = runTest(testDispatcher) {
        val viewModel = viewModel(
            repository = FakeWeatherRepository(response = { successForecast() }),
            geocodingRepository = FakeGeocodingRepository(localizedName = "Пекін")
        )

        viewModel.requestCityWeather("Пекін")
        advanceUntilIdle()

        assertEquals("Пекін", viewModel.uiState.value.currentWeather?.city)
        assertEquals(WeatherUiStatus.Success, viewModel.uiState.value.status)
    }

    @Test
    fun citySearchWithoutGeocodingResult_emitsCityNotFoundOnce() = runTest(testDispatcher) {
        val viewModel = viewModel(
            repository = FakeWeatherRepository(),
            geocodingRepository = FakeGeocodingRepository(found = false)
        )
        val event = async { viewModel.events.first() }

        viewModel.requestCityWeather("Unknown place")
        advanceUntilIdle()

        val expectedError = WeatherUiError.CitySearch(
            DomainError.Api(ApiErrorReason.NotFound)
        )
        assertEquals(expectedError, viewModel.uiState.value.error)
        assertEquals(WeatherUiStatus.Error, viewModel.uiState.value.status)
        assertEquals(WeatherUiEvent.ShowError(expectedError), event.await())
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
        val expectedError = WeatherUiError.Weather(DomainError.Unknown)
        assertEquals(expectedError, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(WeatherUiEvent.ShowError(expectedError), event.await())
    }

    @Test
    fun offlineRequestWithoutCache_showsOfflineError() = runTest(testDispatcher) {
        val viewModel = viewModel(
            FakeWeatherRepository(response = {
                throw DomainFailureException(DomainError.Network(NetworkErrorReason.Offline))
            })
        )
        val event = async { viewModel.events.first() }

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        val expectedError = WeatherUiError.Weather(
            DomainError.Network(NetworkErrorReason.Offline)
        )
        assertEquals(expectedError, viewModel.uiState.value.error)
        assertEquals(WeatherUiEvent.ShowError(expectedError), event.await())
    }

    @Test
    fun missingLocationPermission_setsPermissionError() = runTest(testDispatcher) {
        val locationRepository = object : LocationRepository {
            override suspend fun getCurrentLocation(): UserLocation {
                throw DomainFailureException(
                    DomainError.Location(LocationErrorReason.PermissionDenied)
                )
            }
            override suspend fun getLastSavedLocation(): UserLocation? = null
            override suspend fun saveLastLocation(location: UserLocation) = Unit
        }
        val viewModel = viewModel(FakeWeatherRepository(), locationRepository)
        val event = async { viewModel.events.first() }

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        assertEquals(WeatherUiStatus.Error, viewModel.uiState.value.status)
        val expectedError = WeatherUiError.Location(
            DomainError.Location(LocationErrorReason.PermissionDenied)
        )
        assertEquals(expectedError, viewModel.uiState.value.error)
        assertEquals(
            WeatherUiEvent.ShowError(expectedError),
            event.await()
        )
    }

    @Test
    fun deniedPermission_withSavedLocation_loadsWeatherWithoutPermissionError() =
        runTest(testDispatcher) {
            val weatherRepository = FakeWeatherRepository(response = { successForecast() })
            val locationRepository = FakeLocationRepository(
                currentFailure = SecurityException("Permission denied"),
                savedLocation = UserLocation(49.84, 24.03, "Lviv")
            )
            val viewModel = viewModel(weatherRepository, locationRepository)

            viewModel.requestCurrentLocationWeather()
            advanceUntilIdle()

            assertEquals(WeatherUiStatus.Success, viewModel.uiState.value.status)
            assertNull(viewModel.uiState.value.error)
            assertEquals("Lviv", viewModel.uiState.value.currentWeather?.city)
            assertEquals("49.84", weatherRepository.lastLat)
            assertEquals("24.03", weatherRepository.lastLon)
        }

    @Test
    fun locationFailure_withoutSavedLocation_setsLocationError() = runTest(testDispatcher) {
        val locationRepository = FakeLocationRepository(
            currentFailure = IllegalStateException("Unavailable")
        )
        val viewModel = viewModel(FakeWeatherRepository(), locationRepository)
        val event = async { viewModel.events.first() }

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        val expectedError = WeatherUiError.Location(DomainError.Unknown)
        assertEquals(expectedError, viewModel.uiState.value.error)
        assertEquals(WeatherUiEvent.ShowError(expectedError), event.await())
    }

    @Test
    fun errorEvent_isConsumedOnlyOncePerFailure() = runTest(testDispatcher) {
        val viewModel = viewModel(FakeWeatherRepository(response = { throw IllegalStateException() }))

        viewModel.requestCurrentLocationWeather()
        advanceUntilIdle()

        assertEquals(
            WeatherUiEvent.ShowError(WeatherUiError.Weather(DomainError.Unknown)),
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
        locationRepository: LocationRepository = FakeLocationRepository(),
        geocodingRepository: GeocodingRepository = FakeGeocodingRepository()
    ) = MainViewModel(
        GetCurrentWeatherUseCase(repository),
        GetCurrentLocationUseCase(locationRepository),
        SearchCityWeatherUseCase(repository, geocodingRepository),
        WeatherPresentationMapper(
            conditionLocalizer = { _, fallback -> fallback },
            clock = Clock.fixed(Instant.parse("2026-07-05T12:34:00Z"), ZoneOffset.UTC)
        )
    )

    private class FakeLocationRepository(
        private val coordinates: UserLocation = UserLocation(50.45, 30.52, "Kyiv"),
        private val currentFailure: Exception? = null,
        private val savedLocation: UserLocation? = null
    ) : LocationRepository {
        override suspend fun getCurrentLocation(): UserLocation {
            currentFailure?.let { throw it }
            return coordinates
        }
        override suspend fun getLastSavedLocation(): UserLocation? = savedLocation
        override suspend fun saveLastLocation(location: UserLocation) = Unit
    }

    private class FakeGeocodingRepository(
        private val localizedName: String? = null,
        private val found: Boolean = true
    ) : GeocodingRepository {
        override suspend fun searchLocation(query: String): GeocodedLocation? =
            if (found) GeocodedLocation(50.45, 30.52, localizedName ?: query) else null

        override suspend fun resolvePlaceName(latitude: Double, longitude: Double): String? = null
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
