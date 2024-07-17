package com.laurens.storyappdicoding.data.pref

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.*
import com.laurens.storyappdicoding.data.CeritaRemoteMediator
import com.laurens.storyappdicoding.data.database.CeritaDatabase
import com.laurens.storyappdicoding.data.remote.response.LoginResponse
import com.laurens.storyappdicoding.data.remote.response.SignupResponse
import com.laurens.storyappdicoding.data.remote.retrofit.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val storyDatabase: CeritaDatabase

) {

    suspend fun saveSession(token: String) {
        userPreference.saveSession(token)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun observeUserToken(): LiveData<String?> {
        return userPreference.observeUserToken().asLiveData()
    }

    private val signupResultLiveData = MediatorLiveData<Result<SignupResponse>>()
    private val loginResultLiveData = MediatorLiveData<Result<LoginResponse>>()
    private val storyResultLiveData = MediatorLiveData<Result<CeritaResponse>>()
    private val uploadResultLiveData = MediatorLiveData<Result<UploadResponse>>()


    fun signup(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<SignupResponse>> {
        signupResultLiveData.value = Result.Loading
        val client = apiService.signup(name, email, password)
        client.enqueue(object : Callback<SignupResponse> {
            override fun onResponse(
                call: Call<SignupResponse>,
                response: Response<SignupResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        signupResultLiveData.value = Result.Success(res)
                    }
                } else {
                    signupResultLiveData.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                signupResultLiveData.value = Result.Error("${t.message}")
            }
        })
        return signupResultLiveData
    }

    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> {
        loginResultLiveData.value = Result.Loading
        val client = apiService.login(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>

            ) {
                val res = response.body()
                CoroutineScope(Dispatchers.Main).launch {
                    if (response.isSuccessful) {
                        if (res != null) {
                            saveSession(res.loginResult.token)
                            loginResultLiveData.value = Result.Success(res)
                        }
                    } else {
                        loginResultLiveData.value = Result.Error("${res?.message}")
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginResultLiveData.value = Result.Error("${t.message}")
            }
        })
        return loginResultLiveData
    }
    fun fetchStoriesWithLocation(token: String): LiveData<Result<CeritaResponse>> {
        storyResultLiveData.value = Result.Loading
        userPreference.getSession()
        val authToken = "Bearer $token"
        Log.d("repo loca", "fetchStoriesWithLocation: $authToken")
        val client = apiService.fetchStoriesWithLocation(authToken)
        client.enqueue(object : Callback<CeritaResponse> {
            override fun onResponse(
                call: Call<CeritaResponse>,
                response: Response<CeritaResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        storyResultLiveData.value = Result.Success(res)
                    }
                } else {
                    storyResultLiveData.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<CeritaResponse>, t: Throwable) {
                storyResultLiveData.value = Result.Error("${t.message}")
            }
        })
        return storyResultLiveData
    }

    fun getCerita(token: String): LiveData<PagingData<ListStoryItem>> {
        val authToken = "Bearer $token"
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = CeritaRemoteMediator(storyDatabase, apiService, authToken),
            pagingSourceFactory = {
                storyDatabase.getStoryDao().getAllStory()
            }
        ).liveData
    }



    fun uploadCerita(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody
    ): LiveData<Result<UploadResponse>> {
        uploadResultLiveData.value = Result.Loading
        val authToken = "Bearer $token"
        val client = apiService.uploadStory(authToken, image, description)
        client.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        uploadResultLiveData.value = Result.Success(res)
                    }
                } else {
                    uploadResultLiveData.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                uploadResultLiveData.value = Result.Error("${t.message}")
            }
        })
        return uploadResultLiveData
    }

    fun uploadStoryWithLocation(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ): LiveData<Result<UploadResponse>> {
        uploadResultLiveData.value = Result.Loading
        val authToken = "Bearer $token"
        val client = apiService.uploadStoryWithLocation(authToken, image, description, lat, lon)
        client.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                val res = response.body()
                if (response.isSuccessful) {
                    if (res != null) {
                        uploadResultLiveData.value = Result.Success(res)
                    }
                } else {
                    uploadResultLiveData.value = Result.Error("${res?.message}")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                uploadResultLiveData.value = Result.Error("${t.message}")
            }
        })
        return uploadResultLiveData
    }


    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            storyDatabase: CeritaDatabase
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService, storyDatabase)
            }.also { instance = it }
    }
}