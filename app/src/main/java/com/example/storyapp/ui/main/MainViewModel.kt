package com.example.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.remote.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    val session: LiveData<UserModel> = repository.getSession().asLiveData()

    private val _stories = MutableLiveData<ResultState<List<ListStoryItem>>>()
    val stories: LiveData<ResultState<List<ListStoryItem>>> = _stories

    fun getStories() {
        _stories.value = ResultState.Loading
        viewModelScope.launch {
            _stories.value = repository.getStories()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
