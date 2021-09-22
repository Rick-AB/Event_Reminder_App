package com.example.eventreminder.database

import androidx.room.*
import com.example.eventreminder.model.Event

@Dao
interface DAO {
    @Query("SELECT * FROM event")
    fun getAllEvents(): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)
}