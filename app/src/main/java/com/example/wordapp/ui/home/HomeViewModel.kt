package com.example.wordapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wordapp.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        val userName = preferencesManager.userName ?: "Player"
        val gamesPlayed = preferencesManager.gamesPlayed
        val highScore = preferencesManager.highScore
        
        value = buildString {
            appendLine("Welcome, $userName! 🎮")
            appendLine()
            appendLine("📊 Your Stats:")
            appendLine("• Games Played: $gamesPlayed")
            appendLine("• High Score: $highScore")
            appendLine()
            appendLine("Ready to play Word Game?")
            appendLine("Guess the secret word and climb the leaderboard!")
        }
    }
    val text: LiveData<String> = _text
}