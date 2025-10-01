package com.example.wordapp.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_NAME = "USER_NAME"
        private const val KEY_HIGH_SCORE = "HIGH_SCORE"
        private const val KEY_GAMES_PLAYED = "GAMES_PLAYED"
    }
    
    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()
    
    val isFirstLaunch: Boolean
        get() = userName == null
    
    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()
    
    var gamesPlayed: Int
        get() = prefs.getInt(KEY_GAMES_PLAYED, 0)
        set(value) = prefs.edit().putInt(KEY_GAMES_PLAYED, value).apply()
    
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}