package com.example.wordapp.data.repository

import com.example.wordapp.data.model.GameState
import com.example.wordapp.data.model.GameStatus
import com.example.wordapp.data.model.GuessResult
import com.example.wordapp.data.remote.FallbackWords
import com.example.wordapp.data.remote.WordApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val wordApiService: WordApiService
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    suspend fun startNewGame(level: Int = 1): Result<String> {
        return try {
            _gameState.value = _gameState.value.copy(gameStatus = GameStatus.LOADING)
            
            val word = fetchWordForLevel(level)
            
            _gameState.value = GameState(
                secretWord = word.uppercase(),
                level = level,
                gameStatus = GameStatus.IN_PROGRESS,
                timeStarted = System.currentTimeMillis()
            )
            
            Result.success(word)
        } catch (e: Exception) {
            _gameState.value = _gameState.value.copy(gameStatus = GameStatus.ERROR)
            Result.failure(e)
        }
    }

    private suspend fun fetchWordForLevel(level: Int): String {
        return try {
            // For now, use fallback words. Later we'll integrate with API Ninjas
            val word = FallbackWords.getRandomWordForLevel(level)
            
            // Ensure word meets level requirements
            val requiredLength = when (level) {
                1 -> 4..5
                2 -> 6..7
                3 -> 8..9
                else -> 10..15
            }
            
            if (word.length in requiredLength) {
                word
            } else {
                FallbackWords.getRandomWordForLevel(level)
            }
        } catch (e: Exception) {
            // Fallback to a default word if all else fails
            FallbackWords.getRandomWordForLevel(1)
        }
    }

    fun makeGuess(guess: String): GuessResult {
        val currentState = _gameState.value
        val normalizedGuess = guess.trim().uppercase()
        val secretWord = currentState.secretWord.uppercase()

        return if (normalizedGuess == secretWord) {
            // Correct guess - player wins
            val newState = currentState.copy(
                gameStatus = GameStatus.WON,
                guessHistory = currentState.guessHistory + guess
            )
            _gameState.value = newState
            
            GuessResult(
                isCorrect = true,
                newGameState = newState,
                message = "🎉 Congratulations! You guessed it!"
            )
        } else {
            // Wrong guess - deduct points and attempts
            val newScore = maxOf(0, currentState.currentScore - 10)
            val newAttempts = maxOf(0, currentState.attemptsRemaining - 1)
            val newHistory = currentState.guessHistory + guess
            
            val gameStatus = when {
                newScore == 0 -> GameStatus.LOST
                newAttempts == 0 -> GameStatus.LOST
                else -> GameStatus.IN_PROGRESS
            }
            
            val newState = currentState.copy(
                currentScore = newScore,
                attemptsRemaining = newAttempts,
                guessHistory = newHistory,
                gameStatus = gameStatus
            )
            _gameState.value = newState
            
            val message = when (gameStatus) {
                GameStatus.LOST -> "😞 Game Over! The word was: ${currentState.secretWord}"
                else -> "❌ Wrong guess! Try again. (-10 points)"
            }
            
            GuessResult(
                isCorrect = false,
                newGameState = newState,
                message = message
            )
        }
    }

    fun checkLetterOccurrence(letter: Char): Result<Int> {
        val currentState = _gameState.value
        
        if (!currentState.canCheckLetter) {
            return Result.failure(Exception("Not enough points! Need 5 points."))
        }
        
        val count = currentState.secretWord.count { 
            it.equals(letter, ignoreCase = true) 
        }
        
        val newState = currentState.copy(
            currentScore = currentState.currentScore - 5,
            letterChecksUsed = currentState.letterChecksUsed + 1
        )
        _gameState.value = newState
        
        return Result.success(count)
    }

    fun getWordLength(): Result<Int> {
        val currentState = _gameState.value
        
        if (!currentState.canGetWordLength) {
            return Result.failure(Exception("Not enough points or already revealed!"))
        }
        
        val newState = currentState.copy(
            currentScore = currentState.currentScore - 5,
            wordLengthRevealed = true
        )
        _gameState.value = newState
        
        return Result.success(currentState.secretWord.length)
    }

    suspend fun getHint(): Result<String> {
        val currentState = _gameState.value
        
        if (!currentState.canUseHint) {
            return Result.failure(Exception("Hints available after 5 wrong attempts!"))
        }
        
        val hint = try {
            // First try to get a rhyme
            val rhymeResponse = wordApiService.getRhymes(currentState.secretWord)
            if (rhymeResponse.isSuccessful && !rhymeResponse.body()?.rhymes.isNullOrEmpty()) {
                val rhymes = rhymeResponse.body()!!.rhymes
                val selectedRhyme = rhymes.random()
                "Rhymes with: $selectedRhyme"
            } else {
                // If no rhymes, try thesaurus
                val thesaurusResponse = wordApiService.getSynonyms(currentState.secretWord)
                if (thesaurusResponse.isSuccessful && !thesaurusResponse.body()?.synonyms.isNullOrEmpty()) {
                    val synonyms = thesaurusResponse.body()!!.synonyms
                    val selectedSynonym = synonyms.random()
                    "Similar to: $selectedSynonym"
                } else {
                    // Fallback to simple hint
                    generateSimpleHint(currentState.secretWord)
                }
            }
        } catch (e: Exception) {
            // Network error fallback
            generateSimpleHint(currentState.secretWord)
        }
        
        val newState = currentState.copy(hintsUsed = currentState.hintsUsed + 1)
        _gameState.value = newState
        
        return Result.success(hint)
    }

    private fun generateSimpleHint(word: String): String {
        val hints = listOf(
            "The word has ${word.length} letters",
            "It starts with '${word.first()}'",
            "It ends with '${word.last()}'",
            "It contains the letter '${word[word.length / 2]}'",
            "Think about words in this category",
            "It's a common English word"
        )
        return hints.random()
    }

    fun resetGame() {
        _gameState.value = GameState()
    }
}