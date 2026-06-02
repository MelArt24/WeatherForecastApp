package com.am24.weatherforecastapp.fragments

import android.Manifest
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
import org.json.JSONObject
import java.util.Locale
import com.am24.weatherforecastapp.BuildConfig.WEATHER_API_KEY
import com.am24.weatherforecastapp.utils.TransliterationUtils

/**
 * Головний екран додатка.
 * Керує геолокацією, запитами до мережі та відображенням основної картки погоди.
 */
class MainFragment : Fragment() {

    // Клієнт для отримання координат користувача
    private lateinit var fLocalProviderClient: FusedLocationProviderClient

    // Список фрагментів для ViewPager2 (години та дні)
    private val fragmentList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )

    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    /**
     * Реєстратор для перевірки дозволу на сповіщення (актуально для Android 13+).
     * Наразі він просто запускає активність налаштувань і не виконує додаткових дій після повернення.
     */
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
    }

    // Реєстратор запиту на дозвіл геолокації
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
        observeErrors()
        getLocation()
    }

    private fun observeErrors() {
        model.errorLiveData.observe(viewLifecycleOwner) { errorResId ->
            errorResId?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
                model.errorLiveData.value = null // Скидаємо помилку
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    /**
     * Ініціалізація ViewPager2, TabLayout та кнопок керування
     */
    private fun init() = with(binding) {
        val adapter = ViewPageAdapter(activity as FragmentActivity, fragmentList)
        viewPage.adapter = adapter

        // Зв'язуємо назви вкладок ("Години", "Дні") з ViewPager
        TabLayoutMediator(tabLayout, viewPage){ tab, position ->
            tab.text = tabList[position]
        }.attach()

        fLocalProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Кнопка синхронізації (оновити за GPS)
        ibSync.setOnClickListener {
            getLocation()
            tabLayout.selectTab(tabLayout.getTabAt(0))  // Повертаємося на вкладку годин
            checkLocation()
        }

        // Кнопка пошуку міста
        ibSearch.setOnClickListener {
            DialogManager.citySearchDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    if (name != null) {
                        model.requestWeatherData(city = name)
                    }
                }
            })
        }
    }

    /**
     * Перевіряє, чи увімкнено GPS у налаштуваннях телефона.
     * Якщо так — отримуємо локацію. Якщо ні — показуємо діалог з пропозицією увімкнути.
     */
    private fun checkLocation() {
        if(isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    // Відкриваємо системне вікно налаштувань локації (GPS)
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    /**
     * Перевіряє безпосередньо в системі, чи працює зараз GPS-провайдер.
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Утилітарний метод, який просто повертає true/false: чи дав користувач дозвіл [p] нашому додатку.
     */
    private fun isPermissionGranted(p: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            p
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Отримує широту та довготу користувача
     */
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
                        val lat = location.latitude.toString()
                        val lon = location.longitude.toString()
                        model.requestWeatherData(lat = lat, lon = lon)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Спостерігає за dataCurrent у ViewModel і оновлює верхню картку на екрані
     */
    private fun updateCard() = with(binding) {
        model.dataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemperature = "${it.maximumTemperature}°C / ${it.minimumTemperature}°C"
            tvDate.text = it.time
            tvCurrentTemperature.text = it.currentTemperature.ifEmpty { maxMinTemperature }
            tvCity.text = it.city
            tvCondition.text = it.condition
            tvMaxMinTemperature.text = if(it.currentTemperature.isEmpty()) "" else maxMinTemperature

            val iconId = resources.getIdentifier(
                "weather_icons/set01/big/${it.imageURL}",
                "drawable",
                requireContext().packageName
            )
            if (iconId != 0) {
                ivWeather.setImageResource(iconId)
            }
        }
    }

    /**
     * Ініціює запит на отримання дозволу на локацію (ACCESS_FINE_LOCATION),
     * якщо він ще не наданий користувачем.
     */
    private fun checkPermission() {
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        /**
         * Статичний метод для створення нового екземпляра фрагмента.
         */
        fun newInstance() = MainFragment()
    }
}