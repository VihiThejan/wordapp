package com.example.wordapp.data.remote

import com.example.wordapp.data.model.LeaderboardResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DreamloApiService {
    
    @GET("dreamlo/{publicKey}/json")
    suspend fun getLeaderboard(
        @Path("publicKey") publicKey: String
    ): Response<LeaderboardResponse>
    
    @GET("dreamlo/{privateKey}/add/{name}/{score}")
    suspend fun addLeaderboardEntry(
        @Path("privateKey") privateKey: String,
        @Path("name") name: String,
        @Path("score") score: Int
    ): Response<String>
    
    @GET("dreamlo/{privateKey}/clear")
    suspend fun clearLeaderboard(
        @Path("privateKey") privateKey: String
    ): Response<String>
}