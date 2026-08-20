package com.am24.imbrel

import com.am24.imbrel.domain.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(
    initiallyOnline: Boolean = true,
) : NetworkMonitor {
    private val connectivity = MutableStateFlow(initiallyOnline)

    override fun isOnline(): Boolean = connectivity.value

    override fun observeConnectivity(): Flow<Boolean> = connectivity

    fun setOnline(isOnline: Boolean) {
        connectivity.value = isOnline
    }
}
