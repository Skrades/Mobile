package com.example.myapplication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlayerDao {
    @Insert
    fun insert(player: Player)

    @Update
    fun update(player: Player)

    @Delete
    fun delete(player: Player)

    @Query("SELECT * FROM players ORDER BY bestScore DESC")
    fun getAllPlayers(): List<Player>

    @Query("SELECT * FROM players WHERE id = :playerId")
    fun getPlayerById(playerId: Long): Player
}