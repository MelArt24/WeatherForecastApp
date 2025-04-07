package com.am24.weatherforecastapp

data class DayItem(
    val city: String,
    val date: String,
    val condition: String,
    val image: String,
    val currentTemperature: String,
    val minimumTemperature: String,
    val maximumTemperature: String,
    val hours: String
)
