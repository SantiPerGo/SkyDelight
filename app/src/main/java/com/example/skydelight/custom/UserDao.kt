package com.example.skydelight.custom

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    suspend fun getUser(): List<User>

    @Insert
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("Delete FROM user")
    suspend fun deleteUsers()

    @Query("UPDATE user SET siscoCalendar = :calendar WHERE email = :emailKey")
    suspend fun updateSiscoCalendar(emailKey: String, calendar: String)

    @Query("UPDATE user SET svqCalendar = :calendar WHERE email = :emailKey")
    suspend fun updateSvqCalendar(emailKey: String, calendar: String)

    @Query("UPDATE user SET pssCalendar = :calendar WHERE email = :emailKey")
    suspend fun updatePssCalendar(emailKey: String, calendar: String)

    @Query("UPDATE user SET svsCalendar = :calendar WHERE email = :emailKey")
    suspend fun updateSvsCalendar(emailKey: String, calendar: String)
}