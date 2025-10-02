package com.example.wordapp.data.model

data class GameState(
    val secretWord: String = "",
    val currentScore: Int = 100,
    val attemptsRemaining: Int = 10,
    val guessHistory: List<String> = emptyList(),
    val level: Int = 1,
    val hintsUsed: Int = 0,
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val timeStarted: Long = 0L,
    val letterChecksUsed: Int = 0,
    val wordLengthRevealed: Boolean = false
) {
    val canUseHint: Boolean
        get() = (10 - attemptsRemaining) >= 5 && hintsUsed == 0
        
    val canCheckLetter: Boolean
        get() = currentScore >= 5
        
    val canGetWordLength: Boolean
        get() = currentScore >= 5 && !wordLengthRevealed
        
    fun getRequiredWordLength(): Int {
        return when (level) {
            1 -> (4..5).random()
            2 -> (6..7).random()
            3 -> (8..9).random()
            else -> (10..12).random()
        }
    }
}

enum class GameStatus {
    LOADING,
    IN_PROGRESS,
    WON,
    LOST,
    ERROR
}

data class WordApiResponse(
    val word: String
)

data class GuessResult(
    val isCorrect: Boolean,
    val newGameState: GameState,
    val message: String
)