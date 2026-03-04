package com.am24.weatherforecastapp

import com.am24.weatherforecastapp.fragments.MainFragment
import com.am24.weatherforecastapp.utils.TransliterationUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class TransliterationTest {
    @Test
    fun testTransliteration_Kyiv() {
        val fragment = MainFragment()
        val input = "Київ"
        val expected = "Kyiv"
        val actual = TransliterationUtils.transliterate(input)
        assertEquals(expected, actual)
    }
}