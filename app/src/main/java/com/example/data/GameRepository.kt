package com.example.data

import com.example.data.dao.LevelStatDao
import com.example.data.dao.UserDao
import com.example.data.model.LevelStatEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val userDao: UserDao,
    private val levelStatDao: LevelStatDao
) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun getUserByUsername(username: String): UserEntity? = userDao.getUserByUsername(username)

    suspend fun createUser(user: UserEntity): Long = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(userId: Long) = userDao.deleteUser(userId)

    fun getLevelStatsForUser(userId: Long): Flow<List<LevelStatEntity>> = levelStatDao.getStatsForUser(userId)

    suspend fun recordLevelResult(
        userId: Long,
        levelNumber: Int,
        score: Int,
        stars: Int,
        accuracy: Int,
        tilesColored: Long,
        coinsEarned: Int
    ) {
        val currentUser = userDao.getUserById(userId) ?: return
        
        // Update level stat
        val existingStat = levelStatDao.getStatForLevel(userId, levelNumber)
        val newScore = if (existingStat != null) maxOf(existingStat.score, score) else score
        val newStars = if (existingStat != null) maxOf(existingStat.stars, stars) else stars
        val newAccuracy = if (existingStat != null) maxOf(existingStat.accuracyPercentage, accuracy) else accuracy

        levelStatDao.insertOrUpdateStat(
            LevelStatEntity(
                id = existingStat?.id ?: 0,
                userId = userId,
                levelNumber = levelNumber,
                score = newScore,
                stars = newStars,
                accuracyPercentage = newAccuracy
            )
        )

        // Update user progression
        val newMaxLevel = maxOf(currentUser.maxLevelReached, levelNumber + 1)
        val nextLevel = maxOf(currentUser.currentLevel, levelNumber + 1)
        val newHighScore = maxOf(currentUser.highScore, score)
        val updatedCoins = currentUser.totalCoins + coinsEarned
        val updatedTiles = currentUser.totalTilesColored + tilesColored

        userDao.updateUser(
            currentUser.copy(
                currentLevel = nextLevel,
                maxLevelReached = newMaxLevel,
                highScore = newHighScore,
                totalCoins = updatedCoins,
                totalTilesColored = updatedTiles
            )
        )
    }

    suspend fun unlockSkin(userId: Long, skinId: String, cost: Int): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        if (user.totalCoins < cost) return false

        val currentUnlocked = user.unlockedSkins.split(",").toMutableSet()
        currentUnlocked.add(skinId)

        userDao.updateUser(
            user.copy(
                totalCoins = user.totalCoins - cost,
                unlockedSkins = currentUnlocked.joinToString(","),
                activeSkin = skinId
            )
        )
        return true
    }

    suspend fun selectSkin(userId: Long, skinId: String) {
        val user = userDao.getUserById(userId) ?: return
        if (user.unlockedSkins.split(",").contains(skinId)) {
            userDao.updateUser(user.copy(activeSkin = skinId))
        }
    }
}
