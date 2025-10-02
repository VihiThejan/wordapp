package com.example.wordapp.di

import com.example.wordapp.data.repository.LeaderboardRepository
import com.example.wordapp.data.repository.WordRepository
import com.example.wordapp.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideStartNewGameUseCase(
        wordRepository: WordRepository
    ): StartNewGameUseCase = StartNewGameUseCase(wordRepository)

    @Provides
    @Singleton
    fun provideMakeGuessUseCase(
        wordRepository: WordRepository
    ): MakeGuessUseCase = MakeGuessUseCase(wordRepository)

    @Provides
    @Singleton
    fun provideCheckLetterOccurrenceUseCase(
        wordRepository: WordRepository
    ): CheckLetterOccurrenceUseCase = CheckLetterOccurrenceUseCase(wordRepository)

    @Provides
    @Singleton
    fun provideGetWordLengthUseCase(
        wordRepository: WordRepository
    ): GetWordLengthUseCase = GetWordLengthUseCase(wordRepository)

    @Provides
    @Singleton
    fun provideGetHintUseCase(
        wordRepository: WordRepository
    ): GetHintUseCase = GetHintUseCase(wordRepository)

    @Provides
    @Singleton
    fun provideGetLeaderboardUseCase(
        leaderboardRepository: LeaderboardRepository
    ): GetLeaderboardUseCase = GetLeaderboardUseCase(leaderboardRepository)

    @Provides
    @Singleton
    fun provideSubmitScoreUseCase(
        leaderboardRepository: LeaderboardRepository
    ): SubmitScoreUseCase = SubmitScoreUseCase(leaderboardRepository)

    @Provides
    @Singleton
    fun provideClearLeaderboardUseCase(
        leaderboardRepository: LeaderboardRepository
    ): ClearLeaderboardUseCase = ClearLeaderboardUseCase(leaderboardRepository)
}