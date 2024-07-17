package com.laurens.storyappdicoding.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.laurens.storyappdicoding.data.pref.UserModel
import com.laurens.storyappdicoding.data.pref.UserRepository

class MapsViewModel(private val repository: UserRepository): ViewModel() {

    fun getStoriesWithLocation(token: String) = repository.fetchStoriesWithLocation(token)

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}