package com.example.wordapp.domain.usecase

import com.example.wordapp.data.repository.LeaderboardRepository
import com.example.wordapp.domain.model.GameError
import com.example.wordapp.domain.model.Resource
import com.example.wordapp.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLeaderboardUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<LeaderboardEntry>>> = flow {
        try {
            emit(Resource.Loading("Loading leaderboard..."))
            
            val result = leaderboardRepository.fetchLeaderboard()
            
            if (result.isSuccess) {
                val leaderboard = result.getOrThrow()
                emit(Resource.Success(leaderboard))
            } else {
                emit(Resource.Error(GameError.NetworkError))
            }
        } catch (e: Exception) {
            emit(Resource.Error(GameError.UnknownError(e.message ?: "Error loading leaderboard")))
        }
    }
}

class SubmitScoreUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) {
    suspend operator fun invoke(
        playerName: String,
        score: Int,
        completionTimeMs: Long,
        level: Int
    ): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading("Submitting score..."))
            
            val result = leaderboardRepository.submitScore(
                playerName = playerName,
                score = score,
                completionTimeMs = completionTimeMs,
                level = level
            )
            
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrThrow()))
            } else {
                emit(Resource.Error(GameError.NetworkError))
            }
        } catch (e: Exception) {
            emit(Resource.Error(GameError.UnknownError(e.message ?: "Error submitting score")))
        }
    }
}

class ClearLeaderboardUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) {
    suspend operator fun invoke(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading("Clearing leaderboard..."))
            
            val result = leaderboardRepository.clearLeaderboard()
            
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrThrow()))
            } else {
                emit(Resource.Error(GameError.NetworkError))
            }
        } catch (e: Exception) {
            emit(Resource.Error(GameError.UnknownError(e.message ?: "Error clearing leaderboard")))
        }
    }
}