package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.GeocodedLocation
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.domain.usecase.CityNotFoundException
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchCityWeatherUseCaseTest {
    @Test
    fun localizedQuery_resolvesCoordinatesAndPreservesLocalizedName() = runTest {
        val weatherRepository = FakeWeatherRepository()
        val geocodingRepository = FakeGeocodingRepository(
            GeocodedLocation(39.9042, 116.4074, "Пекін")
        )

        val result = SearchCityWeatherUseCase(weatherRepository, geocodingRepository)("Пекін")

        assertEquals("Пекін", geocodingRepository.query)
        assertEquals("39.9042", weatherRepository.lat)
        assertEquals("116.4074", weatherRepository.lon)
        assertNull(weatherRepository.city)
        assertEquals("Пекін", result.city)
    }

    @Test
    fun missingLocalizedName_usesNormalizedOriginalQuery() = runTest {
        val useCase = SearchCityWeatherUseCase(
            FakeWeatherRepository(),
            FakeGeocodingRepository(GeocodedLocation(50.45, 30.52, null))
        )

        val result = useCase("  Київ   місто ")

        assertEquals("Київ місто", result.city)
    }

    @Test(expected = CityNotFoundException::class)
    fun missingGeocodingResult_producesCityNotFound() = runTest {
        SearchCityWeatherUseCase(
            FakeWeatherRepository(),
            FakeGeocodingRepository(null)
        )("Невідоме місце")
    }

    @Test(expected = CityNotFoundException::class)
    fun blankQuery_producesCityNotFound() = runTest {
        SearchCityWeatherUseCase(
            FakeWeatherRepository(),
            FakeGeocodingRepository(null)
        )("   ")
    }

    private class FakeGeocodingRepository(
        private val result: GeocodedLocation?
    ) : GeocodingRepository {
        var query: String? = null

        override suspend fun searchLocation(query: String): GeocodedLocation? {
            this.query = query
            return result
        }

        override suspend fun resolvePlaceName(latitude: Double, longitude: Double): String? = null
    }

    private class FakeWeatherRepository : WeatherRepository {
        var lat: String? = null
        var lon: String? = null
        var city: String? = null

        override suspend fun getWeatherData(
            lat: String?,
            lon: String?,
            city: String?
        ): WeatherForecast {
            this.lat = lat
            this.lon = lon
            this.city = city
            return WeatherForecast(
                cityName = "Beijing",
                current = CurrentWeather("Clear", 21.4, 2),
                hourly = emptyList(),
                daily = emptyList()
            )
        }
    }
}
