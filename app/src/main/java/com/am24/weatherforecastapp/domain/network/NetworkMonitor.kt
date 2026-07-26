package com.am24.weatherforecastapp.domain.network

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
