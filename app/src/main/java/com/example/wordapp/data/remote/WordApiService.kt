package com.example.wordapp.data.remote

import com.example.wordapp.data.model.WordApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface WordApiService {
    
    @GET("v1/randomword")
    @Headers("X-Api-Key: {api_key}")
    suspend fun getRandomWord(): Response<WordApiResponse>
    
    @GET("v1/rhyme")
    @Headers("X-Api-Key: {api_key}")
    suspend fun getRhymes(@Query("word") word: String): Response<RhymeResponse>
    
    @GET("v1/thesaurus")
    @Headers("X-Api-Key: {api_key}")
    suspend fun getSynonyms(@Query("word") word: String): Response<ThesaurusResponse>
}

data class RhymeResponse(
    val rhymes: List<String>
)

data class ThesaurusResponse(
    val synonyms: List<String>,
    val antonyms: List<String>
)

// Fallback words for offline mode or API failures
object FallbackWords {
    val wordsByLevel = mapOf(
        1 to listOf("cat", "dog", "sun", "moon", "star", "book", "tree", "fish", "bird", "door"),
        2 to listOf("house", "water", "green", "happy", "world", "music", "light", "phone", "table", "chair"),
        3 to listOf("computer", "elephant", "mountain", "rainbow", "butterfly", "treasure", "keyboard", "sandwich"),
        4 to listOf("incredible", "magnificent", "extraordinary", "understanding", "responsibility", "transformation")
    )
    
    fun getRandomWordForLevel(level: Int): String {
        val levelWords = when {
            level <= 1 -> wordsByLevel[1]!!
            level == 2 -> wordsByLevel[2]!!
            level == 3 -> wordsByLevel[3]!!
            else -> wordsByLevel[4]!!
        }
        return levelWords.random()
    }
}