package com.am24.imbrel.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.am24.imbrel.data.error.toLocationDomainError
import com.am24.imbrel.data.local.SavedLocationLocalDataSource
import com.am24.imbrel.domain.error.DomainError
import com.am24.imbrel.domain.error.DomainFailureException
import com.am24.imbrel.domain.error.LocationErrorReason
import com.am24.imbrel.domain.model.LocationCoordinates
import com.am24.imbrel.domain.model.SavedLocation
import com.am24.imbrel.domain.model.UserLocation
import com.am24.imbrel.domain.repository.GeocodingRepository
import com.am24.imbrel.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationRepositoryImpl(
    context: Context,
    private val geocodingRepository: GeocodingRepository,
    private val savedLocationLocalDataSource: SavedLocationLocalDataSource,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : LocationRepository {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /**
     * Requires location permission to be granted before invocation.
     */
    override suspend fun getCurrentLocation(): UserLocation {
        val coordinates =
            try {
                getCoordinates()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (
                @Suppress("TooGenericExceptionCaught", "SwallowedException")
                failure: Exception,
            ) {
                // Play Services can deliver multiple failure types through the task; all
                // non-cancellation failures cross this repository as location domain errors.
                throw DomainFailureException(failure.toLocationDomainError())
            }
        val placeName =
            try {
                geocodingRepository.resolvePlaceName(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // Coordinates remain useful when optional reverse geocoding is unavailable.
                null
            }
        return UserLocation(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            placeName = placeName,
        )
    }

    override suspend fun getLastSavedLocation(): UserLocation? = savedLocationLocalDataSource.getLocation()?.toUserLocation()

    override suspend fun saveLastLocation(location: UserLocation) {
        savedLocationLocalDataSource.saveLocation(
            SavedLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                placeName = location.placeName,
                savedAtMillis = currentTimeMillis(),
            ),
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCoordinates(): LocationCoordinates =
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }

            try {
                locationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cancellationTokenSource.token,
                    ).addOnSuccessListener { location ->
                        if (!continuation.isActive) return@addOnSuccessListener
                        if (location == null) {
                            continuation.resumeWithException(
                                DomainFailureException(
                                    DomainError.Location(LocationErrorReason.Unavailable),
                                ),
                            )
                        } else {
                            continuation.resume(
                                LocationCoordinates(location.latitude, location.longitude),
                            )
                        }
                    }.addOnFailureListener { error ->
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
            } catch (error: SecurityException) {
                if (continuation.isActive) continuation.resumeWithException(error)
            }
        }
}
