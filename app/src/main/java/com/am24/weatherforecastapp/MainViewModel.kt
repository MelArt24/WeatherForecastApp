package com.am24.weatherforecastapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val dataCurrent = MutableLiveData<String>()
}