package com.laurens.storyappdicoding.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.laurens.storyappdicoding.data.pref.ListStoryItem
import com.laurens.storyappdicoding.data.pref.UserModel
import com.laurens.storyappdicoding.data.pref.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun getCerita(token: String): LiveData<PagingData<ListStoryItem>> {
        return repository.getCerita(token)
    }}