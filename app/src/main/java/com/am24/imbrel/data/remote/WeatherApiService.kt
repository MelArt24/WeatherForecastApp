package com.am24.imbrel.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeatherData(
        @Query("lat") lat: String? = null,
        @Query("lon") lon: String? = null,
        @Query("place_id") placeId: String? = null,
        @Query("sections") sections: String = "all",
        @Query("timezone") timezone: String,
        @Query("language") language: String = "en",
        @Query("units") units: String = "metric",
    ): WeatherResponseDto
}
