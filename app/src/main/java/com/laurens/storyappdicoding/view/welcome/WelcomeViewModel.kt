package com.laurens.storyappdicoding.view.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.laurens.storyappdicoding.data.pref.UserModel
import com.laurens.storyappdicoding.data.pref.UserRepository

class WelcomeViewModel(private val repository: UserRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}