package com.example.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import androidx.room.withTransaction
import com.example.storyapp.data.local.room.StoryDatabase
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
    private val userPreference: UserPreference,
    private val storyDatabase: StoryDatabase
) {

    fun getSession(): Flow<UserModel> = userPreference.getSession()

    suspend fun logout() {
        storyDatabase.withTransaction {
            storyDatabase.storyDao().deleteAll()
            storyDatabase.remoteKeysDao().deleteRemoteKeys()
        }
        userPreference.logout()
    }

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

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<ListStoryItem>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
        pagingSourceFactory = { storyDatabase.storyDao().getAllStories() }
    ).liveData

    suspend fun getLatestStories(): ResultState<List<ListStoryItem>> =
        safeApiCall { apiService.getStories(size = WIDGET_SIZE).listStory }

    suspend fun getStoriesWithLocation(): ResultState<List<ListStoryItem>> =
        safeApiCall { apiService.getStoriesWithLocation().listStory }

    suspend fun uploadStory(
        photo: File,
        description: String,
        lat: Double?,
        lon: Double?
    ): ResultState<String?> = safeApiCall {
        val textMediaType = "text/plain".toMediaType()
        val photoPart = MultipartBody.Part.createFormData(
            "photo",
            photo.name,
            photo.asRequestBody("image/jpeg".toMediaType())
        )
        apiService.uploadStory(
            photo = photoPart,
            description = description.toRequestBody(textMediaType),
            lat = lat?.toString()?.toRequestBody(textMediaType),
            lon = lon?.toString()?.toRequestBody(textMediaType)
        ).message
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
        private const val PAGE_SIZE = 10
        private const val WIDGET_SIZE = 10

        @Volatile
        private var INSTANCE: StoryRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference,
            storyDatabase: StoryDatabase
        ): StoryRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: StoryRepository(apiService, userPreference, storyDatabase).also { INSTANCE = it }
        }
    }
}
