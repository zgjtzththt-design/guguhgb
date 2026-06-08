package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_progress WHERE id = 1 LIMIT 1")
    fun getPlayerProgress(): Flow<PlayerProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerProgress(progress: PlayerProgress)

    @Query("SELECT * FROM created_phones ORDER BY timestamp DESC")
    fun getAllCreatedPhones(): Flow<List<CreatedPhone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreatedPhone(phone: CreatedPhone)

    @Query("DELETE FROM created_phones")
    suspend fun clearCreatedPhones()

    @Query("DELETE FROM player_progress")
    suspend fun clearPlayerProgress()
}
