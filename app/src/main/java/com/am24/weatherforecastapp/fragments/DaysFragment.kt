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
import com.am24.weatherforecastapp.databinding.FragmentDaysBinding

/**
 * Фрагмент, що відображає список прогнозів погоди на кілька днів.
 * Реалізує інтерфейс WeatherAdapter.Listener для обробки натискань на дні.
 */
class DaysFragment : Fragment(), WeatherAdapter.Listener {

    // Клас Binding для прямого доступу до View-елементів (замість findViewById)
    private lateinit var binding: FragmentDaysBinding

    // Адаптер для керування списком у RecyclerView
    private lateinit var adapter: WeatherAdapter

    /**
     * Спільна ViewModel. Використовуємо requireActivity(), щоб Fragment і Activity
     * мали доступ до однієї і тієї ж моделі даних.
     */
    private val model: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    /**
     * Створюємо зовнішній вигляд фрагмента
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Коли View вже створена, ми налаштовуємо список та підписуємося на дані
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV() // Ініціалізація RecyclerView

        /**
         * Спостерігаємо (Observe) за змінами у списку погоди.
         * Як тільки у ViewModel оновляться дані,
         * адаптер автоматично оновить список на екрані.
         */
        model.dataList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    /**
     * Налаштування RecyclerView: задаємо менеджер розташування (список зверху вниз)
     * та підключаємо адаптер.
     */
    private fun initRV() = with(binding) {
        rvDays.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(this@DaysFragment)
        rvDays.adapter = adapter
    }

    companion object {
        /**
         * Статичний метод для створення нового екземпляра фрагмента.
         */
        fun newInstance() = DaysFragment()
    }

    /**
     * Викликається при натисканні на конкретний день у списку.
     * Ми беремо цей об'єкт погоди і відправляємо його у ViewModel,
     * щоб головний екран (MainFragment) зміг показати деталі цього дня.
     */
    override fun onClick(item: WeatherModel) {
        model.dataCurrent.value = item
    }
}