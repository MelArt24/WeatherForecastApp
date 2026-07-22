package com.am24.weatherforecastapp.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.am24.weatherforecastapp.data.error.toLocationDomainError
import com.am24.weatherforecastapp.domain.model.UserLocation
import com.am24.weatherforecastapp.domain.model.SavedLocation
import com.am24.weatherforecastapp.data.local.SavedLocationLocalDataSource
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import com.am24.weatherforecastapp.domain.error.LocationErrorReason
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellationException
import com.am24.weatherforecastapp.domain.model.LocationCoordinates

class LocationRepositoryImpl(
    context: Context,
    private val geocodingRepository: GeocodingRepository,
    private val savedLocationLocalDataSource: SavedLocationLocalDataSource,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : LocationRepository {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /**
     * Requires location permission to be granted before invocation.
     */
    override suspend fun getCurrentLocation(): UserLocation {
        val coordinates = try {
            getCoordinates()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            throw DomainFailureException(failure.toLocationDomainError())
        }
        val placeName = try {
            geocodingRepository.resolvePlaceName(
                latitude = coordinates.latitude,
                longitude = coordinates.longitude
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
        return UserLocation(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            placeName = placeName
        )
    }

    override suspend fun getLastSavedLocation(): UserLocation? =
        savedLocationLocalDataSource.getLocation()?.toUserLocation()

    override suspend fun saveLastLocation(location: UserLocation) {
        savedLocationLocalDataSource.saveLocation(
            SavedLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                placeName = location.placeName,
                savedAtMillis = currentTimeMillis()
            )
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
                            continuation.resumeWithException(DomainFailureException(
                                DomainError.Location(LocationErrorReason.Unavailable)
                            ))
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
