package com.example.wordapp.data.util

/**
 * Data validation utilities for the Word Guessing Game
 */

object ValidationUtils {
    
    // Name validation
    fun isValidUserName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length in 2..20 && 
               name.all { it.isLetter() || it.isWhitespace() }
    }
    
    fun getUserNameError(name: String): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 20 -> "Name must not exceed 20 characters"
            !name.all { it.isLetter() || it.isWhitespace() } -> "Name can only contain letters and spaces"
            else -> null
        }
    }
    
    // Guess validation
    fun isValidGuess(guess: String, minLength: Int = 2, maxLength: Int = 15): Boolean {
        return guess.isNotBlank() && 
               guess.length in minLength..maxLength && 
               guess.all { it.isLetter() }
    }
    
    fun getGuessError(guess: String, minLength: Int = 2, maxLength: Int = 15): String? {
        return when {
            guess.isBlank() -> "Guess cannot be empty"
            guess.length < minLength -> "Guess must be at least $minLength characters"
            guess.length > maxLength -> "Guess must not exceed $maxLength characters"
            !guess.all { it.isLetter() } -> "Guess can only contain letters"
            else -> null
        }
    }
    
    // Letter validation
    fun isValidLetter(input: String): Boolean {
        return input.length == 1 && input.first().isLetter()
    }
    
    fun getLetterError(input: String): String? {
        return when {
            input.isBlank() -> "Please enter a letter"
            input.length != 1 -> "Please enter only one letter"
            !input.first().isLetter() -> "Please enter a valid letter (A-Z)"
            else -> null
        }
    }
    
    // Score validation
    fun isValidScore(score: Int): Boolean {
        return score in 0..1000
    }
    
    // Level validation
    fun isValidLevel(level: Int): Boolean {
        return level in 1..10
    }
    
    // Time validation
    fun isValidTime(timeMs: Long): Boolean {
        return timeMs > 0 && timeMs < 24 * 60 * 60 * 1000L // Less than 24 hours
    }
    
    // API key validation
    fun isValidApiKey(key: String): Boolean {
        return key.isNotBlank() && key.length > 10
    }
}

object FormatUtils {
    
    // Format time in MM:SS format
    fun formatTime(timeMs: Long): String {
        if (timeMs <= 0) return "00:00"
        
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    // Format score with commas
    fun formatScore(score: Int): String {
        return when {
            score < 1000 -> score.toString()
            score < 1000000 -> "${score / 1000}K"
            else -> "${score / 1000000}M"
        }
    }
    
    // Format percentage
    fun formatPercentage(percentage: Double): String {
        return String.format("%.1f%%", percentage)
    }
    
    // Format player name for display
    fun formatPlayerName(name: String): String {
        return name.trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
    
    // Format level display
    fun formatLevel(level: Int): String {
        return "Level $level"
    }
    
    // Format attempts remaining
    fun formatAttemptsRemaining(attempts: Int): String {
        return when (attempts) {
            0 -> "No attempts left"
            1 -> "1 attempt left"
            else -> "$attempts attempts left"
        }
    }
}