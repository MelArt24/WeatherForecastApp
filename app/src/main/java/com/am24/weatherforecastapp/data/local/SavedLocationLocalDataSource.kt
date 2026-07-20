package com.am24.weatherforecastapp.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.am24.weatherforecastapp.domain.model.SavedLocation
import kotlinx.coroutines.flow.first

interface SavedLocationLocalDataSource {
    suspend fun getLocation(): SavedLocation?
    suspend fun saveLocation(location: SavedLocation)
}

class DataStoreSavedLocationLocalDataSource(
    private val dataStore: DataStore<Preferences>
) : SavedLocationLocalDataSource {
    override suspend fun getLocation(): SavedLocation? {
        val preferences = dataStore.data.first()

        val latitude = preferences[LATITUDE] ?: return null

        val longitude = preferences[LONGITUDE] ?: return null

        val savedAtMillis = preferences[SAVED_AT_MILLIS] ?: return null

        if (!latitude.isFinite() || !longitude.isFinite()) return null

        return SavedLocation(
            latitude = latitude,
            longitude = longitude,
            placeName = preferences[PLACE_NAME],
            savedAtMillis = savedAtMillis
        )
    }

    override suspend fun saveLocation(location: SavedLocation) {
        dataStore.edit { preferences ->
            val previousPlaceName = preferences[PLACE_NAME]
            val sameCoordinates = preferences[LATITUDE] == location.latitude &&
                preferences[LONGITUDE] == location.longitude
            val placeName = location.placeName
                ?: previousPlaceName?.takeIf { sameCoordinates }

            preferences[LATITUDE] = location.latitude
            preferences[LONGITUDE] = location.longitude
            preferences[SAVED_AT_MILLIS] = location.savedAtMillis
            if (placeName == null) preferences.remove(PLACE_NAME)
            else preferences[PLACE_NAME] = placeName
        }
    }

    private companion object {
        val LATITUDE = doublePreferencesKey("last_current_location_latitude")
        val LONGITUDE = doublePreferencesKey("last_current_location_longitude")
        val PLACE_NAME = stringPreferencesKey("last_current_location_place_name")
        val SAVED_AT_MILLIS = longPreferencesKey("last_current_location_saved_at_millis")
    }
}
