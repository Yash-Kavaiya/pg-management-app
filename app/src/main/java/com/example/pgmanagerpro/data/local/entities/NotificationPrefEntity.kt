package com.example.pgmanagerpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_preferences")
data class NotificationPrefEntity(
    @PrimaryKey val pgId: String,
    val emailEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val daysBefore: Int = 3,
    val sendOverdueReminders: Boolean = true,
    val reminderTime: String = "09:00"
)
