package com.example.wordapp.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wordapp.R
import com.example.wordapp.data.model.LeaderboardEntry
import com.example.wordapp.databinding.ItemLeaderboardEntryBinding

class LeaderboardAdapter : ListAdapter<LeaderboardEntry, LeaderboardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLeaderboardEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry) {
            with(binding) {
                // Rank and trophy
                tvRank.text = "#${entry.rank}"
                
                // Show trophy for top 3 positions
                tvTrophy.isVisible = entry.rank <= 3
                when (entry.rank) {
                    1 -> tvTrophy.text = "🏆" // Gold
                    2 -> tvTrophy.text = "🥈" // Silver
                    3 -> tvTrophy.text = "🥉" // Bronze
                }
                
                // Player info
                tvPlayerName.text = entry.name
                tvScore.text = entry.score.toString()
                tvTime.text = entry.getFormattedTime()
                tvLevel.text = "Level ${entry.level}"
                tvDate.text = entry.getFormattedDate()
                
                // Highlight top 3 with different colors
                val context = itemView.context
                when (entry.rank) {
                    1 -> {
                        tvRank.setTextColor(ContextCompat.getColor(context, R.color.gold))
                        tvScore.setTextColor(ContextCompat.getColor(context, R.color.gold))
                    }
                    2 -> {
                        tvRank.setTextColor(ContextCompat.getColor(context, R.color.silver))
                        tvScore.setTextColor(ContextCompat.getColor(context, R.color.silver))
                    }
                    3 -> {
                        tvRank.setTextColor(ContextCompat.getColor(context, R.color.bronze))
                        tvScore.setTextColor(ContextCompat.getColor(context, R.color.bronze))
                    }
                    else -> {
                        tvRank.setTextColor(ContextCompat.getColor(context, R.color.purple_500))
                        tvScore.setTextColor(ContextCompat.getColor(context, R.color.purple_500))
                    }
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem.name == newItem.name && oldItem.score == newItem.score
        }

        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem == newItem
        }
    }
}