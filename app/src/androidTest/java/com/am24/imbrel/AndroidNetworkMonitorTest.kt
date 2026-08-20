package com.am24.imbrel

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.am24.imbrel.data.network.AndroidNetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class AndroidNetworkMonitorTest {
    private val connectivityManager = mock(ConnectivityManager::class.java)
    private val network = mock(Network::class.java)

    @Test
    fun initialValidatedNetwork_emitsOnline() =
        runBlocking {
            setCurrentNetwork(validatedCapabilities())

            assertEquals(
                true,
                AndroidNetworkMonitor(connectivityManager)
                    .observeConnectivity()
                    .take(1)
                    .toList()
                    .single(),
            )
        }

    @Test
    fun initialMissingNetwork_emitsOffline() =
        runBlocking {
            `when`(connectivityManager.activeNetwork).thenReturn(null)

            assertEquals(
                false,
                AndroidNetworkMonitor(connectivityManager)
                    .observeConnectivity()
                    .take(1)
                    .toList()
                    .single(),
            )
        }

    @Test
    fun validatedNetworkGained_emitsOnline() =
        runBlocking {
            `when`(connectivityManager.activeNetwork).thenReturn(null)
            val (emissions, job, callback) = collectConnectivity(2)

            setCurrentNetwork(validatedCapabilities())
            callback.onAvailable(network)
            job.join()

            assertEquals(listOf(false, true), emissions)
        }

    @Test
    fun networkLost_emitsOffline() =
        runBlocking {
            setCurrentNetwork(validatedCapabilities())
            val (emissions, job, callback) = collectConnectivity(2)

            `when`(connectivityManager.activeNetwork).thenReturn(null)
            callback.onLost(network)
            job.join()

            assertEquals(listOf(true, false), emissions)
        }

    @Test
    fun validationChange_emitsUpdatedConnectivity() =
        runBlocking {
            setCurrentNetwork(unvalidatedCapabilities())
            val (emissions, job, callback) = collectConnectivity(2)

            setCurrentNetwork(validatedCapabilities())
            callback.onCapabilitiesChanged(network, validatedCapabilities())
            job.join()

            assertEquals(listOf(false, true), emissions)
        }

    @Test
    fun duplicateCallbacks_areSuppressed() =
        runBlocking {
            setCurrentNetwork(unvalidatedCapabilities())
            val (emissions, job, callback) = collectConnectivity(2)

            callback.onAvailable(network)
            callback.onCapabilitiesChanged(network, unvalidatedCapabilities())
            setCurrentNetwork(validatedCapabilities())
            callback.onCapabilitiesChanged(network, validatedCapabilities())
            job.join()

            assertEquals(listOf(false, true), emissions)
        }

    @Test
    fun collectorCancellation_unregistersCallback() =
        runBlocking {
            `when`(connectivityManager.activeNetwork).thenReturn(null)
            val monitor = AndroidNetworkMonitor(connectivityManager)
            val job = launch { monitor.observeConnectivity().collect {} }
            yield()
            val callback = capturedCallback()

            job.cancelAndJoin()

            verify(connectivityManager, times(1)).unregisterNetworkCallback(callback)
        }

    private suspend fun collectConnectivity(count: Int): Triple<MutableList<Boolean>, Job, ConnectivityManager.NetworkCallback> {
        val emissions = mutableListOf<Boolean>()
        val monitor = AndroidNetworkMonitor(connectivityManager)
        val job =
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.currentCoroutineContext()).launch {
                monitor.observeConnectivity().take(count).toList(emissions)
            }
        yield()
        return Triple(emissions, job, capturedCallback())
    }

    private fun capturedCallback(): ConnectivityManager.NetworkCallback {
        val captor = ArgumentCaptor.forClass(ConnectivityManager.NetworkCallback::class.java)
        verify(connectivityManager).registerDefaultNetworkCallback(captor.capture())
        return captor.value
    }

    private fun setCurrentNetwork(capabilities: NetworkCapabilities) {
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(capabilities)
    }

    private fun validatedCapabilities(): NetworkCapabilities = capabilities(validated = true)

    private fun unvalidatedCapabilities(): NetworkCapabilities = capabilities(validated = false)

    private fun capabilities(validated: Boolean): NetworkCapabilities =
        mock(NetworkCapabilities::class.java).also { capabilities ->
            `when`(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                .thenReturn(true)
            `when`(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                .thenReturn(validated)
        }
}
