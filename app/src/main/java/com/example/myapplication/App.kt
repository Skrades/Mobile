package com.example.myapplication

import android.app.Application
import androidx.room.Room

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        instance_ = this
        database_ = Room.databaseBuilder(
                applicationContext,
                PlayerDatabase::class.java, "database.db"
        ).fallbackToDestructiveMigration(false).build()
    }
    companion object {
        var instance_: App? = null
        var database_: PlayerDatabase? = null
        fun getInstance(): App? {
            return instance_
        }
    }
    fun getDataBase(): PlayerDatabase? {
        return database_
    }
}