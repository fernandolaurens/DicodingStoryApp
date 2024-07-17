package com.laurens.storyappdicoding.view.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laurens.storyappdicoding.data.pref.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository): ViewModel() {

    fun signup(name: String, email: String, password: String) = repository.signup(name, email, password)
    fun login(email: String, password: String) = repository.login(email, password)

    fun saveSession(token: String) {
        viewModelScope.launch {
            repository.saveSession(token)
        }
    }
}