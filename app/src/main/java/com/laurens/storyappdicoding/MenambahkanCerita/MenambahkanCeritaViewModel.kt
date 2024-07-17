package com.laurens.storyappdicoding.MenambahkanCerita

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.laurens.storyappdicoding.data.pref.UserRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MenambahkanCeritaViewModel (private val repository: UserRepository) : ViewModel() {
    fun uploadCerita(token: String, image: MultipartBody.Part, description: RequestBody) =
        repository.uploadCerita(token, image, description)

    fun uploadStoryWithLocation(token: String, image: MultipartBody.Part, description: RequestBody, lat: RequestBody, lon: RequestBody) =
        repository.uploadStoryWithLocation(token, image, description, lat, lon)

    fun observeUserToken(): LiveData<String?> {
        return repository.observeUserToken()
    }
}