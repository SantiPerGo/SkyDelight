package com.example.skydelight.custom

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val email: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "sex") var sex: String,
    @ColumnInfo(name = "age") var age: Int,
    @ColumnInfo(name = "token") var token: String,
    @ColumnInfo(name = "refresh") var refresh: String,
    @ColumnInfo(name = "session") var session: Boolean = true,
    @ColumnInfo(name = "initialTest") var initialTest: Boolean = false,
    @ColumnInfo(name = "advice") var advice: Boolean = true,
    @ColumnInfo(name = "siscoCalendar") var siscoCalendar: String? = null,
    @ColumnInfo(name = "svqCalendar") var svqCalendar: String? = null,
    @ColumnInfo(name = "pssCalendar") var pssCalendar: String? = null,
    @ColumnInfo(name = "svsCalendar") var svsCalendar: String? = null
)