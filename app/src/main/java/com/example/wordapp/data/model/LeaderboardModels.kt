package com.example.wordapp.data.model

data class LeaderboardEntry(
    val name: String,
    val score: Int,
    val time: Long,
    val level: Int,
    val date: String = "",
    val rank: Int = 0
) {
    fun getFormattedTime(): String {
        val minutes = time / 60000
        val seconds = (time % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun getFormattedDate(): String {
        return if (date.isNotEmpty()) {
            date.take(10) // YYYY-MM-DD format
        } else {
            "Today"
        }
    }
}

data class LeaderboardResponse(
    val dreamlo: DreamloData?
)

data class DreamloData(
    val leaderboard: LeaderboardData?
)

data class LeaderboardData(
    val entry: List<LeaderboardEntryData>?
)

data class LeaderboardEntryData(
    val name: String,
    val score: String,
    val seconds: String,
    val text: String, // Will store level info
    val date: String
)

// Extension function to convert API response to our model
fun LeaderboardEntryData.toLeaderboardEntry(rank: Int): LeaderboardEntry {
    return LeaderboardEntry(
        name = name,
        score = score.toIntOrNull() ?: 0,
        time = (seconds.toLongOrNull() ?: 0L) * 1000, // Convert seconds to milliseconds
        level = text.toIntOrNull() ?: 1,
        date = date,
        rank = rank
    )
}