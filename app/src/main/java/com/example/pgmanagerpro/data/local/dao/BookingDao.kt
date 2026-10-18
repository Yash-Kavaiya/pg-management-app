package com.example.pgmanagerpro.data.local.dao

import androidx.room.*
import com.example.pgmanagerpro.data.local.entities.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE pgId = :pgId ORDER BY startDate DESC")
    fun getBookingsByPG(pgId: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookings(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :bookingId")
    suspend fun deleteBooking(bookingId: Int)
}
