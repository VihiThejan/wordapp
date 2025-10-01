package com.example.wordapp.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wordapp.MainActivity
import com.example.wordapp.databinding.ActivityOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Set up text input listener
        binding.etPlayerName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updatePlayerName(s?.toString() ?: "")
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Set up start game button click
        binding.btnStartGame.setOnClickListener {
            viewModel.savePlayerNameAndProceed()
        }
        
        // Focus on name input
        binding.etPlayerName.requestFocus()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateUI(uiState)
                }
            }
        }
    }
    
    private fun updateUI(uiState: OnboardingUiState) {
        // Update name error
        binding.tilPlayerName.error = uiState.nameError
        
        // Update loading state
        if (uiState.isLoading) {
            binding.btnStartGame.text = ""
            binding.btnStartGame.icon = null
            binding.progressLoading.visibility = View.VISIBLE
            binding.btnStartGame.isEnabled = false
            binding.etPlayerName.isEnabled = false
        } else {
            binding.btnStartGame.text = "Start Playing"
            binding.btnStartGame.setIconResource(android.R.drawable.ic_media_play)
            binding.progressLoading.visibility = View.GONE
            binding.btnStartGame.isEnabled = true
            binding.etPlayerName.isEnabled = true
        }
        
        // Navigate to main game if onboarding is complete
        if (uiState.onboardingComplete) {
            navigateToMainGame()
        }
    }
    
    private fun navigateToMainGame() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}