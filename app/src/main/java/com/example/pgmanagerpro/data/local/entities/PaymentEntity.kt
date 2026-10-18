package com.example.pgmanagerpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pgId: String,
    val tenant: String,
    val tenantId: Int = 0,
    val room: String,
    val amount: Double,
    val dueDate: String,
    val status: String, // "Paid", "Pending", "Overdue", "Partial"
    val paidDate: String? = null,
    val paymentMethod: String = "UPI", // "Cash", "UPI", "Bank Transfer", "Card", "Cheque", "Online"
    val paymentType: String = "Rent", // "Rent", "Security Deposit", "Advance", "Maintenance", "Electricity", "Water", "Other"
    val receiptNumber: String = "",
    val transactionId: String = "",
    val notes: String = "",
    val lateFee: Double = 0.0,
    val discount: Double = 0.0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
