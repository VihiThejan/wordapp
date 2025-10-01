package com.example.wordapp.presentation.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.example.wordapp.R
import com.example.wordapp.databinding.DialogLetterCheckBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LetterCheckDialog(
    context: Context,
    private val currentPoints: Int,
    private val onLetterCheck: (Char) -> Unit
) : AlertDialog(context) {

    private lateinit var binding: DialogLetterCheckBinding
    private val letterButtons = mutableListOf<Button>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DialogLetterCheckBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        setupUI()
        setupLetterButtons()
        setupClickListeners()
    }
    
    private fun setupUI() {
        // Disable check button initially
        binding.btnCheck.isEnabled = false
        
        // Show warning if insufficient points
        if (currentPoints < 5) {
            binding.letterInputLayout.error = "Insufficient points! You need at least 5 points."
            binding.btnCheck.isEnabled = false
        }
        
        // Setup text input validation
        binding.letterInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput(s?.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupLetterButtons() {
        // Collect all alphabet buttons
        letterButtons.addAll(listOf(
            binding.btnA, binding.btnB, binding.btnC, binding.btnD, binding.btnE, binding.btnF, binding.btnG,
            binding.btnH, binding.btnI, binding.btnJ, binding.btnK, binding.btnL, binding.btnM, binding.btnN,
            binding.btnO, binding.btnP, binding.btnQ, binding.btnR, binding.btnS, binding.btnT, binding.btnU,
            binding.btnV, binding.btnW, binding.btnX, binding.btnY, binding.btnZ
        ))
        
        // Set click listeners for all letter buttons
        letterButtons.forEach { button ->
            button.setOnClickListener {
                val letter = button.text.toString()
                binding.letterInput.setText(letter)
                selectLetter(letter.first())
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnCheck.setOnClickListener {
            val letter = binding.letterInput.text?.toString()?.uppercase()?.firstOrNull()
            if (letter != null && isValidLetter(letter)) {
                onLetterCheck(letter)
                dismiss()
            }
        }
    }
    
    private fun validateInput(input: String?) {
        binding.letterInputLayout.error = null
        
        if (currentPoints < 5) {
            binding.letterInputLayout.error = "Insufficient points! You need at least 5 points."
            binding.btnCheck.isEnabled = false
            return
        }
        
        when {
            input.isNullOrEmpty() -> {
                binding.btnCheck.isEnabled = false
            }
            input.length > 1 -> {
                binding.letterInputLayout.error = "Please enter only one letter"
                binding.btnCheck.isEnabled = false
            }
            !isValidLetter(input.first()) -> {
                binding.letterInputLayout.error = "Please enter a valid letter (A-Z)"
                binding.btnCheck.isEnabled = false
            }
            else -> {
                binding.btnCheck.isEnabled = true
                highlightSelectedButton(input.uppercase().first())
            }
        }
    }
    
    private fun selectLetter(letter: Char) {
        if (currentPoints >= 5) {
            validateInput(letter.toString())
            highlightSelectedButton(letter)
        }
    }
    
    private fun highlightSelectedButton(letter: Char) {
        // Reset all button states
        letterButtons.forEach { button ->
            button.isSelected = false
        }
        
        // Highlight selected button
        letterButtons.find { it.text.toString() == letter.toString() }?.isSelected = true
    }
    
    private fun isValidLetter(char: Char): Boolean {
        return char.uppercaseChar() in 'A'..'Z'
    }
    
    companion object {
        fun show(
            context: Context,
            currentPoints: Int,
            onLetterCheck: (Char) -> Unit
        ) {
            LetterCheckDialog(context, currentPoints, onLetterCheck).show()
        }
    }
}