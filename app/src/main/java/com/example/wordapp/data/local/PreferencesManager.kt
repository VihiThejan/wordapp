package com.example.wordapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.wordapp.domain.model.PlayerStats
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
        private const val KEY_GAMES_WON = "GAMES_WON"
        private const val KEY_TOTAL_SCORE = "TOTAL_SCORE"
        private const val KEY_BEST_TIME = "BEST_TIME"
        private const val KEY_CURRENT_STREAK = "CURRENT_STREAK"
        private const val KEY_BEST_STREAK = "BEST_STREAK"
        private const val KEY_CURRENT_LEVEL = "CURRENT_LEVEL"
        private const val KEY_SOUND_ENABLED = "SOUND_ENABLED"
        private const val KEY_VIBRATION_ENABLED = "VIBRATION_ENABLED"
    }
    
    // Basic user data
    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()
    
    val isFirstLaunch: Boolean
        get() = userName == null
    
    // Game statistics
    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) {
            if (value > highScore) {
                prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()
            }
        }
    
    var gamesPlayed: Int
        get() = prefs.getInt(KEY_GAMES_PLAYED, 0)
        set(value) = prefs.edit().putInt(KEY_GAMES_PLAYED, value).apply()
    
    var gamesWon: Int
        get() = prefs.getInt(KEY_GAMES_WON, 0)
        set(value) = prefs.edit().putInt(KEY_GAMES_WON, value).apply()
    
    var totalScore: Int
        get() = prefs.getInt(KEY_TOTAL_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_SCORE, value).apply()
    
    var bestTime: Long
        get() = prefs.getLong(KEY_BEST_TIME, 0L)
        set(value) {
            if (bestTime == 0L || (value > 0 && value < bestTime)) {
                prefs.edit().putLong(KEY_BEST_TIME, value).apply()
            }
        }
    
    var currentStreak: Int
        get() = prefs.getInt(KEY_CURRENT_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_CURRENT_STREAK, value).apply()
    
    var bestStreak: Int
        get() = prefs.getInt(KEY_BEST_STREAK, 0)
        set(value) {
            if (value > bestStreak) {
                prefs.edit().putInt(KEY_BEST_STREAK, value).apply()
            }
        }
    
    var currentLevel: Int
        get() = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_LEVEL, value).apply()
    
    // Settings
    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
    
    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()
    
    // Helper methods
    fun getPlayerStats(): PlayerStats {
        return PlayerStats(
            gamesPlayed = gamesPlayed,
            gamesWon = gamesWon,
            totalScore = totalScore,
            averageScore = if (gamesPlayed > 0) totalScore.toDouble() / gamesPlayed else 0.0,
            bestTime = bestTime,
            currentStreak = currentStreak,
            bestStreak = bestStreak
        )
    }
    
    fun recordGameResult(won: Boolean, score: Int, completionTime: Long) {
        gamesPlayed++
        totalScore += score
        
        if (won) {
            gamesWon++
            currentStreak++
            bestStreak = maxOf(bestStreak, currentStreak)
            
            if (completionTime > 0) {
                bestTime = completionTime
            }
        } else {
            currentStreak = 0
        }
        
        highScore = score
    }
    
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
    
    fun clearStatistics() {
        prefs.edit()
            .remove(KEY_HIGH_SCORE)
            .remove(KEY_GAMES_PLAYED)
            .remove(KEY_GAMES_WON)
            .remove(KEY_TOTAL_SCORE)
            .remove(KEY_BEST_TIME)
            .remove(KEY_CURRENT_STREAK)
            .remove(KEY_BEST_STREAK)
            .apply()
    }
}