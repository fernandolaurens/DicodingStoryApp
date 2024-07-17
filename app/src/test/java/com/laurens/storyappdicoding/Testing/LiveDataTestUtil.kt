package com.laurens.storyappdicoding.Testing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getValueBlocking(
    duration: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObservation: () -> Unit = {}
): T {
    var result: T? = null
    val countDownLatch = CountDownLatch(1)
    val liveDataObserver = object : Observer<T> {
        override fun onChanged(value: T) {
            result = value
            countDownLatch.countDown()
            this@getValueBlocking.removeObserver(this)
        }
    }
    this.observeForever(liveDataObserver)

    try {
        afterObservation.invoke()
        if (!countDownLatch.await(duration, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

    } finally {
        this.removeObserver(liveDataObserver)
    }

    @Suppress("UNCHECKED_CAST")
    return result as T
}