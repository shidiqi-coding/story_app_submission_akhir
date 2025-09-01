package com.dicoding.storyapp.utils

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.view.main.ListStoryAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Untuk mengambil nilai LiveData di Unit Test
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)

    val observer = object : Observer<T> {
        override fun onChanged(o: T) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    try {
        afterObserve.invoke()


        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }
    } finally {
        this.removeObserver(observer)
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}


@VisibleForTesting(otherwise = VisibleForTesting.NONE)
suspend fun <T> LiveData<T>.observeForTesting(block: suspend () -> Unit) {
    val observer = Observer<T> { }
    try {
        observeForever(observer)
        block()
    } finally {
        removeObserver(observer)
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T : Any> Flow<PagingData<T>>.getOrAwaitPagingData(): List<T> {
    val differ = AsyncPagingDataDiffer(
        diffCallback = defaultDiffCallback(),
        updateCallback = NoopListCallback,
        mainDispatcher = Dispatchers.Main,
        workerDispatcher = Dispatchers.IO
    )

    val job = CoroutineScope(Dispatchers.Main).launch {
        this@getOrAwaitPagingData.collectLatest { pagingData ->
            differ.submitData(pagingData)
        }
    }


    advanceUntilIdle()
    job.cancel()

    return differ.snapshot().items
}


@Suppress("UNCHECKED_CAST")
private fun <T : Any> defaultDiffCallback(): DiffUtil.ItemCallback<T> {
    return ListStoryAdapter.StoryDiffCallback as DiffUtil.ItemCallback<T>
}


object NoopListCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
