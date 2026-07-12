package com.am24.weatherforecastapp.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.am24.weatherforecastapp.domain.model.LocationCoordinates
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationRepositoryImpl(context: Context) : LocationRepository {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Requires location permission to be granted before invocation.
     */
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationCoordinates =
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
