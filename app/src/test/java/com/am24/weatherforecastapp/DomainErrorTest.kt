package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.domain.error.ApiErrorReason
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.LocationErrorReason
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainErrorTest {
    @Test
    fun errorCategories_shareTheDomainErrorHierarchy() {
        val errors = listOf(
            DomainError.Network(NetworkErrorReason.Offline),
            DomainError.Api(ApiErrorReason.InvalidResponse),
            DomainError.Location(LocationErrorReason.Unavailable),
            DomainError.Unknown
        )

        assertTrue(errors.all { it is DomainError })
    }

    @Test
    fun categorizedErrors_haveValueSemantics() {
        assertEquals(
            DomainError.Network(NetworkErrorReason.Timeout),
            DomainError.Network(NetworkErrorReason.Timeout)
        )
        assertEquals(
            DomainError.Location(LocationErrorReason.PermissionDenied),
            DomainError.Location(LocationErrorReason.PermissionDenied)
        )
        assertSame(DomainError.Unknown, DomainError.Unknown)
    }

    @Test
    fun apiError_canCarryStatusCodeWithoutUserFacingText() {
        val serverError = DomainError.Api(
            reason = ApiErrorReason.ServerError,
            statusCode = 503
        )
        val invalidResponse = DomainError.Api(ApiErrorReason.InvalidResponse)

        assertEquals(ApiErrorReason.ServerError, serverError.reason)
        assertEquals(503, serverError.statusCode)
        assertNull(invalidResponse.statusCode)
    }
}
