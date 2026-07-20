package com.am24.weatherforecastapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.am24.weatherforecastapp.data.local.DataStoreSavedLocationLocalDataSource
import com.am24.weatherforecastapp.domain.model.SavedLocation
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SavedLocationLocalDataSourceTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val file = File(
        ApplicationProvider.getApplicationContext<android.content.Context>().filesDir,
        "datastore/location-${UUID.randomUUID()}.preferences_pb"
    )
    private val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
    private val dataSource = DataStoreSavedLocationLocalDataSource(dataStore)

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
    }

    @Test
    fun location_roundTripsWithoutPrecisionLoss() = runBlocking {
        val location = SavedLocation(
            latitude = 50.450001234567,
            longitude = 30.523456789012,
            placeName = "Kyiv",
            savedAtMillis = 123456789L
        )

        dataSource.saveLocation(location)

        assertEquals(location, dataSource.getLocation())
    }

    @Test
    fun newerLocation_replacesPreviousLocation() = runBlocking {
        dataSource.saveLocation(SavedLocation(1.0, 2.0, "Old", 100L))
        val newer = SavedLocation(3.0, 4.0, "New", 200L)

        dataSource.saveLocation(newer)

        assertEquals(newer, dataSource.getLocation())
    }

    @Test
    fun nullPlaceName_isStoredSafely() = runBlocking {
        val location = SavedLocation(1.0, 2.0, null, 100L)

        dataSource.saveLocation(location)

        assertEquals(location, dataSource.getLocation())
    }

    @Test
    fun incompleteRecord_returnsNull() = runBlocking {
        dataStore.edit { preferences ->
            preferences[doublePreferencesKey("last_current_location_latitude")] = 50.45
        }

        assertNull(dataSource.getLocation())
    }

    @Test
    fun nullName_doesNotOverwriteNameAtSameCoordinates() = runBlocking {
        dataSource.saveLocation(SavedLocation(1.0, 2.0, "Known", 100L))

        dataSource.saveLocation(SavedLocation(1.0, 2.0, null, 200L))

        assertEquals("Known", dataSource.getLocation()?.placeName)
        assertEquals(200L, dataSource.getLocation()?.savedAtMillis)
    }
}
