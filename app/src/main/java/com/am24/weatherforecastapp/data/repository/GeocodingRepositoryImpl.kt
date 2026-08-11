package com.am24.weatherforecastapp.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.am24.weatherforecastapp.data.error.toLocationDomainError
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import com.am24.weatherforecastapp.domain.error.LocationErrorReason
import com.am24.weatherforecastapp.domain.model.GeocodedLocation
import com.am24.weatherforecastapp.domain.repository.GeocodingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class GeocodingRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GeocodingRepository {
    override suspend fun searchLocation(query: String): GeocodedLocation? {
        if (!Geocoder.isPresent()) throw providerUnavailable()
        val address =
            executeGeocoderCall {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    searchAsync(query)
                } else {
                    searchLegacy(query)
                }
            }

        return address?.let {
            GeocodedLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                localizedName = it.resolvePlaceName(),
            )
        }
    }

    override suspend fun resolvePlaceName(
        latitude: Double,
        longitude: Double,
    ): String? {
        if (!Geocoder.isPresent()) throw providerUnavailable()
        val address =
            executeGeocoderCall {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    reverseAsync(latitude, longitude)
                } else {
                    reverseLegacy(latitude, longitude)
                }
            }
        return address?.resolvePlaceName()
    }

    private suspend fun executeGeocoderCall(block: suspend () -> Address?): Address? =
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            failure: Exception,
        ) {
            // Geocoder providers expose inconsistent failures; this is the domain boundary for all
            // non-cancellation provider errors.
            throw DomainFailureException(failure.toLocationDomainError())
        }

    private fun geocoder(): Geocoder =
        Geocoder(
            context.applicationContext,
            context.resources.configuration.locales[0],
        )

    private fun providerUnavailable() =
        DomainFailureException(
            DomainError.Location(LocationErrorReason.Unavailable),
        )

    private fun Address.resolvePlaceName(): String? = locality ?: subLocality ?: subAdminArea ?: adminArea ?: featureName

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun searchAsync(query: String): Address? =
        suspendCancellableCoroutine { continuation ->
            geocoder().getFromLocationName(query, 1) { addresses ->
                if (continuation.isActive) continuation.resume(addresses.firstOrNull())
            }
        }

    @Suppress("DEPRECATION")
    private suspend fun searchLegacy(query: String): Address? =
        withContext(ioDispatcher) {
            geocoder().getFromLocationName(query, 1)?.firstOrNull()
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun reverseAsync(
        latitude: Double,
        longitude: Double,
    ): Address? =
        suspendCancellableCoroutine { continuation ->
            geocoder().getFromLocation(latitude, longitude, 1) { addresses ->
                if (continuation.isActive) continuation.resume(addresses.firstOrNull())
            }
        }

    @Suppress("DEPRECATION")
    private suspend fun reverseLegacy(
        latitude: Double,
        longitude: Double,
    ): Address? =
        withContext(ioDispatcher) {
            geocoder().getFromLocation(latitude, longitude, 1)?.firstOrNull()
        }
}
