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

class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
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
        initRV()
        model.dataCurrent.observe(viewLifecycleOwner) {
            adapter.submitList(getHoursList(it))
        }
    }

    private fun initRV() = with(binding) {
        rvHours.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter()
        rvHours.adapter = adapter
    }

    private fun getHoursList(weatherItem: WeatherModel): List<WeatherModel> {
        val hoursArr = JSONArray(weatherItem.hours)
        val list = ArrayList<WeatherModel>()

        for(i in 0 until hoursArr.length()) {
            val item = WeatherModel(
                weatherItem.city,
                (hoursArr[i] as JSONObject).getString("time"),
                (hoursArr[i] as JSONObject).getJSONObject("condition").getString("text"),
                (hoursArr[i] as JSONObject).getString("temp_c"),
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

        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}