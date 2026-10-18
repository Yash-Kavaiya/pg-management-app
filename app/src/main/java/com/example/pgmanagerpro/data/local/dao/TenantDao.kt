package com.example.pgmanagerpro.data.local.dao

import androidx.room.*
import com.example.pgmanagerpro.data.local.entities.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE pgId = :pgId ORDER BY name ASC")
    fun getTenantsByPG(pgId: String): Flow<List<TenantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: TenantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTenants(tenants: List<TenantEntity>)

    @Update
    suspend fun updateTenant(tenant: TenantEntity)

    @Query("DELETE FROM tenants WHERE id = :tenantId")
    suspend fun deleteTenant(tenantId: Int)
}
