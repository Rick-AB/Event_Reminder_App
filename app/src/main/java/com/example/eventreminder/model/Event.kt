package com.example.eventreminder.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "Event_Title") var title: String,
    @ColumnInfo(name = "Event_Date") var date: String
) : Serializable