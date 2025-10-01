package com.example.wordapp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameTimer {
    private var startTime: Long = 0
    private var isRunning = false
    private var timerJob: Job? = null
    
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    private val _formattedTime = MutableStateFlow("00:00")
    val formattedTime: StateFlow<String> = _formattedTime.asStateFlow()

    fun start(scope: CoroutineScope) {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            isRunning = true
            startTimerUpdates(scope)
        }
    }

    fun stop(): Long {
        isRunning = false
        timerJob?.cancel()
        return if (startTime != 0L) {
            (System.currentTimeMillis() - startTime) / 1000
        } else 0L
    }

    fun pause() {
        isRunning = false
        timerJob?.cancel()
    }

    fun reset() {
        isRunning = false
        timerJob?.cancel()
        startTime = 0L
        _elapsedTime.value = 0L
        _formattedTime.value = "00:00"
    }

    private fun startTimerUpdates(scope: CoroutineScope) {
        timerJob = scope.launch {
            while (isRunning) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _elapsedTime.value = elapsed
                _formattedTime.value = formatTime(elapsed)
                delay(1000)
            }
        }
    }

    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun getCurrentElapsedSeconds(): Long {
        return if (isRunning && startTime != 0L) {
            (System.currentTimeMillis() - startTime) / 1000
        } else {
            _elapsedTime.value
        }
    }
}