package com.am24.weatherforecastapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.adapters.WeatherAdapter
import com.am24.weatherforecastapp.adapters.WeatherModel
import com.am24.weatherforecastapp.databinding.FragmentHoursBinding
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * Фрагмент для відображення погоди по годинах.
 */
class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter

    // Отримуємо доступ до тієї ж ViewModel, що й в Activity
    private val model: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV() // Налаштовуємо список

        /**
         * Спостерігаємо за переглядом конкретного дня.
         * Коли користувач обирає день у DaysFragment, дані тут оновлюються,
         * і ми перемальовуємо години саме для цього дня.
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.dataCurrent.collect { weather ->
                    weather?.let {
                        adapter.submitList(getHoursList(it))
                    }
                }
            }
        }
    }

    /**
     * Ініціалізація списку годин.
     */
    private fun initRV() = with(binding) {
        rvHours.layoutManager = LinearLayoutManager(activity)

        // Передаємо null, бо нам не потрібна реакція на клік по годині
        adapter = WeatherAdapter(null)
        rvHours.adapter = adapter
    }

    /**
     * Функція, що бере об'єкт дня, дістає з нього рядок "hours" (JSON формат),
     * розбирає його на окремі години та створює список об'єктів WeatherModel.
     */
    private fun getHoursList(weatherItem: WeatherModel): List<WeatherModel> {
        val hoursArr = JSONArray(weatherItem.hours)
        val list = ArrayList<WeatherModel>()

        for(i in 0 until hoursArr.length()) {
            val hourObj = hoursArr.getJSONObject(i)
            val item = WeatherModel(
                weatherItem.city,
                hourObj.getString("date").split("T").first().substring(0, 10)
                        + " " + hourObj.getString("date").split("T").last().substring(0, 5),
                hourObj.getString("summary"),
                hourObj.getDouble("temperature").toInt().toString() + "°C",
                "",
                "",
                hourObj.getString("icon"),
                ""
            )
            list.add(item)
        }
        return list
    }

    companion object {
        /**
         * Статичний метод для створення нового екземпляра фрагмента.
         */
        fun newInstance() = HoursFragment()
    }
}