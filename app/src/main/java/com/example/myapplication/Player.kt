package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String? = null,
    var gender: String? = null,
    var course: String? = null,
    var difficulty: String? = null,
    var date: String? = null,
    var zodiac: String? = null,
    var bestScore: Int = 0
)

class PlayerAdapter(private val onPlayerClick: (Player) -> Unit, private val onDeleteClick: (Player) -> Unit) :
    ListAdapter<Player, PlayerAdapter.PlayerViewHolder>(PlayerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = getItem(position)
        holder.bind(player, onPlayerClick, onDeleteClick)
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
        private val statsTextView: TextView = itemView.findViewById(R.id.playerStatsTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(player: Player, onPlayerClick: (Player) -> Unit, onDeleteClick: (Player) -> Unit) {
            nameTextView.text = player.name
            statsTextView.text = "Рекорд: ${player.bestScore} | Сложность: ${player.difficulty}"

            itemView.setOnClickListener {
                onPlayerClick(player)
            }
            deleteButton.setOnClickListener {
                onDeleteClick(player)
            }
        }
    }
}

class PlayerDiffCallback : DiffUtil.ItemCallback<Player>() {
    override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
        return oldItem == newItem
    }
}
