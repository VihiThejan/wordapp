package com.example.wordapp.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wordapp.MainActivity
import com.example.wordapp.databinding.ActivitySplashBinding
import com.example.wordapp.presentation.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        observeViewModel()
        viewModel.checkUserStatus()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { destination ->
                    when (destination) {
                        NavigationDestination.Onboarding -> {
                            startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
                            finish()
                        }
                        NavigationDestination.MainGame -> {
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }
                        NavigationDestination.None -> {
                            // Stay on splash screen
                        }
                    }
                }
            }
        }
    }
}