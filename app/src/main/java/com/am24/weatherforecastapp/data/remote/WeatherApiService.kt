package com.am24.weatherforecastapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("api/v1/free/point")
    suspend fun getWeatherData(
        @Query("lat") lat: String? = null,
        @Query("lon") lon: String? = null,
        @Query("place_id") placeId: String? = null,
        @Query("sections") sections: String = "all",
        @Query("timezone") timezone: String = "UTC",
        @Query("language") language: String = "en",
        @Query("units") units: String = "metric",
        @Query("key") apiKey: String
    ): WeatherResponse
}
