package com.example.pgmanagerpro.data.local.dao

import androidx.room.*
import com.example.pgmanagerpro.data.local.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE pgId = :pgId ORDER BY dueDate DESC")
    fun getPaymentsByPG(pgId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun deletePayment(paymentId: Int)
}
