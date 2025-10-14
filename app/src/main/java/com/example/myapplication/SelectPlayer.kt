package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectPlayer : AppCompatActivity() {
    private lateinit var adapter: PlayerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var exitButton: Button
    val db : PlayerDatabase? = App.getInstance()?.getDataBase()
    val playerDao = db?.playerDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selection)

        exitButton = findViewById(R.id.exitButton)

        exitButton.setOnClickListener {
            startActivity(Intent(this@SelectPlayer, MainActivity::class.java))
        }

        recyclerView = findViewById(R.id.playersView)
        adapter = PlayerAdapter(
            onPlayerClick = { player ->
                startGameWithPlayer(player)
            },
            onDeleteClick = { player ->
                AlertDialog.Builder(this)
                    .setTitle("Удаление игрока")
                    .setMessage("Вы уверены, что хотите удалить ${player.name}?")
                    .setPositiveButton("Удалить") { dialog, which ->
                        deletePlayer(player)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SelectPlayer)
            adapter = this@SelectPlayer.adapter
            setHasFixedSize(true)
        }

        loadPlayers()
    }

    private fun loadPlayers() {
        lifecycleScope.launch {
            val playersList = withContext(Dispatchers.IO) {
                playerDao?.getAllPlayers() ?: emptyList()
            }
            adapter.submitList(playersList)
        }
    }

    private fun deletePlayer(player: Player) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                playerDao?.delete(player)
            }
            loadPlayers()
        }
    }

    private fun startGameWithPlayer(player: Player) {
        val intent = Intent(this, Game::class.java).apply {
            putExtra("PLAYER_ID", player.id)
            putExtra("PLAYER_DIFFICULTY", player.difficulty)
        }
        startActivity(intent)
    }
}