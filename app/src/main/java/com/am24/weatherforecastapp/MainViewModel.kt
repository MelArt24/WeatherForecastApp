package com.am24.weatherforecastapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.am24.weatherforecastapp.adapters.WeatherModel

class MainViewModel : ViewModel() {
    val dataCurrent = MutableLiveData<WeatherModel>()
    val dataList = MutableLiveData<List<WeatherModel>>()
}