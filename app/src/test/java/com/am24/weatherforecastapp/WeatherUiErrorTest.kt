package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.error.ApiErrorReason
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.LocationErrorReason
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
import com.am24.weatherforecastapp.presentation.WeatherUiError
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherUiErrorTest {
    @Test
    fun networkErrors_mapToSpecificMessages() {
        assertMessage(R.string.offline_error, DomainError.Network(NetworkErrorReason.Offline))
        assertMessage(R.string.timeout_error, DomainError.Network(NetworkErrorReason.Timeout))
        assertMessage(
            R.string.connection_error,
            DomainError.Network(NetworkErrorReason.ConnectionFailed)
        )
    }

    @Test
    fun apiErrors_mapToSpecificMessages() {
        assertMessage(R.string.api_access_error, DomainError.Api(ApiErrorReason.Unauthorized))
        assertMessage(R.string.weather_not_found, DomainError.Api(ApiErrorReason.NotFound))
        assertMessage(R.string.rate_limit_error, DomainError.Api(ApiErrorReason.RateLimited))
        assertMessage(
            R.string.invalid_response_error,
            DomainError.Api(ApiErrorReason.InvalidResponse)
        )
        assertMessage(R.string.server_error, DomainError.Api(ApiErrorReason.ServerError))
        assertMessage(R.string.weather_error, DomainError.Api(ApiErrorReason.RequestFailed))
        assertEquals(
            R.string.city_not_found,
            WeatherUiError.CitySearch(
                DomainError.Api(ApiErrorReason.NotFound)
            ).messageResource()
        )
    }

    @Test
    fun locationErrors_mapToSpecificMessages() {
        assertMessage(
            R.string.location_permission_denied,
            DomainError.Location(LocationErrorReason.PermissionDenied),
            ::locationError
        )
        assertMessage(
            R.string.location_unavailable_error,
            DomainError.Location(LocationErrorReason.Unavailable),
            ::locationError
        )
        assertMessage(
            R.string.location_resolution_error,
            DomainError.Location(LocationErrorReason.ResolutionFailed),
            ::locationError
        )
    }

    @Test
    fun unknownError_mapsToSafeGenericMessage() {
        assertMessage(R.string.unknown_error, DomainError.Unknown)
    }

    private fun assertMessage(
        expected: Int,
        error: DomainError,
        uiError: (DomainError) -> WeatherUiError = WeatherUiError::Weather
    ) {
        assertEquals(expected, uiError(error).messageResource())
    }

    private fun locationError(error: DomainError): WeatherUiError = WeatherUiError.Location(error)
}
