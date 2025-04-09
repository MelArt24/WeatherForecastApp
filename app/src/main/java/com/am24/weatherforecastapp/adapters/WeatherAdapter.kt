package com.am24.weatherforecastapp.adapters

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.am24.weatherforecastapp.databinding.ListItemBinding

class WeatherAdapter : ListAdapter<WeatherModel, WeatherAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)

        fun bind(item: WeatherModel) = with(binding) {
            tvDate.text = item.time
            tvCondition.text = item.condition
            tvTemperature.text = item.currentTemperature
        }
    }

}