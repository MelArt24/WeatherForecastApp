package com.am24.weatherforecastapp

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleyProvider {
    fun getQueue(context: Context): RequestQueue {
        return Volley.newRequestQueue(context)
    }
}
