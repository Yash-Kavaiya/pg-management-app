package com.example.pgmanagerpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class TenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pgId: String,
    val name: String,
    val room: String,
    val phone: String,
    val email: String,
    val status: String, // "Active", "Notice Period", "Inactive"
    val joinDate: String
)
