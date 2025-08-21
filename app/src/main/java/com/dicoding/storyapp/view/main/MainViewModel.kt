package com.dicoding.storyapp.view.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.StoryRepository
import com.dicoding.storyapp.data.database.StoryEntity
import kotlinx.coroutines.flow.Flow

class MainViewModel(private val repository: StoryRepository) : ViewModel() {


    val stories: Flow<PagingData<StoryEntity>> =
        repository.getStories().cachedIn(viewModelScope)
}
