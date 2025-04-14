package com.am24.weatherforecastapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.am24.weatherforecastapp.MainViewModel
import com.am24.weatherforecastapp.R
import com.am24.weatherforecastapp.adapters.WeatherAdapter
import com.am24.weatherforecastapp.adapters.WeatherModel
import com.am24.weatherforecastapp.databinding.FragmentDaysBinding

class DaysFragment : Fragment(), WeatherAdapter.Listener {

    private lateinit var binding: FragmentDaysBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV()
        model.dataList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun initRV() = with(binding) {
        rvDays.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter(this@DaysFragment)
        rvDays.adapter = adapter
    }

    companion object {
        fun newInstance() = DaysFragment()
    }

    override fun onClick(item: WeatherModel) {
        model.dataCurrent.value = item
    }
}