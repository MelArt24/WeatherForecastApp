package com.am24.weatherforecastapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.am24.weatherforecastapp.DialogManager
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.R
import com.am24.weatherforecastapp.VolleyProvider
import com.am24.weatherforecastapp.adapters.ViewPageAdapter
import com.am24.weatherforecastapp.adapters.WeatherModel
import com.am24.weatherforecastapp.databinding.FragmentMainBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.Locale
import com.am24.weatherforecastapp.BuildConfig.WEATHER_API_KEY
import com.am24.weatherforecastapp.utils.TransliterationUtils


class MainFragment(
    private val volleyProvider: VolleyProvider = VolleyProvider()
) : Fragment() {

    private lateinit var fLocalProviderClient: FusedLocationProviderClient

    private val fragmentList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )

    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getLocation()
        } else {
            Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }


    private lateinit var tabList: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ServiceCast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.areNotificationsEnabled()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
                }
                notificationPermissionLauncher.launch(intent)
            }
        }

        tabList = listOf(
            getString(R.string.hours),
            getString(R.string.days)
        )
        init()
        updateCard()
        getLocation()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding) {
        val adapter = ViewPageAdapter(activity as FragmentActivity, fragmentList)
        viewPage.adapter = adapter

        TabLayoutMediator(tabLayout, viewPage){ tab, position ->
            tab.text = tabList[position]
        }.attach()

        fLocalProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        ibSync.setOnClickListener {
            getLocation()
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }

        ibSearch.setOnClickListener {
            DialogManager.citySearchDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    if (name != null) {
                        requestWeatherData(name)
                    }
                }
            })

        }
    }

    private fun checkLocation() {
        if(isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun isPermissionGranted(p: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            p
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        val cancellationToken = CancellationTokenSource()

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
            !isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return
        }

        try {
            fLocalProviderClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
                .addOnCompleteListener { task ->
                    val location = task.result
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        requestWeatherData("$lat, $lon")
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            // This catches cases where permissions might have been revoked just before this call
            // or if getCurrentLocation requires a finer permission than what was granted.
            // You can log the exception here for debugging if needed.
            Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCard() = with(binding) {
        model.dataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemperature = "${it.maximumTemperature}°C / ${it.minimumTemperature}°C"
            tvDate.text = it.time
            tvCurrentTemperature.text = it.currentTemperature.ifEmpty { maxMinTemperature }
            tvCity.text = it.city
            tvCondition.text = it.condition
            tvMaxMinTemperature.text = if(it.currentTemperature.isEmpty()) "" else maxMinTemperature
            Picasso.get().load("https:" + it.imageURL).into(ivWeather)
        }
    }

    private fun checkPermission() {
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun requestWeatherData(city: String, isTransliterated: Boolean = false) {
        val isUkrainian = Locale.getDefault().language == "uk"
        val langParam = if (isUkrainian) "&lang=uk" else ""

        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                WEATHER_API_KEY +
                "&q=" +
                city +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no" +
                langParam

        val queue = volleyProvider.getQueue(requireContext())

        val request = object : StringRequest(
            Method.GET, url,
            { result ->
                parseWeatherData(result)
            },
            { error ->
                if (!isTransliterated && error.networkResponse?.statusCode == 400) {
                    val transliteratedCity = TransliterationUtils.transliterate(city)
                    requestWeatherData(transliteratedCity, true)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.city_not_found), Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse): Response<String> {
                return try {
                    val utf8String = String(response.data, Charsets.UTF_8)
                    Response.success(utf8String, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: Exception) {
                    Response.error(com.android.volley.ParseError(e))
                }
            }
        }
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
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.dataList.value = list
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition")
                .getString("text"),
            mainObject.getJSONObject("current").getString("temp_c").toFloat().toInt().toString()  + "°C",
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