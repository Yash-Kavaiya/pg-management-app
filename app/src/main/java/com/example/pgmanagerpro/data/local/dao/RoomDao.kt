package com.example.pgmanagerpro.data.local.dao

import androidx.room.*
import com.example.pgmanagerpro.data.local.entities.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE pgId = :pgId ORDER BY floor ASC, number ASC")
    fun getRoomsByPG(pgId: String): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRooms(rooms: List<RoomEntity>)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: Int)
}
