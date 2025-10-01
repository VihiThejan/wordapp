package com.example.wordapp.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordapp.data.local.PreferencesManager
import com.example.wordapp.data.model.GameState
import com.example.wordapp.data.model.GameStatus
import com.example.wordapp.data.repository.WordRepository
import com.example.wordapp.data.repository.LeaderboardRepository
import com.example.wordapp.util.GameTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val preferencesManager: PreferencesManager,
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val gameTimer = GameTimer()
    
    val gameState = wordRepository.gameState
    val timerDisplay = gameTimer.formattedTime

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<GameUiEvent>()
    val uiEvents: SharedFlow<GameUiEvent> = _uiEvents.asSharedFlow()

    // Combined state for UI updates
    val combinedState = combine(gameState, uiState, timerDisplay) { game, ui, timer ->
        CombinedGameState(game, ui, timer)
    }

    init {
        startNewGame()
    }

    fun startNewGame(level: Int? = null) {
        viewModelScope.launch {
            val currentLevel = level ?: gameState.value.level
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            gameTimer.reset()
            
            val result = wordRepository.startNewGame(currentLevel)
            
            result.onSuccess {
                gameTimer.start(viewModelScope)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentGuess = "",
                    message = "Guess the secret word!"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Failed to start game: ${error.message}"
                )
                _uiEvents.emit(GameUiEvent.ShowError("Failed to start new game"))
            }
        }
    }

    fun updateGuess(guess: String) {
        _uiState.value = _uiState.value.copy(currentGuess = guess)
    }

    fun submitGuess() {
        val guess = _uiState.value.currentGuess.trim()
        
        if (guess.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Please enter a guess!")
            return
        }

        if (guess.length < 2) {
            _uiState.value = _uiState.value.copy(message = "Guess must be at least 2 characters!")
            return
        }

        val result = wordRepository.makeGuess(guess)
        
        _uiState.value = _uiState.value.copy(
            currentGuess = "",
            message = result.message
        )

        // Handle game end scenarios
        when (result.newGameState.gameStatus) {
            GameStatus.WON -> {
                val completionTime = gameTimer.stop()
                handleGameWin(result.newGameState, completionTime)
            }
            GameStatus.LOST -> {
                gameTimer.stop()
                viewModelScope.launch {
                    _uiEvents.emit(GameUiEvent.ShowGameOver(result.newGameState.secretWord))
                }
            }
            else -> {
                // Game continues
            }
        }
    }

    private fun handleGameWin(gameState: GameState, completionTime: Long) {
        viewModelScope.launch {
            // Update high score if needed
            val currentHighScore = preferencesManager.highScore
            if (gameState.currentScore > currentHighScore) {
                preferencesManager.highScore = gameState.currentScore
            }
            
            // Increment games played
            preferencesManager.gamesPlayed = preferencesManager.gamesPlayed + 1
            
            // Submit score to leaderboard
            submitScoreToLeaderboard(gameState.currentScore, completionTime, gameState.level)
            
            _uiEvents.emit(
                GameUiEvent.ShowLevelComplete(
                    gameState.currentScore,
                    completionTime,
                    gameState.level
                )
            )
        }
    }

    private fun submitScoreToLeaderboard(score: Int, completionTimeMs: Long, level: Int) {
        viewModelScope.launch {
            try {
                leaderboardRepository.submitScore(
                    playerName = preferencesManager.userName ?: "Player",
                    score = score,
                    completionTimeMs = completionTimeMs,
                    level = level
                )
            } catch (e: Exception) {
                // Silent fail for leaderboard submission - don't interrupt game flow
                // Could add logging here in production
            }
        }
    }

    fun checkLetter(letter: Char) {
        val currentState = gameState.value
        
        // Check if player has enough points
        if (!currentState.canCheckLetter) {
            _uiState.value = _uiState.value.copy(
                message = "Not enough points! Need 5 points to check a letter."
            )
            return
        }
        
        // Check if game is still active
        if (currentState.gameStatus != GameStatus.IN_PROGRESS) {
            _uiState.value = _uiState.value.copy(
                message = "Game is not in progress"
            )
            return
        }

        val result = wordRepository.checkLetterOccurrence(letter)
        
        result.onSuccess { count ->
            // Deduct 5 points for the letter check - this is handled in the repository
            
            val message = when (count) {
                0 -> "❌ Letter '$letter' is not in the word! (-5 points)"
                1 -> "✅ Letter '$letter' appears once in the word! (-5 points)"
                else -> "✅ Letter '$letter' appears $count times in the word! (-5 points)"
            }
            
            _uiState.value = _uiState.value.copy(message = message)
            
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(message = error.message ?: "Error checking letter")
        }
    }

    fun getWordLength() {
        val result = wordRepository.getWordLength()
        
        result.onSuccess { length ->
            _uiState.value = _uiState.value.copy(
                message = "💡 The word has $length letters!"
            )
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                message = error.message ?: "Error getting word length"
            )
        }
    }

    fun getHint() {
        if (!gameState.value.canUseHint) {
            val attemptsLeft = gameState.value.attemptsRemaining
            _uiState.value = _uiState.value.copy(
                message = "Hints available after 5 wrong attempts! (${10 - attemptsLeft}/5)"
            )
            return
        }

        viewModelScope.launch {
            val result = wordRepository.getHint()
            
            result.onSuccess { hint ->
                _uiState.value = _uiState.value.copy(
                    message = "💡 Hint: $hint"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    message = error.message ?: "Error getting hint"
                )
            }
        }
    }

    fun showLetterCheckDialog() {
        viewModelScope.launch {
            _uiEvents.emit(GameUiEvent.ShowLetterCheckDialog)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = "Guess the secret word!")
    }

    override fun onCleared() {
        super.onCleared()
        gameTimer.stop()
    }
}

data class GameUiState(
    val isLoading: Boolean = false,
    val currentGuess: String = "",
    val message: String = "Guess the secret word!"
)

data class CombinedGameState(
    val gameState: GameState,
    val uiState: GameUiState,
    val timerDisplay: String
)

sealed class GameUiEvent {
    object ShowLetterCheckDialog : GameUiEvent()
    data class ShowLevelComplete(
        val score: Int,
        val time: Long,
        val level: Int
    ) : GameUiEvent()
    data class ShowGameOver(val correctWord: String) : GameUiEvent()
    data class ShowError(val message: String) : GameUiEvent()
}