package com.example.wordapp.domain.usecase

import com.example.wordapp.data.repository.WordRepository
import com.example.wordapp.domain.model.GameError
import com.example.wordapp.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StartNewGameUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(level: Int): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading("Starting new game..."))
            
            val result = wordRepository.startNewGame(level)
            
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrThrow()))
            } else {
                emit(Resource.Error(GameError.NetworkError))
            }
        } catch (e: Exception) {
            emit(Resource.Error(GameError.UnknownError(e.message ?: "Unknown error occurred")))
        }
    }
}

class MakeGuessUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(guess: String): Resource<String> {
        return try {
            if (guess.isBlank()) {
                Resource.Error(GameError.InvalidInput)
            } else {
                val result = wordRepository.makeGuess(guess)
                Resource.Success(result.message)
            }
        } catch (e: Exception) {
            Resource.Error(GameError.UnknownError(e.message ?: "Error making guess"))
        }
    }
}

class CheckLetterOccurrenceUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(letter: Char): Resource<Int> {
        return try {
            val result = wordRepository.checkLetterOccurrence(letter)
            
            if (result.isSuccess) {
                Resource.Success(result.getOrThrow())
            } else {
                Resource.Error(GameError.InsufficientPoints)
            }
        } catch (e: Exception) {
            Resource.Error(GameError.UnknownError(e.message ?: "Error checking letter"))
        }
    }
}

class GetWordLengthUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Resource<Int> {
        return try {
            val result = wordRepository.getWordLength()
            
            if (result.isSuccess) {
                Resource.Success(result.getOrThrow())
            } else {
                Resource.Error(GameError.InsufficientPoints)
            }
        } catch (e: Exception) {
            Resource.Error(GameError.UnknownError(e.message ?: "Error getting word length"))
        }
    }
}

class GetHintUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading("Getting hint..."))
            
            val result = wordRepository.getHint()
            
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrThrow()))
            } else {
                emit(Resource.Error(GameError.GameNotInProgress))
            }
        } catch (e: Exception) {
            emit(Resource.Error(GameError.NetworkError))
        }
    }
}