package com.example.storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    val stories: LiveData<PagingData<ListStoryItem>> = repository.getStories().cachedIn(viewModelScope)

    fun getSession(): LiveData<UserModel> = repository.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            wrapEspressoIdlingResource {
                repository.logout()
            }
        }
    }
}
