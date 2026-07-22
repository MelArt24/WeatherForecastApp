package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.data.network.NetworkMonitor
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
import com.am24.weatherforecastapp.domain.model.UserLocation
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.am24.weatherforecastapp.domain.usecase.GetCurrentLocationUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCurrentLocationUseCaseTest {
    private val current = UserLocation(50.45000123, 30.52345678, "Kyiv")
    private val saved = UserLocation(49.839683, 24.029717, "Lviv")

    @Test
    fun currentSuccess_isReturnedAndPersisted() = runTest {
        val repository = FakeLocationRepository(current = current)

        val result = GetCurrentLocationUseCase(repository)()

        assertSame(current, result)
        assertEquals(current, repository.savedByUseCase)
    }

    @Test
    fun offline_usesSavedLocationWithoutRequestingCurrentLocation() = runTest {
        val repository = FakeLocationRepository(current = current, lastSaved = saved)

        val result = GetCurrentLocationUseCase(repository, NetworkMonitor { false })()

        assertEquals(saved, result)
        assertEquals(0, repository.currentReadCount)
    }

    @Test
    fun offlineWithoutSavedLocation_failsWithoutRequestingCurrentLocation() = runTest {
        val repository = FakeLocationRepository(current = current)

        val failure = captureFailure {
            GetCurrentLocationUseCase(repository, NetworkMonitor { false })()
        }

        assertEquals(
            DomainError.Network(NetworkErrorReason.Offline),
            (failure as DomainFailureException).error
        )
    }

    @Test
    fun currentFailure_returnsSavedLocationIncludingNameAndCoordinates() = runTest {
        val repository = FakeLocationRepository(
            currentFailure = IllegalStateException("unavailable"),
            lastSaved = saved
        )

        assertEquals(saved, GetCurrentLocationUseCase(repository)())
    }

    @Test
    fun securityFailure_returnsSavedLocation() = runTest {
        val repository = FakeLocationRepository(
            currentFailure = SecurityException("denied"),
            lastSaved = saved
        )

        assertEquals(saved, GetCurrentLocationUseCase(repository)())
    }

    @Test
    fun securityFailure_withoutSavedLocation_preservesOriginalFailure() = runTest {
        val failure = SecurityException("denied")
        val thrown = captureFailure {
            GetCurrentLocationUseCase(FakeLocationRepository(currentFailure = failure))()
        }

        assertSame(failure, thrown)
    }

    @Test
    fun genericFailure_withoutSavedLocation_preservesOriginalFailure() = runTest {
        val failure = IllegalStateException("unavailable")
        val thrown = captureFailure {
            GetCurrentLocationUseCase(FakeLocationRepository(currentFailure = failure))()
        }

        assertSame(failure, thrown)
    }

    @Test
    fun saveFailure_doesNotHideCurrentLocation() = runTest {
        val repository = FakeLocationRepository(
            current = current,
            saveFailure = IllegalStateException("disk")
        )

        assertSame(current, GetCurrentLocationUseCase(repository)())
    }

    @Test
    fun currentProviderCancellation_isPropagatedWithoutFallback() = runTest {
        val cancellation = CancellationException("cancel provider")
        val repository = FakeLocationRepository(
            currentFailure = cancellation,
            lastSaved = saved
        )

        assertSame(cancellation, captureFailure { GetCurrentLocationUseCase(repository)() })
        assertTrue(repository.savedReadCount == 0)
    }

    @Test
    fun savedLocationReadCancellation_isPropagated() = runTest {
        val cancellation = CancellationException("cancel read")
        val repository = FakeLocationRepository(
            currentFailure = IllegalStateException("unavailable"),
            savedReadFailure = cancellation
        )

        assertSame(cancellation, captureFailure { GetCurrentLocationUseCase(repository)() })
    }

    @Test
    fun saveCancellation_isPropagated() = runTest {
        val cancellation = CancellationException("cancel save")
        val repository = FakeLocationRepository(current = current, saveFailure = cancellation)

        assertSame(cancellation, captureFailure { GetCurrentLocationUseCase(repository)() })
    }

    private suspend fun captureFailure(block: suspend () -> Unit): Throwable? = try {
        block()
        null
    } catch (failure: Throwable) {
        failure
    }

    private class FakeLocationRepository(
        private val current: UserLocation? = null,
        private val currentFailure: Exception? = null,
        private val lastSaved: UserLocation? = null,
        private val savedReadFailure: Exception? = null,
        private val saveFailure: Exception? = null
    ) : LocationRepository {
        var savedByUseCase: UserLocation? = null
        var savedReadCount = 0
        var currentReadCount = 0

        override suspend fun getCurrentLocation(): UserLocation {
            currentReadCount++
            currentFailure?.let { throw it }
            return requireNotNull(current)
        }

        override suspend fun getLastSavedLocation(): UserLocation? {
            savedReadCount++
            savedReadFailure?.let { throw it }
            return lastSaved
        }

        override suspend fun saveLastLocation(location: UserLocation) {
            saveFailure?.let { throw it }
            savedByUseCase = location
        }
    }
}
