package com.am24.weatherforecastapp

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.am24.weatherforecastapp.fragments.MainFragment
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class RequestWeatherDataTest {

    private lateinit var fragment: MainFragment
    private lateinit var mockContext: Context
    private val mockQueue: RequestQueue = mockk(relaxed = true)
    private val mockProvider: VolleyProvider = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        mockkStatic(TextUtils::class)
        mockkStatic(Uri::class)

        every { Log.isLoggable(any(), any()) } returns true

        every { TextUtils.isEmpty(any()) } returns false

        every { Uri.parse(any<String>()) } returns mockk {
            every { host } returns "mockedHost"
        }

        mockContext = mockk(relaxed = true)

        every { mockProvider.getQueue(any()) } returns mockQueue

        fragment = spyk(MainFragment(), recordPrivateCalls = true)
        every { fragment.requireContext() } returns mockContext
        every { fragment.context } returns mockContext

        fragment.javaClass.getDeclaredField("volleyProvider").apply {
            isAccessible = true
            set(fragment, mockProvider)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testRequestWeatherData_enqueuesRequest() {
        // WHEN
        fragment.requestWeatherData("Kyiv")

        // THEN
        verify {
            mockProvider.getQueue(mockContext)
            mockQueue.add(any<StringRequest>())
        }
    }
}
