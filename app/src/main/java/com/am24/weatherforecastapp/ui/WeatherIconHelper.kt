package com.am24.weatherforecastapp.ui

import androidx.annotation.DrawableRes
import com.am24.weatherforecastapp.R

object WeatherIconHelper {
    @DrawableRes
    fun getWeatherIcon(iconCode: String): Int {
        return when (iconCode) {
            "1" -> R.drawable.w1
            "2" -> R.drawable.w2
            "3" -> R.drawable.w3
            "4" -> R.drawable.w4
            "5" -> R.drawable.w5
            "6" -> R.drawable.w6
            "7" -> R.drawable.w7
            "8" -> R.drawable.w8
            "9" -> R.drawable.w9
            "10" -> R.drawable.w10
            "11" -> R.drawable.w11
            "12" -> R.drawable.w12
            "13" -> R.drawable.w13
            "14" -> R.drawable.w14
            "15" -> R.drawable.w15
            "16" -> R.drawable.w16
            "17" -> R.drawable.w17
            "18" -> R.drawable.w18
            "19" -> R.drawable.w19
            "20" -> R.drawable.w20
            "21" -> R.drawable.w21
            "22" -> R.drawable.w22
            "23" -> R.drawable.w23
            "24" -> R.drawable.w24
            "25" -> R.drawable.w25
            "26" -> R.drawable.w26
            "27" -> R.drawable.w27
            "28" -> R.drawable.w28
            "29" -> R.drawable.w29
            "30" -> R.drawable.w30
            "31" -> R.drawable.w31
            "32" -> R.drawable.w32
            "33" -> R.drawable.w33
            "34" -> R.drawable.w34
            "35" -> R.drawable.w35
            "36" -> R.drawable.w36
            else -> R.drawable.w1
        }
    }
}
