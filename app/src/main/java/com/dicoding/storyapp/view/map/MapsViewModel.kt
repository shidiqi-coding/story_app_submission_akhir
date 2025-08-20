package com.dicoding.storyapp.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.StoryRepository
import com.dicoding.storyapp.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val storiesWithLocation: LiveData<List<ListStoryItem>> = _storiesWithLocation

    fun getSession() = repository.getSession()

    fun loadStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getStoriesWithLocation(token)
                _storiesWithLocation.value = response.listStory ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _storiesWithLocation.value = emptyList()
            }
        }
    }
}
