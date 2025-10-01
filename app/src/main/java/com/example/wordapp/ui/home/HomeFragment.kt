package com.example.wordapp.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wordapp.R
import com.example.wordapp.data.model.GameStatus
import com.example.wordapp.databinding.FragmentGameBinding
import com.example.wordapp.presentation.game.GameUiEvent
import com.example.wordapp.presentation.game.GameViewModel
import com.example.wordapp.presentation.game.GuessHistoryAdapter
import com.example.wordapp.presentation.game.GuessHistoryItem
import com.example.wordapp.ui.leaderboard.LeaderboardFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    
    private val gameViewModel: GameViewModel by viewModels()
    private lateinit var guessHistoryAdapter: GuessHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        setupTextWatcher()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        guessHistoryAdapter = GuessHistoryAdapter()
        binding.rvGuessHistory.apply {
            adapter = guessHistoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmitGuess.setOnClickListener {
            gameViewModel.submitGuess()
        }

        binding.btnCheckLetter.setOnClickListener {
            gameViewModel.showLetterCheckDialog()
        }

        binding.btnWordLength.setOnClickListener {
            gameViewModel.getWordLength()
        }

        binding.btnGetHint.setOnClickListener {
            gameViewModel.getHint()
        }

        binding.btnNewGame.setOnClickListener {
            gameViewModel.startNewGame()
        }

        binding.btnLeaderboard.setOnClickListener {
            showLeaderboard()
        }
    }

    private fun setupTextWatcher() {
        binding.etWordGuess.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                gameViewModel.updateGuess(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    gameViewModel.combinedState.collect { state ->
                        updateUI(state.gameState, state.uiState, state.timerDisplay)
                    }
                }

                launch {
                    gameViewModel.uiEvents.collect { event ->
                        handleUiEvent(event)
                    }
                }
            }
        }
    }

    private fun updateUI(
        gameState: com.example.wordapp.data.model.GameState,
        uiState: com.example.wordapp.presentation.game.GameUiState,
        timerDisplay: String
    ) {
        // Update game stats
        binding.tvScore.text = gameState.currentScore.toString()
        binding.tvLevel.text = gameState.level.toString()
        binding.tvAttempts.text = gameState.attemptsRemaining.toString()
        binding.tvTimer.text = timerDisplay

        // Update game status
        binding.tvGameStatus.text = uiState.message

        // Update loading state
        binding.progressLoading.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
        binding.cardWordInput.visibility = if (uiState.isLoading) View.GONE else View.VISIBLE

        // Update guess input
        if (binding.etWordGuess.text.toString() != uiState.currentGuess) {
            binding.etWordGuess.setText(uiState.currentGuess)
            binding.etWordGuess.setSelection(uiState.currentGuess.length)
        }

        // Update button states
        updateButtonStates(gameState)

        // Update guess history
        updateGuessHistory(gameState.guessHistory, gameState.secretWord)

        // Handle game status
        when (gameState.gameStatus) {
            GameStatus.LOADING -> {
                binding.progressLoading.visibility = View.VISIBLE
            }
            GameStatus.WON -> {
                binding.etWordGuess.isEnabled = false
                binding.btnSubmitGuess.isEnabled = false
            }
            GameStatus.LOST -> {
                binding.etWordGuess.isEnabled = false
                binding.btnSubmitGuess.isEnabled = false
            }
            GameStatus.IN_PROGRESS -> {
                binding.etWordGuess.isEnabled = true
                binding.btnSubmitGuess.isEnabled = true
            }
            GameStatus.ERROR -> {
                // Handle error state
            }
        }
    }

    private fun updateButtonStates(gameState: com.example.wordapp.data.model.GameState) {
        // Check Letter button
        binding.btnCheckLetter.isEnabled = gameState.canCheckLetter
        binding.btnCheckLetter.alpha = if (gameState.canCheckLetter) 1.0f else 0.5f

        // Word Length button
        binding.btnWordLength.isEnabled = gameState.canGetWordLength
        binding.btnWordLength.alpha = if (gameState.canGetWordLength) 1.0f else 0.5f

        // Hint button
        binding.btnGetHint.isEnabled = gameState.canUseHint
        binding.btnGetHint.alpha = if (gameState.canUseHint) 1.0f else 0.5f
        
        val attemptsUsed = 10 - gameState.attemptsRemaining
        binding.btnGetHint.text = if (gameState.hintsUsed > 0) {
            "Used\n(1/1)"
        } else {
            "Hint\n($attemptsUsed/5)"
        }
    }

    private fun updateGuessHistory(guessHistory: List<String>, secretWord: String) {
        val historyItems = guessHistory.mapIndexed { index, guess ->
            GuessHistoryItem(
                number = index + 1,
                guess = guess,
                isCorrect = guess.equals(secretWord, ignoreCase = true)
            )
        }.reversed() // Show most recent first

        guessHistoryAdapter.submitList(historyItems)
        
        binding.tvNoGuesses.visibility = if (historyItems.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun handleUiEvent(event: GameUiEvent) {
        when (event) {
            is GameUiEvent.ShowLetterCheckDialog -> {
                showLetterCheckDialog()
            }
            is GameUiEvent.ShowLevelComplete -> {
                showLevelCompleteDialog(event.score, event.time, event.level)
            }
            is GameUiEvent.ShowGameOver -> {
                showGameOverDialog(event.correctWord)
            }
            is GameUiEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showLetterCheckDialog() {
        val currentState = gameViewModel.gameState.value
        
        com.example.wordapp.presentation.dialogs.LetterCheckDialog.show(
            requireContext(),
            currentState.currentScore
        ) { letter ->
            gameViewModel.checkLetter(letter)
        }
    }

    private fun showLevelCompleteDialog(score: Int, time: Long, level: Int) {
        val timeFormatted = formatTime(time)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🎉 Level Complete!")
            .setMessage(
                "Congratulations!\n\n" +
                "Level: $level\n" +
                "Final Score: $score points\n" +
                "Time: $timeFormatted\n\n" +
                "Ready for the next level?"
            )
            .setPositiveButton("Next Level") { _, _ ->
                gameViewModel.startNewGame(level + 1)
            }
            .setNegativeButton("Stay Here") { _, _ ->
                gameViewModel.startNewGame(level)
            }
            .setCancelable(false)
            .show()
    }

    private fun showGameOverDialog(correctWord: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("😞 Game Over")
            .setMessage("The correct word was: $correctWord\n\nTry again?")
            .setPositiveButton("New Game") { _, _ ->
                gameViewModel.startNewGame()
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }
    
    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun showLeaderboard() {
        val leaderboardFragment = LeaderboardFragment()
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, leaderboardFragment)
            .addToBackStack("leaderboard")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}