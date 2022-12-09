package com.example.skydelight.custom

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}