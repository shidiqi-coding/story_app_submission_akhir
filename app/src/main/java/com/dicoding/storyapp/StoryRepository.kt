package com.dicoding.storyapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.data.ResultState
import com.dicoding.storyapp.data.StoryRemoteMediator
import com.dicoding.storyapp.data.database.StoryDatabase
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.response.Story
import com.dicoding.storyapp.data.response.StoryResponse
import com.dicoding.storyapp.data.response.UploadResponse
import com.dicoding.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class StoryRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
    private val storyDatabase: StoryDatabase,
    private val context: Context
) {

    fun login(email: String, password: String) = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.login(email, password)
            val error = response.error ?: true
            val loginResult = response.loginResult

            if (!error && loginResult != null) {
                emit(ResultState.Success(loginResult))
            } else {
                emit(ResultState.Error(response.message ?: context.getString(R.string.login_failed_title)))
            }
        } catch (e: HttpException) {
            val errorMsg = e.response()?.errorBody()?.string()
            emit(ResultState.Error(errorMsg ?: context.getString(R.string.error_network)))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.error_network)))
        }
    }

    fun register(name: String, email: String, password: String) = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.register(name, email, password)
            if (response.error == false) {
                val successMessage = String.format(
                    context.getString(R.string.register_success_message),
                    email
                )
                emit(ResultState.Success(Pair(name, successMessage)))
            } else {
                emit(ResultState.Error(response.message ?: context.getString(R.string.register_failed)))
            }
        } catch (e: HttpException) {
            val errorMsg = try {
                val errorBody = e.response()?.errorBody()?.string()
                JSONObject(errorBody ?: "").getString("message")
            } catch (_: Exception) {
                context.getString(R.string.error_network)
            }
            emit(ResultState.Error(errorMsg))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.error_network)))
        }
    }

    fun uploadStory(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ): LiveData<ResultState<UploadResponse>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.uploadStory("Bearer $token", image, description, lat, lon)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(ResultState.Success(body))
            } else {
                emit(ResultState.Error(response.message()))
            }
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string() ?: "Unknown error"
            emit(ResultState.Error(errorMessage))
        } catch (_: IOException) {
            emit(ResultState.Error(context.getString(R.string.error_network)))
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): Flow<PagingData<StoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(
                storyDatabase,
                apiService,
                "Bearer ${runBlocking { userPreference.getSession().first().token }}"
            ),
            pagingSourceFactory = { storyDatabase.storyDao().getAllStories() }
        ).flow
    }




    @SuppressLint("StringFormatInvalid")
    suspend fun getStoryDetail(storyId: String): Story {
        val user = userPreference.getSession().first()
        val token = "Bearer ${user.token}"

        return try {
            val response = apiService.getStoriesDetail(token, storyId)
            if (response.error == false && response.story != null) {
                response.story
            } else {
                throw Exception(
                    context.getString(R.string.error_detail_connection, response.message)
                )
            }
        } catch (e: HttpException) {
            throw Exception("HTTP Error: ${e.code()} ${e.message()}")
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_message, e.message))
        }
    }

    suspend fun getStoriesWithLocation(token: String): StoryResponse {
        return apiService.getStoriesWithLocation(token)
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): LiveData<UserModel> {
        return userPreference.getSession().asLiveData()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: StoryRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference,
            storyDatabase: StoryDatabase,
            context: Context
        ): StoryRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = StoryRepository(apiService, userPreference, storyDatabase, context)
                INSTANCE = instance
                instance
            }
        }
    }
}
