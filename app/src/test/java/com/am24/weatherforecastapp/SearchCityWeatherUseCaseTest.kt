package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.GeocodedLocation
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.data.network.NetworkMonitor
import com.am24.weatherforecastapp.domain.CityNotFoundException
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
            GeocodedLocation(39.9042, 116.4074, "РџРµРєС–РЅ")
        )

        val result = SearchCityWeatherUseCase(weatherRepository, geocodingRepository)("РџРµРєС–РЅ")

        assertEquals("РџРµРєС–РЅ", geocodingRepository.query)
        assertEquals("39.9042", weatherRepository.lat)
        assertEquals("116.4074", weatherRepository.lon)
        assertEquals(geocodingRepository.query, weatherRepository.city)
        assertEquals("РџРµРєС–РЅ", result.city)
    }

    @Test
    fun missingLocalizedName_usesNormalizedOriginalQuery() = runTest {
        val useCase = SearchCityWeatherUseCase(
            FakeWeatherRepository(),
            FakeGeocodingRepository(GeocodedLocation(50.45, 30.52, null))
        )

        val result = useCase("  РљРёС—РІ   РјС–СЃС‚Рѕ ")

        assertEquals("РљРёС—РІ РјС–СЃС‚Рѕ", result.city)
    }

    @Test(expected = CityNotFoundException::class)
    fun missingGeocodingResult_producesCityNotFound() = runTest {
        SearchCityWeatherUseCase(
            FakeWeatherRepository(),
            FakeGeocodingRepository(null)
        )("РќРµРІС–РґРѕРјРµ РјС–СЃС†Рµ")
    }

    @Test(expected = CityNotFoundException::class)
    fun blankQuery_producesCityNotFound() = runTest {
        SearchCityWeatherUseCase(
            FakeWeatherRepository(),
            FakeGeocodingRepository(null)
        )("   ")
    }

    @Test
    fun offlineSearch_usesCachedCityWithoutGeocoding() = runTest {
        val weatherRepository = FakeWeatherRepository()
        val geocodingRepository = FakeGeocodingRepository(null)

        val result = SearchCityWeatherUseCase(
            weatherRepository,
            geocodingRepository,
            NetworkMonitor { false }
        )("  Kyiv ")

        assertEquals("Kyiv", result.city)
        assertEquals("Kyiv", weatherRepository.city)
        assertNull(geocodingRepository.query)
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
