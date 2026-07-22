package com.am24.weatherforecastapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.DomainFailureException

fun interface NetworkMonitor {
    fun isOnline(): Boolean
}

fun NetworkMonitor.isOnlineOrDomainFailure(): Boolean = try {
    isOnline()
} catch (_: Exception) {
    throw DomainFailureException(DomainError.Unknown)
}

class AndroidNetworkMonitor(context: Context) : NetworkMonitor {
    private val connectivityManager = context.applicationContext.getSystemService(
        ConnectivityManager::class.java
    )

    override fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
