package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.model.CurrentWeather
import com.am24.weatherforecastapp.domain.model.WeatherForecast
import com.am24.weatherforecastapp.domain.repository.WeatherRepository
import com.am24.weatherforecastapp.domain.usecase.SearchCityWeatherUseCase
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SearchCityWeatherUseCaseTest {
    @Test
    fun invoke_usesOriginalCityWhenRequestSucceeds() = runTest {
        val repository = FakeWeatherRepository(
            responses = mutableListOf(FakeResponse.Success(emptyForecast()))
        )
        val useCase = SearchCityWeatherUseCase(repository)

        val result = useCase("Kyiv")

        assertEquals(listOf("Kyiv"), repository.requestedCities)
        assertEquals("Kyiv", result.city)
    }

    @Test
    fun invoke_retriesWithTransliteratedCityWhenBadRequestFailsFirstSearch() = runTest {
        val repository = FakeWeatherRepository(
            responses = mutableListOf(
                FakeResponse.Failure(httpException(400)),
                FakeResponse.Success(emptyForecast())
            )
        )
        val useCase = SearchCityWeatherUseCase(repository)

        val result = useCase("Київ")

        assertEquals(listOf("Київ", "Kyiv"), repository.requestedCities)
        assertEquals("Kyiv", result.city)
    }

    @Test(expected = HttpException::class)
    fun invoke_rethrowsHttpExceptionWhenTransliteratedRetryFails() = runTest {
        val repository = FakeWeatherRepository(
            responses = mutableListOf(
                FakeResponse.Failure(httpException(400)),
                FakeResponse.Failure(httpException(400))
            )
        )
        val useCase = SearchCityWeatherUseCase(repository)

        useCase("Київ")
    }

    private class FakeWeatherRepository(
        private val responses: MutableList<FakeResponse>
    ) : WeatherRepository {
        val requestedCities = mutableListOf<String?>()

        override suspend fun getWeatherData(
            lat: String?,
            lon: String?,
            city: String?
        ): WeatherForecast {
            requestedCities.add(city)
            return when (val response = responses.removeAt(0)) {
                is FakeResponse.Success -> response.forecast
                is FakeResponse.Failure -> throw response.exception
            }
        }
    }

    private sealed class FakeResponse {
        data class Success(val forecast: WeatherForecast) : FakeResponse()
        data class Failure(val exception: HttpException) : FakeResponse()
    }

    private fun httpException(code: Int): HttpException {
        val body = "".toResponseBody("text/plain".toMediaType())
        return HttpException(Response.error<String>(code, body))
    }

    private fun emptyForecast() = WeatherForecast(
        cityName = "Kyiv",
        current = CurrentWeather(
            summary = "Clear",
            temperature = 21.4,
            iconCode = 1
        ),
        hourly = emptyList(),
        daily = emptyList()
    )
}
