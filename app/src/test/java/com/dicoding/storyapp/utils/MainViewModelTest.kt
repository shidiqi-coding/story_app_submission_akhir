package com.dicoding.storyapp.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import com.dicoding.storyapp.StoryRepository
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.utils.getOrAwaitPagingData
import com.dicoding.storyapp.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner


@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MainViewModelStoriesTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when getStories called should return correct data`() = runTest {
        val dummyStories = generateDummyStories(10)
        val pagingData = PagingData.from(dummyStories)

        Mockito.`when`(storyRepository.getStories()).thenReturn(flowOf(pagingData))

        val actual = storyRepository.getStories().getOrAwaitPagingData()

        assertEquals(dummyStories.size, actual.size)
        assertEquals(dummyStories.first(), actual.first())
    }


    @Test
    fun `stories should emit empty when repository returns empty PagingData`() = testScope.runTest {


    }

    private fun generateDummyStories(count: Int): List<StoryEntity> {
        val list = mutableListOf<StoryEntity>()
        for (i in 1..count) {
            list.add(
                StoryEntity(
                    id = "story-$i",
                    name = "User $i",
                    description = "Description $i",
                    photoUrl = "https://example.com/photo$i.jpg",
                    createdAt = "2023-12-0${(i % 9) + 1}T00:00:00Z",
                    lat = (-6.2 + i * 0.001),
                    lon = (106.8 + i * 0.001)
                )
            )
        }
        return list
    }

    private object NoopListCallback : androidx.recyclerview.widget.ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    private object StoryDiffCallback : DiffUtil.ItemCallback<StoryEntity>() {
        override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean =
            oldItem == newItem
    }
}
