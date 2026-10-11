package com.example.storyapp.ui.add

import android.location.Location
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.ResultState
import com.example.storyapp.data.StoryRepository
import kotlinx.coroutines.launch
import java.io.File

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri

    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    private val _uploadResult = MutableLiveData<ResultState<String?>>()
    val uploadResult: LiveData<ResultState<String?>> = _uploadResult

    private var pendingCameraUri: Uri? = null

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun prepareCameraUri(uri: Uri) {
        pendingCameraUri = uri
    }

    fun onCameraResult(isSuccess: Boolean) {
        if (isSuccess) pendingCameraUri?.let { _imageUri.value = it }
        pendingCameraUri = null
    }

    fun setLocation(location: Location?) {
        _location.value = location
    }

    fun uploadStory(photo: File, description: String) {
        val currentLocation = _location.value
        _uploadResult.value = ResultState.Loading
        viewModelScope.launch {
            _uploadResult.value = repository.uploadStory(
                photo = photo,
                description = description,
                lat = currentLocation?.latitude,
                lon = currentLocation?.longitude
            )
        }
    }
}
