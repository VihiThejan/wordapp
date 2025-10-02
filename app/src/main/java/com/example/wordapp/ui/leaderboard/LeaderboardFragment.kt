package com.example.wordapp.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wordapp.databinding.FragmentLeaderboardBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LeaderboardViewModel by viewModels()
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter()
        
        binding.recyclerViewLeaderboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaderboardAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            viewModel.loadLeaderboard()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadLeaderboard()
        }

        binding.btnAddSampleScores.setOnClickListener {
            addSampleScores()
        }

        binding.fabPlayGame.setOnClickListener {
            // Navigate back to game
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        updateUI(uiState)
                    }
                }
                
                launch {
                    viewModel.leaderboard.collect { entries ->
                        updateLeaderboard(entries)
                    }
                }
                
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        updateLoadingState(isLoading)
                    }
                }
            }
        }
    }

    private fun updateUI(uiState: LeaderboardUiState) {
        with(binding) {
            // Show/hide different states
            layoutLoading.isVisible = uiState.isLoading
            layoutError.isVisible = uiState.errorMessage != null
            layoutEmpty.isVisible = uiState.isEmpty && !uiState.isLoading && uiState.errorMessage == null
            recyclerViewLeaderboard.isVisible = !uiState.isLoading && uiState.errorMessage == null && !uiState.isEmpty

            // Update error message
            uiState.errorMessage?.let { error ->
                tvErrorMessage.text = error
                showSnackbar(error)
                viewModel.clearError()
            }

            // Update success message
            uiState.message?.let { message ->
                showSnackbar(message)
                viewModel.clearMessage()
            }
        }
        
        // Update player stats if available
        updatePlayerStats()
    }

    private fun updateLeaderboard(entries: List<com.example.wordapp.data.model.LeaderboardEntry>) {
        leaderboardAdapter.submitList(entries)
        
        // Show/hide empty state
        binding.layoutEmpty.isVisible = entries.isEmpty() && 
                !binding.layoutLoading.isVisible && 
                !binding.layoutError.isVisible
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.layoutLoading.isVisible = isLoading
        binding.btnRefresh.isEnabled = !isLoading
    }

    private fun updatePlayerStats() {
        val playerStats = viewModel.getPlayerStats()
        
        binding.cardPlayerStats.isVisible = playerStats != null
        
        playerStats?.let { stats ->
            with(binding) {
                tvPlayerName.text = "${stats.name}'s Best"
                tvPlayerStats.text = "Score: ${stats.score} | Time: ${stats.getFormattedTime()} | Level: ${stats.level}"
                tvPlayerRank.text = "#${stats.rank}"
            }
        }
    }

    private fun addSampleScores() {
        // Disable button to prevent multiple submissions
        binding.btnAddSampleScores.isEnabled = false
        
        // Submit sample scores with different player names
        viewModel.submitCustomScore("AliceGamer", 1250, 95000L, 3)  // Score: 1250, Time: 1:35, Level: 3
        
        // Add a slight delay and submit another score
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            viewModel.submitCustomScore("BobWordMaster", 980, 120000L, 2)  // Score: 980, Time: 2:00, Level: 2
            
            // Re-enable button after submissions
            binding.btnAddSampleScores.isEnabled = true
            
            showSnackbar("Sample scores added! Check the leaderboard.")
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}