package com.example.pgmanagerpro.data.local.dao

import androidx.room.*
import com.example.pgmanagerpro.data.local.entities.NotificationPrefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationPrefDao {
    @Query("SELECT * FROM notification_preferences WHERE pgId = :pgId")
    fun getPreferences(pgId: String): Flow<NotificationPrefEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(pref: NotificationPrefEntity)
}
