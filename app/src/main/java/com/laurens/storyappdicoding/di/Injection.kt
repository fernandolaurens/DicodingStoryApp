package com.laurens.storyappdicoding.di

import android.content.Context
import com.laurens.storyappdicoding.data.database.CeritaDatabase
import com.laurens.storyappdicoding.data.pref.UserPreference
import com.laurens.storyappdicoding.data.pref.UserRepository
import com.laurens.storyappdicoding.data.pref.dataStore
import com.laurens.storyappdicoding.data.remote.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val database = CeritaDatabase.getDatabase(context)
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(pref, apiService, database)
    }
}