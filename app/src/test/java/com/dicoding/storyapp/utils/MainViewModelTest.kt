package com.dicoding.storyapp.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.dicoding.storyapp.StoryRepository
import com.dicoding.storyapp.data.ResultState
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.utils.DataStoryDummy
import com.dicoding.storyapp.utils.getOrAwaitValue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var mainViewModel: MainViewModel

    private val dummyStory = DataStoryDummy.generateDummyStoryEntity()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mainViewModel = MainViewModel(storyRepository)
    }

    @Test
    fun `when getNewStory should Not Null and Return Success`() {

        val expectedStory = MutableLiveData<ResultState<List<StoryEntity>>>()
        expectedStory.value = ResultState.Success(dummyStory)
        Mockito.`when`(storyRepository.getNewStory()).thenReturn(expectedStory)


        val actualStory = mainViewModel.getNewStory().getOrAwaitValue()


        Mockito.verify(storyRepository).getNewStory()
        Assert.assertNotNull(actualStory)
        Assert.assertTrue(actualStory is ResultState.Success)
        Assert.assertEquals(dummyStory.size , (actualStory as ResultState.Success).data.size)
    }

    @Test
    fun `when getNewStory Error should Return Error`() {

        val expectedError = MutableLiveData<ResultState<List<StoryEntity>>>()
        expectedError.value = ResultState.Error("Error occurred")
        Mockito.`when`(storyRepository.getNewStory()).thenReturn(expectedError)


        val actualStory = mainViewModel.getNewStory().getOrAwaitValue()


        Mockito.verify(storyRepository).getNewStory()
        Assert.assertNotNull(actualStory)
        Assert.assertTrue(actualStory is ResultState.Error)
        Assert.assertEquals("Error occurred" , (actualStory as ResultState.Error).error)
    }
}
