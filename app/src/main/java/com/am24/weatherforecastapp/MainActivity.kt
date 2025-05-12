package com.am24.weatherforecastapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.am24.weatherforecastapp.fragments.MainFragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeHolder, MainFragment.newInstance()).commit()
    }
}