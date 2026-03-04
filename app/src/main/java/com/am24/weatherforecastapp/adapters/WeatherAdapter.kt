package com.am24.weatherforecastapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.am24.weatherforecastapp.R
import com.am24.weatherforecastapp.databinding.ListItemBinding
import com.squareup.picasso.Picasso

/**
 * Адаптер для відображення списку погоди (години або дні) у RecyclerView.
 * Використовує ListAdapter для ефективного оновлення даних.
 */
class WeatherAdapter(private val listener: Listener?) : ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {

    /**
     * Holder — "контейнер" для одного елемента списку.
     * Він зберігає посилання на всі View (текст, картинки), щоб не шукати їх щоразу.
     */
    class Holder(view: View, private val listener: Listener?) : RecyclerView.ViewHolder(view) {

        // Binding дозволяє звертатися до елементів дизайну (id) без findViewById
        private val binding = ListItemBinding.bind(view)
        private var itemTemperature: WeatherModel? = null

        // Налаштовуємо клік на весь елемент списку
        init {
            itemView.setOnClickListener {
                // Якщо дані є, передаємо їх у інтерфейс слухача
                itemTemperature?.let { it1 -> listener?.onClick(it1) }
            }
        }

        /**
         * Метод, який наповнює UI реальними даними про погоду
         */
        fun bind(item: WeatherModel) = with(binding) {
            itemTemperature = item
            tvDate.text = item.time           // Встановлюємо час/дату
            tvCondition.text = item.condition // Стан погоди (ясно, дощ тощо)

            // Якщо поточної температури немає (це прогноз на день), показуємо макс/мін
            tvTemperature.text = item.currentTemperature.ifEmpty {
                "${item.maximumTemperature}°C / ${item.minimumTemperature}°C"
            }

            // Завантажуємо іконку погоди з інтернету за допомогою Picasso
            Picasso.get().load("https:" + item.imageURL).into(iv)
        }
    }

    /**
     * Компаратор порівнює старий список із новим.
     * Він допомагає адаптеру зрозуміти, які саме елементи змінилися.
     */
    class Comparator: DiffUtil.ItemCallback<WeatherModel>(){

        // Чи це той самий об'єкт?
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        // Чи змінився вміст всередині об'єкта?
        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Створює новий Holder (пусту комірку), коли RecyclerView це потрібно
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view, listener)
    }

    /**
     * Зв'язує створений Holder із конкретними даними зі списку за позицією
     */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Інтерфейс для обробки натискань на елемент списку.
     * Його ми реалізовуємо у фрагменті або активності.
     */
    interface Listener {
        fun onClick(item: WeatherModel)
    }
}