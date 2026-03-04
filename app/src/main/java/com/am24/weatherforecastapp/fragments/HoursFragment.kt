package com.am24.weatherforecastapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.adapters.WeatherAdapter
import com.am24.weatherforecastapp.adapters.WeatherModel
import com.am24.weatherforecastapp.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject

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
        model.dataCurrent.observe(viewLifecycleOwner) {
            // Передаємо в адаптер список, який ми витягли з JSON-рядка
            adapter.submitList(getHoursList(it))
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
            val item = WeatherModel(
                weatherItem.city,
                (hoursArr[i] as JSONObject).getString("time"),
                (hoursArr[i] as JSONObject).getJSONObject("condition").getString("text"),
                (hoursArr[i] as JSONObject).getString("temp_c").toFloat().toInt().toString()  + "°C",
                "",
                "",
                (hoursArr[i] as JSONObject).getJSONObject("condition").getString("icon"),
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