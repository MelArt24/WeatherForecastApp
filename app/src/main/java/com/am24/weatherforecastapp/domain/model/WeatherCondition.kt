package com.am24.weatherforecastapp.domain.model

enum class WeatherCondition {
    Clear,
    MostlyClear,
    PartlyCloudy,
    Cloudy,
    Overcast,
    Fog,
    Drizzle,
    Rain,
    HeavyRain,
    Snow,
    Sleet,
    FreezingRain,
    Hail,
    Thunderstorm,
    Unknown
}

fun weatherConditionFromIcon(iconCode: Int): WeatherCondition = when (iconCode) {
    2, 26 -> WeatherCondition.Clear
    3, 27 -> WeatherCondition.MostlyClear
    4, 28 -> WeatherCondition.PartlyCloudy
    5, 6, 29, 30 -> WeatherCondition.Cloudy
    7, 8, 31 -> WeatherCondition.Overcast
    9 -> WeatherCondition.Fog
    10 -> WeatherCondition.Drizzle
    11, 12, 13, 32 -> WeatherCondition.Rain
    14, 15, 33 -> WeatherCondition.Thunderstorm
    16, 18 -> WeatherCondition.Snow
    17, 19, 34 -> WeatherCondition.Snow
    20, 21, 22, 35 -> WeatherCondition.Sleet
    23, 24, 36 -> WeatherCondition.FreezingRain
    25 -> WeatherCondition.Hail
    else -> WeatherCondition.Unknown
}
