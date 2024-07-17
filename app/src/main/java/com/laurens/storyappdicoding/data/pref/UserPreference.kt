package com.laurens.storyappdicoding.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    fun observeUserToken(): Flow<String?> = dataStore.data.map { it[SECRET_TOKEN_KEY] }


    suspend fun saveSession(token: String) {
        dataStore.edit { preferences ->
            preferences[SECRET_TOKEN_KEY] = token
            preferences[IS_USER_LOGGED_IN_KEY] = true
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                preferences[USER_EMAIL_KEY] ?: "",
                preferences[SECRET_TOKEN_KEY] ?: "",
                preferences[IS_USER_LOGGED_IN_KEY] ?: false
            )
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null
        private val USER_EMAIL_KEY = stringPreferencesKey("email")
        private val SECRET_TOKEN_KEY = stringPreferencesKey("secretToken")
        private val IS_USER_LOGGED_IN_KEY = booleanPreferencesKey("isUserLoggedIn")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}