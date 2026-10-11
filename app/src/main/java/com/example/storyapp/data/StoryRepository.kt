package com.example.storyapp.data

import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.remote.response.BasicResponse
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.data.remote.retrofit.ApiService
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    fun getSession(): Flow<UserModel> = userPreference.getSession()

    suspend fun logout() = userPreference.logout()

    suspend fun register(name: String, email: String, password: String): ResultState<String?> =
        safeApiCall { apiService.register(name, email, password).message }

    suspend fun login(email: String, password: String): ResultState<String> = safeApiCall {
        val result = apiService.login(email, password).loginResult
        userPreference.saveSession(
            UserModel(
                userId = result.userId,
                name = result.name,
                token = result.token,
                isLogin = true
            )
        )
        result.name
    }

    suspend fun getStories(): ResultState<List<ListStoryItem>> =
        safeApiCall { apiService.getStories().listStory }

    suspend fun uploadStory(photo: File, description: String): ResultState<String?> = safeApiCall {
        val descriptionBody = description.toRequestBody("text/plain".toMediaType())
        val photoBody = photo.asRequestBody("image/jpeg".toMediaType())
        val photoPart = MultipartBody.Part.createFormData("photo", photo.name, photoBody)
        apiService.uploadStory(photoPart, descriptionBody).message
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): ResultState<T> = try {
        ResultState.Success(block())
    } catch (e: HttpException) {
        ResultState.Error(e.errorMessage())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ResultState.Error(null)
    }

    private fun HttpException.errorMessage(): String? = try {
        val errorBody = response()?.errorBody()?.string()
        Gson().fromJson(errorBody, BasicResponse::class.java)?.message
    } catch (e: JsonSyntaxException) {
        null
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryRepository(apiService, userPreference).also { INSTANCE = it }
            }
    }
}
