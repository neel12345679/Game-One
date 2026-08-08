package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String = "",
    val avatarIcon: String = "avatar_cyber_1",
    val currentLevel: Int = 1,
    val maxLevelReached: Int = 1,
    val highScore: Int = 0,
    val totalCoins: Int = 100,
    val totalTilesColored: Long = 0,
    val activeSkin: String = "skin_cyber_ball",
    val unlockedSkins: String = "skin_cyber_ball", // comma separated
    val createdAt: Long = System.currentTimeMillis()
)
