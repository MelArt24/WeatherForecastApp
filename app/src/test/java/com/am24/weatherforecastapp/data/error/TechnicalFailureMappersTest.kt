package com.am24.weatherforecastapp.data.error

import com.am24.weatherforecastapp.domain.error.ApiErrorReason
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.LocationErrorReason
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TechnicalFailureMappersTest {
    @Test
    fun connectionFailure_mapsToNetworkConnectionFailed() {
        assertEquals(
            DomainError.Network(NetworkErrorReason.ConnectionFailed),
            ConnectException().toWeatherDomainError()
        )
    }

    @Test
    fun timeout_mapsToNetworkTimeout() {
        assertEquals(
            DomainError.Network(NetworkErrorReason.Timeout),
            SocketTimeoutException().toWeatherDomainError()
        )
    }

    @Test fun unauthorizedResponse_preservesSafeStatus() = assertHttp(401, ApiErrorReason.Unauthorized)
    @Test fun forbiddenResponse_mapsToUnauthorized() = assertHttp(403, ApiErrorReason.Unauthorized)
    @Test fun notFoundResponse_mapsToNotFound() = assertHttp(404, ApiErrorReason.NotFound)
    @Test fun rateLimitedResponse_preservesSafeStatus() = assertHttp(429, ApiErrorReason.RateLimited)
    @Test fun serverResponse_preservesSafeStatus() = assertHttp(503, ApiErrorReason.ServerError)
    @Test fun genericHttpFailure_mapsToRequestFailed() = assertHttp(418, ApiErrorReason.RequestFailed)

    @Test
    fun unknownHost_mapsToNetworkConnectionFailed() {
        assertEquals(
            DomainError.Network(NetworkErrorReason.ConnectionFailed),
            UnknownHostException().toWeatherDomainError()
        )
    }

    @Test
    fun genericIoFailure_mapsToNetworkConnectionFailed() {
        assertEquals(
            DomainError.Network(NetworkErrorReason.ConnectionFailed),
            IOException().toWeatherDomainError()
        )
    }

    @Test
    fun existingDomainFailure_preservesMappedError() {
        val error = DomainError.Api(ApiErrorReason.RateLimited, statusCode = 429)

        assertSame(error, DomainFailureException(error).toWeatherDomainError())
        assertSame(error, DomainFailureException(error).toLocationDomainError())
    }

    @Test
    fun malformedResponse_mapsToInvalidResponse() {
        assertEquals(
            DomainError.Api(ApiErrorReason.InvalidResponse),
            object : SerializationException() {}.toWeatherDomainError()
        )
    }

    @Test
    fun unusableDecodedResponse_mapsToInvalidResponse() {
        assertEquals(
            DomainError.Api(ApiErrorReason.InvalidResponse),
            InvalidWeatherResponseException().toWeatherDomainError()
        )
    }

    @Test
    fun permissionFailure_mapsToLocationPermissionDenied() {
        assertEquals(
            DomainError.Location(LocationErrorReason.PermissionDenied),
            SecurityException().toLocationDomainError()
        )
    }

    @Test
    fun locationResolutionFailure_mapsToResolutionFailed() {
        assertEquals(
            DomainError.Location(LocationErrorReason.ResolutionFailed),
            IllegalStateException().toLocationDomainError()
        )
    }

    @Test
    fun unexpectedWeatherFailure_mapsToUnknown() {
        assertEquals(DomainError.Unknown, IllegalStateException().toWeatherDomainError())
    }

    private fun assertHttp(status: Int, reason: ApiErrorReason) {
        val failure = HttpException(Response.error<Any>(status, ByteArray(0).toResponseBody()))
        assertEquals(DomainError.Api(reason, status), failure.toWeatherDomainError())
    }
}
