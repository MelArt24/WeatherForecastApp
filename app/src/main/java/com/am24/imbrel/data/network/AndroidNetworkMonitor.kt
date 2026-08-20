package com.am24.imbrel.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.am24.imbrel.domain.network.NetworkMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AndroidNetworkMonitor internal constructor(
    private val connectivityManager: ConnectivityManager,
) : NetworkMonitor {
    constructor(context: Context) : this(
        context.applicationContext.getSystemService(ConnectivityManager::class.java),
    )

    override fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return hasValidatedInternet(capabilities)
    }

    override fun observeConnectivity(): Flow<Boolean> =
        callbackFlow {
            fun emitCurrentConnectivity() {
                trySend(isOnline())
            }

            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) = emitCurrentConnectivity()

                    override fun onLost(network: Network) = emitCurrentConnectivity()

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities,
                    ) {
                        emitCurrentConnectivity()
                    }
                }

            connectivityManager.registerDefaultNetworkCallback(callback)
            emitCurrentConnectivity()

            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }.distinctUntilChanged()

    private fun hasValidatedInternet(capabilities: NetworkCapabilities): Boolean =
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
