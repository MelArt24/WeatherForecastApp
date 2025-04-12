package com.am24.weatherforecastapp.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.WEATHER_API_KEY
import com.am24.weatherforecastapp.adapters.ViewPageAdapter
import com.am24.weatherforecastapp.adapters.WeatherModel
import com.am24.weatherforecastapp.databinding.FragmentMainBinding
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject



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
    private val model: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }


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
        updateCard()
        requestWeatherData("Kyiv")
    }

    private fun init() = with(binding) {
        val adapter = ViewPageAdapter(activity as FragmentActivity, fragmentList)
        viewPage.adapter = adapter
        TabLayoutMediator(tabLayout, viewPage){
            tab, position -> tab.text = tabList[position]
        }.attach()
    }

    private fun updateCard() = with(binding) {
        model.dataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemperature = "${it.maximumTemperature}°C/${it.minimumTemperature}°C"
            tvDate.text = it.time
            tvCurrentTemperature.text = it.currentTemperature
            tvCity.text = it.city
            tvCondition.text = it.condition
            tvMaxMinTemperature.text = maxMinTemperature
            Picasso.get().load("https:" + it.imageURL).into(ivWeather)
        }
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
                city +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET, url,
            {
                result -> parseWeatherData(result)
            },
            {
                error -> Log.d("MyLog", "Error: $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

        val name = mainObject.getJSONObject("location").getString("name")

        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maximumTemperature,
            weatherItem.minimumTemperature,
            mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("icon"),
            weatherItem.hours
        )
        model.dataCurrent.value = item
    }

    companion object {
        fun newInstance() = MainFragment()
    }

}