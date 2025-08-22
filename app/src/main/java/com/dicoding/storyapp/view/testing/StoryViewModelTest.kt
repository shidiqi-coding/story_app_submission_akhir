package com.dicoding.storyapp.view.testing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.dicoding.storyapp.data.ResultState
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.view.main.MainViewModel
import com.dicoding.storyapp.StoryRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Suppress("UNCHECKED_CAST")
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {

        mainViewModel = MainViewModel(storyRepository)
    }

    private fun <T> LiveData<T>.getOrAwaitValue(): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = Observer<T> {
            data = it
            latch.countDown()
        }
        this.observeForever(observer)

        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw TimeoutException("LiveData value was never set.")
            }
        } finally {
            this.removeObserver(observer)
        }
        return data as T
    }

    @Test
    fun whenGetNewStoryShouldNotNullAndReturnSuccess() {

        val dummyStory = listOf(
            StoryEntity(
                id = "1",
                name = "Dicoding",
                description = "Belajar Unit Test",
                photoUrl = "https://dicoding.com/story.jpg",
                createdAt = "2024-01-01T00:00:00Z",
                lat = null,
                lon = null
            )
        )

        val expectedStory = MutableLiveData<ResultState<List<StoryEntity>>>()
        expectedStory.value = ResultState.Success(dummyStory)

        Mockito.`when`(storyRepository.getNewStory()).thenReturn(expectedStory)

        val actualStory = mainViewModel.getNewStory().getOrAwaitValue()

        Mockito.verify(storyRepository).getNewStory()
        assertNotNull(actualStory)
        assertTrue(actualStory is ResultState.Success)
        assertEquals(dummyStory.size, (actualStory as ResultState.Success).data.size)
    }

    @Test
    fun whenNetworkErrorShouldReturnError() {
        val errorLiveData = MutableLiveData<ResultState<List<StoryEntity>>>()
        errorLiveData.value = ResultState.Error("Error")

        Mockito.`when`(storyRepository.getNewStory()).thenReturn(errorLiveData)

        val actualStory = mainViewModel.getNewStory().getOrAwaitValue()

        Mockito.verify(storyRepository).getNewStory()
        assertNotNull(actualStory)
        assertTrue(actualStory is ResultState.Error)
    }

    class TimeoutException(message: String) : Exception(message)
}
