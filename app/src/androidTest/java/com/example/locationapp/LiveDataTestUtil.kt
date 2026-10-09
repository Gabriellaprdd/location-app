package com.example.locationapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {

    var data: T? = null
    val latch = CountDownLatch(1)

    lateinit var observer: Observer<T>

    observer = Observer { value ->
        data = value
        latch.countDown()
    }

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        observeForever(observer)
    }

    if (!latch.await(time, timeUnit)) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            removeObserver(observer)
        }

        throw TimeoutException("LiveData value was never set.")
    }

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        removeObserver(observer)
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}