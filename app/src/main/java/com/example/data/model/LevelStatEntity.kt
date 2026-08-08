package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_stats")
data class LevelStatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val levelNumber: Int,
    val score: Int,
    val stars: Int,
    val accuracyPercentage: Int,
    val completedAt: Long = System.currentTimeMillis()
)
