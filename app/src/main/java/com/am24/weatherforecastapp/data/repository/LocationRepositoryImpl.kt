package com.am24.weatherforecastapp.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.am24.weatherforecastapp.domain.model.UserLocation
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.am24.weatherforecastapp.domain.model.LocationCoordinates
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepositoryImpl(context: Context) : LocationRepository {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val geocoder = Geocoder(context.applicationContext, Locale.getDefault())

    /**
     * Requires location permission to be granted before invocation.
     */
    override suspend fun getCurrentLocation(): UserLocation {
        val coordinates = getCoordinates()

        val placeName = runCatching {
            getPlaceName(
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

    private fun Address.resolvePlaceName(): String? {
        return locality
            ?: subLocality
            ?: subAdminArea
            ?: adminArea
    }

    private suspend fun getPlaceName(
        latitude: Double,
        longitude: Double
    ): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPlaceNameAsync(latitude, longitude)
        } else {
            getPlaceNameLegacy(latitude, longitude)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getPlaceNameAsync(
        latitude: Double,
        longitude: Double
    ): String? = suspendCancellableCoroutine { continuation ->

        geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) { addresses ->
            if (!continuation.isActive) {
                return@getFromLocation
            }

            val placeName = addresses
                .firstOrNull()
                ?.resolvePlaceName()

            continuation.resume(placeName)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getPlaceNameLegacy(
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        geocoder.getFromLocation(
            latitude,
            longitude,
            1
        )
            ?.firstOrNull()
            ?.resolvePlaceName()
    }
}
