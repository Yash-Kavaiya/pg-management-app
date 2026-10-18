package com.example.pgmanagerpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pg_properties")
data class PGEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val city: String,
    val totalRooms: Int,
    val occupiedRooms: Int,
    val monthlyRevenue: Double
)
