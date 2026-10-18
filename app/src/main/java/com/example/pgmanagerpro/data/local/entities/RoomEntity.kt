package com.example.pgmanagerpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pgId: String,
    val number: String,
    val type: String, // "Single", "Double", "Triple"
    val rent: Double,
    val deposit: Double,
    val status: String, // "Occupied", "Available", "Maintenance"
    val floor: Int
)
