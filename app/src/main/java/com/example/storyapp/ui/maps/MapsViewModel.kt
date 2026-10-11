package com.example.storyapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.ListStoryItem
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _stories = MutableLiveData<ResultState<List<ListStoryItem>>>()
    val stories: LiveData<ResultState<List<ListStoryItem>>> = _stories

    init {
        getStoriesWithLocation()
    }

    fun getStoriesWithLocation() {
        _stories.value = ResultState.Loading
        viewModelScope.launch {
            _stories.value = repository.getStoriesWithLocation()
        }
    }
}
