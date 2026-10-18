package com.example.pgmanagerpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pgId: String,
    val room: String,
    val tenant: String,
    val email: String = "",
    val phone: String = "",
    val startDate: String,
    val endDate: String,
    val status: String, // "Active", "Upcoming", "Completed", "Cancelled", "Pending"
    val monthlyRent: Double,
    val securityDeposit: Double,
    val advance: Double = 0.0,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val emergencyContactRelation: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val notes: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
