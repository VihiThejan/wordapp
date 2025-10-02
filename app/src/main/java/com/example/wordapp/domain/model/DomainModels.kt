package com.example.wordapp.domain.model

/**
 * Domain-level data models that represent business logic entities
 */

data class GameConfig(
    val maxAttempts: Int = 10,
    val initialScore: Int = 100,
    val scoreDeductionPerGuess: Int = 10,
    val letterCheckCost: Int = 5,
    val wordLengthCost: Int = 5,
    val hintAvailableThreshold: Int = 5
)

data class PlayerStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalScore: Int = 0,
    val averageScore: Double = 0.0,
    val bestTime: Long = 0L,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
) {
    val winRate: Double
        get() = if (gamesPlayed > 0) (gamesWon.toDouble() / gamesPlayed) * 100 else 0.0
        
    fun getFormattedBestTime(): String {
        if (bestTime == 0L) return "--:--"
        val minutes = bestTime / 60000
        val seconds = (bestTime % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
}

data class GameResult(
    val isWin: Boolean,
    val finalScore: Int,
    val completionTime: Long,
    val level: Int,
    val attemptsUsed: Int,
    val hintsUsed: Int,
    val letterChecksUsed: Int,
    val secretWord: String
)

sealed class GameError : Exception() {
    object NetworkError : GameError()
    object ApiLimitExceeded : GameError()
    object InvalidInput : GameError()
    object InsufficientPoints : GameError()
    object GameNotInProgress : GameError()
    data class UnknownError(override val message: String) : GameError()
}

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val error: GameError) : Resource<T>()
    data class Loading<T>(val message: String = "Loading...") : Resource<T>()
}