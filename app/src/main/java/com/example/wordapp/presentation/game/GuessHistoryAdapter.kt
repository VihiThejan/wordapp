package com.example.wordapp.presentation.game

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wordapp.R
import com.example.wordapp.databinding.ItemGuessHistoryBinding

class GuessHistoryAdapter : ListAdapter<GuessHistoryItem, GuessHistoryAdapter.GuessHistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuessHistoryViewHolder {
        val binding = ItemGuessHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GuessHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuessHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GuessHistoryViewHolder(
        private val binding: ItemGuessHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GuessHistoryItem) {
            binding.tvGuessNumber.text = item.number.toString()
            binding.tvGuessText.text = item.guess
            
            // Set appropriate icon and color based on correctness
            if (item.isCorrect) {
                binding.ivGuessResult.setImageResource(android.R.drawable.ic_input_add)
                binding.ivGuessResult.setColorFilter(
                    ContextCompat.getColor(binding.root.context, R.color.success_green)
                )
            } else {
                binding.ivGuessResult.setImageResource(android.R.drawable.ic_delete)
                binding.ivGuessResult.setColorFilter(
                    ContextCompat.getColor(binding.root.context, R.color.error_red)
                )
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GuessHistoryItem>() {
        override fun areItemsTheSame(oldItem: GuessHistoryItem, newItem: GuessHistoryItem): Boolean {
            return oldItem.number == newItem.number && oldItem.guess == newItem.guess
        }

        override fun areContentsTheSame(oldItem: GuessHistoryItem, newItem: GuessHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}

data class GuessHistoryItem(
    val number: Int,
    val guess: String,
    val isCorrect: Boolean
)