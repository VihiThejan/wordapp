package com.example.wordapp.data.repository

import com.example.wordapp.BuildConfig
import com.example.wordapp.data.local.PreferencesManager
import com.example.wordapp.data.model.LeaderboardEntry
import com.example.wordapp.data.model.toLeaderboardEntry
import com.example.wordapp.data.remote.DreamloApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor(
    private val dreamloApiService: DreamloApiService,
    private val preferencesManager: PreferencesManager
) {
    
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    suspend fun fetchLeaderboard(): Result<List<LeaderboardEntry>> {
        return try {
            _isLoading.value = true
            
            val response = dreamloApiService.getLeaderboard(BuildConfig.DREAMLO_PUBLIC_KEY)
            
            if (response.isSuccessful) {
                val entries = response.body()?.dreamlo?.leaderboard?.entry?.mapIndexed { index, entry ->
                    entry.toLeaderboardEntry(index + 1)
                } ?: emptyList()
                
                _leaderboard.value = entries.sortedByDescending { it.score }
                Result.success(entries)
            } else {
                Result.failure(Exception("Failed to fetch leaderboard: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Return empty list on network error - graceful degradation
            _leaderboard.value = emptyList()
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun submitScore(
        playerName: String,
        score: Int,
        completionTimeMs: Long,
        level: Int
    ): Result<String> {
        return try {
            val completionTimeSeconds = completionTimeMs / 1000
            
            val response = dreamloApiService.addLeaderboardEntry(
                privateKey = BuildConfig.DREAMLO_PRIVATE_KEY,
                name = playerName,
                score = score,
                seconds = completionTimeSeconds,
                level = level
            )
            
            if (response.isSuccessful) {
                // Refresh leaderboard after successful submission
                fetchLeaderboard()
                Result.success("Score submitted successfully!")
            } else {
                Result.failure(Exception("Failed to submit score: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getPlayerName(): String {
        return preferencesManager.userName ?: "Player"
    }
    
    fun hasPlayerName(): Boolean {
        return !preferencesManager.userName.isNullOrBlank()
    }
    
    suspend fun clearLeaderboard(): Result<String> {
        return try {
            val response = dreamloApiService.clearLeaderboard(BuildConfig.DREAMLO_PRIVATE_KEY)
            
            if (response.isSuccessful) {
                _leaderboard.value = emptyList()
                Result.success("Leaderboard cleared successfully!")
            } else {
                Result.failure(Exception("Failed to clear leaderboard: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}