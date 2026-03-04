package com.am24.weatherforecastapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.am24.weatherforecastapp.fragments.MainFragment

/**
 * Головна точка входу в додаток.
 * У даній архітектурі Activity слугує лише контейнером для фрагментів.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * SupportFragmentManager керує відображенням фрагментів.
         * beginTransaction() — починає процес заміни/додавання.
         * replace() — знаходить контейнер R.id.placeHolder і вставляє туди MainFragment.
         * commit() — підтверджує зміни.
         */
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeHolder, MainFragment.newInstance()).commit()
    }
}