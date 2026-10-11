package com.example.storyapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.di.Injection
import com.example.storyapp.ui.add.AddStoryViewModel
import com.example.storyapp.ui.login.LoginViewModel
import com.example.storyapp.ui.main.MainViewModel
import com.example.storyapp.ui.register.RegisterViewModel

class ViewModelFactory private constructor(
    private val repository: StoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repository)
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository)
        modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(repository)
        modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> AddStoryViewModel(repository)
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelFactory(Injection.provideRepository(context)).also { INSTANCE = it }
            }
    }
}
