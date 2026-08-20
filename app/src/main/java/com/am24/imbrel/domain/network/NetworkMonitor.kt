package com.am24.imbrel.domain.network

import com.am24.imbrel.domain.error.DomainError
import com.am24.imbrel.domain.error.DomainFailureException
import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    fun isOnline(): Boolean

    fun observeConnectivity(): Flow<Boolean>
}

/** Converts a connectivity probe failure into the domain contract instead of assuming a state. */
fun NetworkMonitor.isOnlineOrDomainFailure(): Boolean =
    try {
        isOnline()
    } catch (_: Exception) {
        throw DomainFailureException(DomainError.Unknown)
    }
