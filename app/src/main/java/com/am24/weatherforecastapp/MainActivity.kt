package com.am24.weatherforecastapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.am24.weatherforecastapp.ui.WeatherApp
import com.am24.weatherforecastapp.ui.theme.WeatherForecastAppTheme
import com.google.android.gms.location.LocationServices

/**
 * Головна точка входу в додаток.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val fLocalProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val context = LocalContext.current
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    checkLocation(viewModel, fLocalProviderClient)
                } else {
                    Toast.makeText(context, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(Unit) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            WeatherForecastAppTheme {
                WeatherApp(viewModel, fLocalProviderClient)
            }
        }
    }

    private fun checkLocation(viewModel: MainViewModel, fLocalProviderClient: com.google.android.gms.location.FusedLocationProviderClient) {
        if (isLocationEnabled()) {
            viewModel.getLocation(fLocalProviderClient)
        } else {
            DialogManager.locationSettingsDialog(this, object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}