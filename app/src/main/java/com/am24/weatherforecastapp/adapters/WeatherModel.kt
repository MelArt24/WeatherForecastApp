package com.am24.weatherforecastapp.adapters

/**
 * Модель даних, яка описує стан погоди.
 * Використовується для передачі інформації від API (інтернету) до екрана користувача.
 */
data class WeatherModel(
    val city: String,
    val time: String,
    val condition: String,
    val currentTemperature: String,
    val minimumTemperature: String,
    val maximumTemperature: String,
    val imageURL: String,
    val hours: String
)