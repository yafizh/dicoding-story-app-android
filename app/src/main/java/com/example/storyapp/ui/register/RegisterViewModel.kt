package com.example.storyapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.StoryRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<ResultState<String?>>()
    val registerResult: LiveData<ResultState<String?>> = _registerResult

    fun register(name: String, email: String, password: String) {
        _registerResult.value = ResultState.Loading
        viewModelScope.launch {
            _registerResult.value = repository.register(name, email, password)
        }
    }
}
