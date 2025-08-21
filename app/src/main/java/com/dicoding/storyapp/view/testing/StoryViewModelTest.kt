package com.dicoding.storyapp.view.testing

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.dicoding.storyapp.data.ResultState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import com.dicoding.storyapp.view.main.MainViewModel
import com.dicoding.storyapp.StoryRepository
import com.dicoding.storyapp.view.testing.DataStoryDummy
import com.dicoding.storyapp.data.database.StoryEntity


@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storyRepository : StoryRepository
    private lateinit var mainViewModel : MainViewModel
    private val dummyStory = DataStoryDummy.generateDummyStoryEntity()

    @Before
    fun setUp(){
        mainViewModel = MainViewModel(storyRepository)
    }

    @Test
    fun `when Get New Story Should Not Null and Return Success`() {
        val expectedStory = MutableLiveData<Result<List<StoryEntity>>>()
        expectedStory.value = Result.Success(dummyStory)
        `when`(storyRepository.getStories()).thenReturn(expectedStory)

        val actualStory = mainViewModel.getNewStory().getOrAwaitValue()

        Mockito.verify(storyRepository).getNewStory()
        Assert.assertNotnull(actualStory)
        Assert.assertTrue(actualStory is ResultState.Success)
        Assert.assertEquals(dummyStory.size,(actualStory as ResultState.Success).data.size)
    }


}