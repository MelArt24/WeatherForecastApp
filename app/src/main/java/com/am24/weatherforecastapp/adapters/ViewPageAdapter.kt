package com.am24.weatherforecastapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Клас-адаптер, який відповідає за відображення списку фрагментів у ViewPager2.
 * Він успадковується від FragmentStateAdapter, що ідеально підходить для гортання сторінок.
 * @param fragmentActivity посилання на головну Activity (потрібно для керування життєвим циклом фрагментів).
 * @param list список фрагментів, які ми хочемо показати.
 */
class ViewPageAdapter(fragmentActivity: FragmentActivity, private val list: List<Fragment>)
    : FragmentStateAdapter(fragmentActivity) {

    /**
     * Повідомляє системі, скільки всього елементів (фрагментів) є у нашому списку.
     */
    override fun getItemCount(): Int {
        return list.size
    }


    /**
     * Цей метод викликається, коли користувач переходить на нову сторінку.
     * Він бере фрагмент із нашого списку за відповідним індексом (position)
     * і віддає його для відображення на екрані.
     */
    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}