package com.dicoding.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.StoryRepository
import com.dicoding.storyapp.data.ResultState
import com.dicoding.storyapp.data.database.StoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Suppress("UNCHECKED_CAST")
class MainViewModel(
    private val repository: StoryRepository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    val stories: Flow<PagingData<StoryEntity>> =
        repository.getStories().cachedIn(scope)

    fun getNewStory(): LiveData<ResultState<List<StoryEntity>>> {
        return repository.getNewStory()
    }
}
