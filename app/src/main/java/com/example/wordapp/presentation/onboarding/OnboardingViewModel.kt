package com.example.wordapp.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordapp.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updatePlayerName(name: String) {
        _uiState.value = _uiState.value.copy(
            playerName = name,
            nameError = null
        )
    }

    fun savePlayerNameAndProceed() {
        val currentName = _uiState.value.playerName.trim()
        
        if (!isValidName(currentName)) {
            _uiState.value = _uiState.value.copy(
                nameError = getNameError(currentName)
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // Save the name to preferences
                preferencesManager.userName = currentName
                
                // Simulate a brief loading delay for better UX
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    onboardingComplete = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nameError = "Failed to save name. Please try again."
                )
            }
        }
    }

    private fun isValidName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length >= 2 && 
               name.length <= 20 &&
               name.all { it.isLetter() || it.isWhitespace() }
    }

    private fun getNameError(name: String): String {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters long"
            name.length > 20 -> "Name cannot exceed 20 characters"
            !name.all { it.isLetter() || it.isWhitespace() } -> "Name can only contain letters and spaces"
            else -> "Please enter a valid name"
        }
    }
}

data class OnboardingUiState(
    val playerName: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val onboardingComplete: Boolean = false
)