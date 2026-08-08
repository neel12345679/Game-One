package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LevelStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelStatDao {
    @Query("SELECT * FROM level_stats WHERE userId = :userId ORDER BY levelNumber ASC")
    fun getStatsForUser(userId: Long): Flow<List<LevelStatEntity>>

    @Query("SELECT * FROM level_stats WHERE userId = :userId AND levelNumber = :levelNumber LIMIT 1")
    suspend fun getStatForLevel(userId: Long, levelNumber: Int): LevelStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStat(stat: LevelStatEntity): Long
}
