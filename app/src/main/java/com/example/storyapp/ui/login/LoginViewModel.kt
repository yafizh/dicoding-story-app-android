package com.example.storyapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<ResultState<String>>()
    val loginResult: LiveData<ResultState<String>> = _loginResult

    fun login(email: String, password: String) {
        _loginResult.value = ResultState.Loading
        viewModelScope.launch {
            wrapEspressoIdlingResource {
                _loginResult.value = repository.login(email, password)
            }
        }
    }
}
