package com.dicoding.storyapp.view.map

import android.util.Log
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

    fun loadStoriesWithLocation() {
        viewModelScope.launch {
            try {
                val stories = repository.getStoriesWithLocation()
                _storiesWithLocation.value = stories
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error: ${e.message}")
            }
        }
    }
}
