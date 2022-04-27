package com.example.dicodingstoryapp.data.preference

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dicodingstoryapp.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {
    private val Context.userPreferenceDataStore: DataStore<Preferences> by preferencesDataStore(
        name = DATA_STORE_NAME
    )

    fun getUser(): Flow<UserModel> {
        return context.userPreferenceDataStore.data.map { preferences ->
            UserModel(
                preferences[NAME_KEY] ?: "",
                preferences[EMAIL_KEY] ?: "",
                preferences[PASSWORD_KEY] ?: "",
                preferences[STATE_KEY] ?: false,
                preferences[ID_KEY] ?: "",
                preferences[TOKEN_KEY] ?: ""
            )
        }
    }

    suspend fun setUser(user: UserModel) {
        context.userPreferenceDataStore.edit { preferences ->
            preferences[NAME_KEY] = user.name
            preferences[EMAIL_KEY] = user.email
            preferences[PASSWORD_KEY] = user.password
            preferences[STATE_KEY] = user.isLogin
            preferences[ID_KEY] = user.id
            preferences[TOKEN_KEY] = user.token
        }
    }

    suspend fun logout() {
        context.userPreferenceDataStore.edit { preferences ->
            preferences[NAME_KEY] = ""
            preferences[EMAIL_KEY] = ""
            preferences[PASSWORD_KEY] = ""
            preferences[STATE_KEY] = false
            preferences[ID_KEY] = ""
            preferences[TOKEN_KEY] = ""
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: UserPreferences? = null

        private val NAME_KEY = stringPreferencesKey("name")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val STATE_KEY = booleanPreferencesKey("state")
        private val ID_KEY = stringPreferencesKey("id")
        private val TOKEN_KEY = stringPreferencesKey("token")

        fun getInstance(context: Context): UserPreferences =
            INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: UserPreferences(context).also { INSTANCE = it }
                instance
            }

        const val DATA_STORE_NAME = "SETTINGS_DATASTORE"
    }
}