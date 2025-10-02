package com.example.wordapp.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordapp.data.model.LeaderboardEntry
import com.example.wordapp.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    val leaderboard = leaderboardRepository.leaderboard
    val isLoading = leaderboardRepository.isLoading

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = leaderboardRepository.fetchLeaderboard()
            
            result.onSuccess { entries ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEmpty = entries.isEmpty(),
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load leaderboard"
                )
            }
        }
    }

    fun submitScore(score: Int, completionTimeMs: Long, level: Int) {
        if (!leaderboardRepository.hasPlayerName()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Player name not set. Please complete onboarding first."
            )
            return
        }

        viewModelScope.launch {
            val playerName = leaderboardRepository.getPlayerName()
            
            val result = leaderboardRepository.submitScore(
                playerName = playerName,
                score = score,
                completionTimeMs = completionTimeMs,
                level = level
            )

            result.onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    message = message
                )
                // Reload to show updated rankings
                loadLeaderboard()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to submit score"
                )
            }
        }
    }
    
    // Method for submitting scores with custom names (for testing/samples)
    fun submitCustomScore(playerName: String, score: Int, completionTimeMs: Long, level: Int) {
        viewModelScope.launch {
            val result = leaderboardRepository.submitScore(
                playerName = playerName,
                score = score,
                completionTimeMs = completionTimeMs,
                level = level
            )

            result.onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    message = message
                )
                // Reload to show updated rankings
                loadLeaderboard()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to submit score"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getPlayerStats(): PlayerStats? {
        val playerName = leaderboardRepository.getPlayerName()
        val entries = leaderboard.value
        
        val playerEntry = entries.find { it.name.equals(playerName, ignoreCase = true) }
        
        return playerEntry?.let { entry ->
            PlayerStats(
                name = entry.name,
                rank = entry.rank,
                score = entry.score,
                time = entry.time,
                level = entry.level
            )
        }
    }
}

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null
)

data class PlayerStats(
    val name: String,
    val rank: Int,
    val score: Int,
    val time: Long,
    val level: Int
) {
    fun getFormattedTime(): String {
        val minutes = time / 60000
        val seconds = (time % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
}