package com.am24.weatherforecastapp.fragments

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.isPermissionGranted(param: String) : Boolean {
    return ContextCompat.checkSelfPermission(activity as AppCompatActivity,
        param) == PackageManager.PERMISSION_GRANTED
}