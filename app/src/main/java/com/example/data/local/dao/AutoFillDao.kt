package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AutoFillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoFillDao {

    @Query("SELECT * FROM autofill_credentials ORDER BY domain ASC, username ASC")
    fun getAllCredentials(): Flow<List<AutoFillEntity>>

    @Query("SELECT * FROM autofill_credentials WHERE domain = :domain OR domain LIKE '%' || :domain || '%'")
    suspend fun getCredentialsForDomain(domain: String): List<AutoFillEntity>

    @Query("SELECT * FROM autofill_credentials WHERE id = :id LIMIT 1")
    suspend fun getCredentialById(id: Long): AutoFillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: AutoFillEntity): Long

    @Update
    suspend fun updateCredential(credential: AutoFillEntity)

    @Delete
    suspend fun deleteCredential(credential: AutoFillEntity)

    @Query("DELETE FROM autofill_credentials WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM autofill_credentials")
    suspend fun clearAll()
}
