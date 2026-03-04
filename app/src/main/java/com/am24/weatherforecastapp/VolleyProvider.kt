package com.am24.weatherforecastapp

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Клас-постачальник для бібліотеки Volley.
 * Він відповідає за створення черги запитів (RequestQueue),
 * через яку будуть проходити всі звернення до Weather API.
 */
class VolleyProvider {

    /**
     * Створює або повертає чергу запитів.
     * @param context контекст додатка (потрібен Volley для налаштування кешу та мережі).
     * @return RequestQueue — черга, в яку ми будемо "закидати" наші запити на погоду.
     */
    fun getQueue(context: Context): RequestQueue {
        return Volley.newRequestQueue(context)
    }
}
