
package com.dicoding.storyapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.dicoding.storyapp.StoryRepository
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.utils.DataStoryDummy
import com.dicoding.storyapp.view.main.MainViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import androidx.lifecycle.Observer
import com.dicoding.storyapp.data.ResultState
import org.mockito.Mockito


@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var mainViewModel: MainViewModel
    private val dummyStory = DataStoryDummy.generateDummyStoryEntity()

    @Before
    fun setUp() {
        mainViewModel = MainViewModel(storyRepository)
    }

    @Test
    fun `when get NewStory should Not Null and Return Success`() {
        val observer = Observer<ResultState<List<StoryEntity>>> {}
        try {
            val expectedStory = MutableLiveData<ResultState<List<StoryEntity>>>()
            expectedStory.value = ResultState.Success(dummyStory)
            `when`(storyRepository.getNewStory()).thenReturn(expectedStory)

            val actualStory = mainViewModel.getNewStory()

            Mockito.verify(storyRepository).getNewStory()
            Assert.assertNotNull(actualStory)
        } finally {
            mainViewModel.getNewStory().removeObserver(observer)
        }
    }
}



