package com.example.wordapp.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordapp.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationDestination>()
    val navigationEvent: SharedFlow<NavigationDestination> = _navigationEvent.asSharedFlow()

    fun checkUserStatus() {
        viewModelScope.launch {
            // Show splash for minimum 2 seconds for better UX
            delay(2000)
            
            val destination = if (preferencesManager.isFirstLaunch) {
                NavigationDestination.Onboarding
            } else {
                NavigationDestination.MainGame
            }
            
            _navigationEvent.emit(destination)
        }
    }
}

enum class NavigationDestination {
    None, Onboarding, MainGame
}