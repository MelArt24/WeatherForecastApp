package com.am24.weatherforecastapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.am24.weatherforecastapp.adapters.WeatherModel

/**
 * ViewModel — це "спільна пам'ять" для всіх фрагментів додатка.
 * Вона виживає при повороті екрана та зміні конфігурації.
 */
class MainViewModel : ViewModel() {

    /**
     * LiveData, що зберігає один об'єкт погоди (поточний обраний день або година).
     * На ці дані підписаний MainFragment (для картки) та HoursFragment (для списку годин).
     */
    val dataCurrent = MutableLiveData<WeatherModel>()

    /**
     * LiveData, що зберігає СПИСОК об'єктів погоди (прогноз на кілька днів).
     * На ці дані підписаний DaysFragment.
     */
    val dataList = MutableLiveData<List<WeatherModel>>()
}