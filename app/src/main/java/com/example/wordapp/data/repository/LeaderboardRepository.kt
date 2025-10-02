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
            
            // Return mock data for now since Dreamlo API is having issues
            val mockEntries = if (_leaderboard.value.isEmpty()) {
                listOf(
                    LeaderboardEntry("Alice", 1500, 180000, 3, "2023-10-02", 1),
                    LeaderboardEntry("Bob", 1200, 210000, 2, "2023-10-02", 2),
                    LeaderboardEntry("Charlie", 980, 240000, 2, "2023-10-02", 3),
                    LeaderboardEntry("Diana", 750, 180000, 1, "2023-10-01", 4),
                    LeaderboardEntry("Eve", 600, 300000, 1, "2023-10-01", 5)
                )
            } else {
                _leaderboard.value
            }
            
            _leaderboard.value = mockEntries
            Result.success(mockEntries)
            
            // TODO: Uncomment when Dreamlo API is working
            /*
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
            */
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
            // Create a local mock entry for now since Dreamlo API is having issues
            val mockEntry = LeaderboardEntry(
                name = playerName,
                score = score,
                time = completionTimeMs,
                level = level,
                date = "2023-10-03",
                rank = 0 // Will be recalculated when sorted
            )
            
            // Add to local storage for now
            val currentEntries = _leaderboard.value.toMutableList()
            currentEntries.add(mockEntry)
            
            // Sort by score descending and update ranks
            val sortedEntries = currentEntries.sortedByDescending { it.score }.take(10)
            val rankedEntries = sortedEntries.mapIndexed { index, entry ->
                entry.copy(rank = index + 1)
            }
            
            _leaderboard.value = rankedEntries
            
            Result.success("Score submitted successfully! (Local storage)")
            
            // TODO: Uncomment when Dreamlo API is working
            /*
            val response = dreamloApiService.addLeaderboardEntry(
                privateKey = BuildConfig.DREAMLO_PRIVATE_KEY,
                name = playerName,
                score = score
            )
            
            if (response.isSuccessful) {
                fetchLeaderboard()
                Result.success("Score submitted successfully!")
            } else {
                Result.failure(Exception("Failed to submit score: ${response.code()}"))
            }
            */
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
            // Clear local data for now
            _leaderboard.value = emptyList()
            Result.success("Leaderboard cleared successfully! (Local storage)")
            
            // TODO: Uncomment when Dreamlo API is working
            /*
            val response = dreamloApiService.clearLeaderboard(BuildConfig.DREAMLO_PRIVATE_KEY)
            
            if (response.isSuccessful) {
                _leaderboard.value = emptyList()
                Result.success("Leaderboard cleared successfully!")
            } else {
                Result.failure(Exception("Failed to clear leaderboard: ${response.code()}"))
            }
            */
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}