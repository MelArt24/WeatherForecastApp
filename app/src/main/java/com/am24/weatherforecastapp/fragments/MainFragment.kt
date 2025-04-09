package com.am24.weatherforecastapp.fragments

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.am24.weatherforecastapp.R
import com.am24.weatherforecastapp.WEATHER_API_KEY
import com.am24.weatherforecastapp.adapters.ViewPageAdapter
import com.am24.weatherforecastapp.databinding.FragmentMainBinding
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment() {

    private val fragmentList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )

    private val tabList = listOf(
        "Hours",
        "Days"
    )

    private lateinit var paramLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
    }

    private fun init() = with(binding) {
        val adapter = ViewPageAdapter(activity as FragmentActivity, fragmentList)
        viewPage.adapter = adapter
        TabLayoutMediator(tabLayout, viewPage){
            tab, position -> tab.text = tabList[position]
        }.attach()
    }

    private fun permissionListener(){
        paramLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        }
    }

    private fun checkPermission() {
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            paramLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                WEATHER_API_KEY +
                "&q=" +
                "London" +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
    }

    companion object {
        fun newInstance() = MainFragment()
    }

}