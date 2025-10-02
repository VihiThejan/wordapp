package com.example.wordapp.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.example.wordapp.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.button.MaterialButton

/**
 * Common UI components and utilities for the Word Guessing Game
 */

// Loading State Component
class LoadingStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val progressIndicator: CircularProgressIndicator
    private val loadingText: MaterialTextView

    init {
        inflate(context, R.layout.component_loading_state, this)
        orientation = VERTICAL
        
        progressIndicator = findViewById(R.id.progressIndicator)
        loadingText = findViewById(R.id.tvLoadingText)
    }

    fun setLoadingMessage(message: String) {
        loadingText.text = message
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}

// Error State Component  
class ErrorStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val errorTitle: MaterialTextView
    private val errorMessage: MaterialTextView
    private val retryButton: MaterialButton

    var onRetryClick: (() -> Unit)? = null

    init {
        inflate(context, R.layout.component_error_state, this)
        orientation = VERTICAL
        
        errorTitle = findViewById(R.id.tvErrorTitle)
        errorMessage = findViewById(R.id.tvErrorMessage)
        retryButton = findViewById(R.id.btnRetry)
        
        retryButton.setOnClickListener { onRetryClick?.invoke() }
    }

    fun setError(title: String, message: String) {
        errorTitle.text = title
        errorMessage.text = message
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}

// Empty State Component
class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val emptyTitle: MaterialTextView
    private val emptyMessage: MaterialTextView
    private val actionButton: MaterialButton

    var onActionClick: (() -> Unit)? = null

    init {
        inflate(context, R.layout.component_empty_state, this)
        orientation = VERTICAL
        
        emptyTitle = findViewById(R.id.tvEmptyTitle)
        emptyMessage = findViewById(R.id.tvEmptyMessage)
        actionButton = findViewById(R.id.btnAction)
        
        actionButton.setOnClickListener { onActionClick?.invoke() }
    }

    fun setEmptyState(title: String, message: String, actionText: String) {
        emptyTitle.text = title
        emptyMessage.text = message
        actionButton.text = actionText
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}

// Game Stats Card Component
class GameStatsCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val scoreText: MaterialTextView
    private val levelText: MaterialTextView
    private val attemptsText: MaterialTextView
    private val timerText: MaterialTextView

    init {
        inflate(context, R.layout.component_game_stats, this)
        
        scoreText = findViewById(R.id.tvScore)
        levelText = findViewById(R.id.tvLevel)
        attemptsText = findViewById(R.id.tvAttempts)
        timerText = findViewById(R.id.tvTimer)
    }

    fun updateStats(score: Int, level: Int, attempts: Int, timer: String) {
        scoreText.text = score.toString()
        levelText.text = "Level $level"
        attemptsText.text = "$attempts left"
        timerText.text = timer
    }
}