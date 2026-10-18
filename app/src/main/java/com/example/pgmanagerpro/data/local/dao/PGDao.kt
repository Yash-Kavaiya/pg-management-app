package com.example.pgmanagerpro.data.local.dao

import androidx.room.*
import com.example.pgmanagerpro.data.local.entities.PGEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PGDao {
    @Query("SELECT * FROM pg_properties")
    fun getAllPGs(): Flow<List<PGEntity>>

    @Query("SELECT COUNT(*) FROM pg_properties")
    suspend fun getPGCount(): Int

    @Query("SELECT * FROM pg_properties WHERE id = :pgId")
    fun getPGById(pgId: String): Flow<PGEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPG(pg: PGEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPGs(pgs: List<PGEntity>)

    @Update
    suspend fun updatePG(pg: PGEntity)

    @Delete
    suspend fun deletePG(pg: PGEntity)
}
