package com.am24.weatherforecastapp.data.repository

import android.annotation.SuppressLint
import com.am24.weatherforecastapp.domain.model.UserLocation
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import com.am24.weatherforecastapp.domain.model.LocationCoordinates

class LocationRepositoryImpl(
    context: android.content.Context,
    private val geocodingRepository: GeocodingRepository
) : LocationRepository {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /**
     * Requires location permission to be granted before invocation.
     */
    override suspend fun getCurrentLocation(): UserLocation {
        val coordinates = getCoordinates()

        val placeName = runCatching {
            geocodingRepository.resolvePlaceName(
                latitude = coordinates.latitude,
                longitude = coordinates.longitude
            )
        }.getOrNull()

        return UserLocation(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            placeName = placeName
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
                        cancellationTokenSource.token
                    )
                    .addOnSuccessListener { location ->
                        if (!continuation.isActive) return@addOnSuccessListener
                        if (location == null) {
                            continuation.resumeWithException(
                                IllegalStateException("Current location is unavailable")
                            )
                        } else {
                            continuation.resume(
                                LocationCoordinates(location.latitude, location.longitude)
                            )
                        }
                    }
                    .addOnFailureListener { error ->
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
            } catch (error: SecurityException) {
                if (continuation.isActive) continuation.resumeWithException(error)
            }
        }

}
