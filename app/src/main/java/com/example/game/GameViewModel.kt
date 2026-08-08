package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.model.LevelStatEntity
import com.example.data.model.UserEntity
import com.example.game.engine.Game3DRenderer
import com.example.game.engine.GameObstacle3D
import com.example.game.engine.GamePowerup3D
import com.example.game.engine.GameTile3D
import com.example.game.engine.LevelData
import com.example.game.engine.LevelGenerator
import com.example.game.engine.ObstacleType
import com.example.game.engine.PowerupType
import com.example.game.engine.Vector3D
import com.example.util.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class ScreenType {
    LOGIN,
    MAIN_MENU,
    IN_GAME,
    VOXEL_GALLERY,
    SHOP,
    LEADERBOARD
}

data class GameUiState(
    val currentUser: UserEntity? = null,
    val allUsers: List<UserEntity> = emptyList(),
    val currentScreen: ScreenType = ScreenType.LOGIN,
    val selectedLevelNumber: Int = 1,
    val levelData: LevelData? = null,
    val playerPos: Vector3D = Vector3D(0f, 1f, 0f),
    val playerTargetLane: Int = 0,
    val activePowerups: Set<PowerupType> = emptySet(),
    val score: Int = 0,
    val combo: Int = 1,
    val tilesPainted: Int = 0,
    val coinsEarnedInSession: Int = 0,
    val isGameOver: Boolean = false,
    val isLevelComplete: Boolean = false,
    val isPaused: Boolean = false,
    val isMuted: Boolean = false,
    val gameTimeSeconds: Float = 0f,
    val loginError: String? = null,
    val userLevelStats: List<LevelStatEntity> = emptyList()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = GameRepository(database.userDao(), database.levelStatDao())
    val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val renderer = Game3DRenderer()
    private var gameLoopJob: Job? = null

    init {
        observeUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            repository.allUsers.collectLatest { users ->
                _uiState.update { state ->
                    val current = if (state.currentUser != null) {
                        users.find { it.id == state.currentUser.id } ?: state.currentUser
                    } else if (users.isNotEmpty()) {
                        users.first()
                    } else null

                    state.copy(
                        allUsers = users,
                        currentUser = current,
                        currentScreen = if (current != null && state.currentScreen == ScreenType.LOGIN) ScreenType.MAIN_MENU else state.currentScreen
                    )
                }
                if (_uiState.value.currentUser != null) {
                    observeStats(_uiState.value.currentUser!!.id)
                }
            }
        }
    }

    private fun observeStats(userId: Long) {
        viewModelScope.launch {
            repository.getLevelStatsForUser(userId).collectLatest { stats ->
                _uiState.update { it.copy(userLevelStats = stats) }
            }
        }
    }

    // --- Authentication / User Profiles ---

    fun loginOrRegister(username: String, avatar: String) {
        if (username.isBlank()) {
            _uiState.update { it.copy(loginError = "Please enter a valid username") }
            return
        }

        viewModelScope.launch {
            val existing = repository.getUserByUsername(username.trim())
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        currentUser = existing,
                        currentScreen = ScreenType.MAIN_MENU,
                        loginError = null
                    )
                }
                observeStats(existing.id)
            } else {
                val newUser = UserEntity(
                    username = username.trim(),
                    avatarIcon = avatar,
                    totalCoins = 250
                )
                val newId = repository.createUser(newUser)
                val createdUser = newUser.copy(id = newId)
                _uiState.update {
                    it.copy(
                        currentUser = createdUser,
                        currentScreen = ScreenType.MAIN_MENU,
                        loginError = null
                    )
                }
                observeStats(newId)
            }
        }
    }

    fun switchUser(user: UserEntity) {
        _uiState.update {
            it.copy(
                currentUser = user,
                currentScreen = ScreenType.MAIN_MENU
            )
        }
        observeStats(user.id)
    }

    fun navigateTo(screen: ScreenType) {
        _uiState.update { it.copy(currentScreen = screen, loginError = null) }
    }

    fun selectLevelToPlay(levelNum: Int) {
        _uiState.update { it.copy(selectedLevelNumber = levelNum) }
    }

    fun toggleMute() {
        val newMute = !uiState.value.isMuted
        soundManager.setMuted(newMute)
        _uiState.update { it.copy(isMuted = newMute) }
    }

    // --- Game Logic Engine ---

    fun startLevel(levelNum: Int) {
        stopGameLoop()
        val levelData = LevelGenerator.generateLevel(levelNum)
        _uiState.update {
            it.copy(
                selectedLevelNumber = levelNum,
                levelData = levelData,
                playerPos = Vector3D(0f, 1f, 0f),
                playerTargetLane = 0,
                activePowerups = emptySet(),
                score = 0,
                combo = 1,
                tilesPainted = 0,
                coinsEarnedInSession = 0,
                isGameOver = false,
                isLevelComplete = false,
                isPaused = false,
                gameTimeSeconds = 0f,
                currentScreen = ScreenType.IN_GAME
            )
        }
        startGameLoop()
    }

    fun setTargetLaneDelta(delta: Int) {
        val currentTarget = uiState.value.playerTargetLane
        val newTarget = (currentTarget + delta).coerceIn(-2, 2)
        _uiState.update { it.copy(playerTargetLane = newTarget) }
    }

    fun setTargetLaneDirect(lane: Int) {
        val clamped = lane.coerceIn(-2, 2)
        _uiState.update { it.copy(playerTargetLane = clamped) }
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false) }
    }

    private fun startGameLoop() {
        gameLoopJob = viewModelScope.launch {
            val frameTimeMs = 16L
            val deltaTime = frameTimeMs / 1000f

            while (true) {
                delay(frameTimeMs)
                if (!_uiState.value.isPaused && !_uiState.value.isGameOver && !_uiState.value.isLevelComplete) {
                    stepGameEngine(deltaTime)
                }
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun stepGameEngine(dt: Float) {
        val currentState = _uiState.value
        val levelData = currentState.levelData ?: return

        renderer.updateParticles(dt)

        // Smooth X lane movement
        val laneWidth = 2.4f
        val targetX = currentState.playerTargetLane * laneWidth
        val newX = currentState.playerPos.x + (targetX - currentState.playerPos.x) * (dt * 18f)

        // Forward Z speed
        val speedMultiplier = if (currentState.activePowerups.contains(PowerupType.SPEED_BOOST)) 1.4f else 1.0f
        val currentSpeed = levelData.baseSpeed * speedMultiplier
        val newZ = currentState.playerPos.z + currentSpeed * dt

        val newPlayerPos = Vector3D(newX, 1.0f, newZ)

        var newScore = currentState.score
        var newCombo = currentState.combo
        var newTilesPainted = currentState.tilesPainted
        var newCoins = currentState.coinsEarnedInSession
        val currentPowerups = currentState.activePowerups.toMutableSet()

        // 1. Check Tile Painting
        val playerLane = (newX / laneWidth).let { kotlin.math.round(it).toInt() }.coerceIn(-2, 2)
        for (tile in levelData.tiles) {
            if (!tile.isPainted && tile.lane == playerLane && abs(tile.z - newZ) <= 2.2f) {
                tile.isPainted = true
                newTilesPainted++
                newCombo++
                val points = 10 * newCombo * (if (currentPowerups.contains(PowerupType.DOUBLE_SCORE)) 2 else 1)
                newScore += points

                renderer.spawnPaintSplash(Vector3D(tile.lane * laneWidth, 0.5f, tile.z), tile.paintColor)
                soundManager.playSplash()
            }
        }

        // 2. Check Powerups & Gems
        for (p in levelData.powerups) {
            if (!p.isCollected && abs(p.z - newZ) <= 2.0f && abs((p.lane * laneWidth) - newX) <= 1.8f) {
                p.isCollected = true
                when (p.type) {
                    PowerupType.COIN_CRYSTAL -> {
                        newCoins += 10
                        newScore += 50
                        soundManager.playCollectCoin()
                    }
                    PowerupType.RAINBOW_SHIELD, PowerupType.COLOR_MAGNET, PowerupType.SPEED_BOOST, PowerupType.DOUBLE_SCORE -> {
                        currentPowerups.add(p.type)
                        soundManager.playPowerup()
                    }
                }
            }
        }

        // 3. Check Obstacle Collisions
        var hitObstacle = false
        for (obs in levelData.obstacles) {
            if (abs(obs.z - newZ) <= 1.8f && abs((obs.lane * laneWidth) - newX) <= 1.5f) {
                if (currentPowerups.contains(PowerupType.RAINBOW_SHIELD)) {
                    currentPowerups.remove(PowerupType.RAINBOW_SHIELD)
                    soundManager.playObstacleHit()
                    renderer.spawnPaintSplash(newPlayerPos, levelData.palette.accent)
                } else {
                    hitObstacle = true
                    soundManager.playObstacleHit()
                }
                break
            }
        }

        if (hitObstacle) {
            _uiState.update {
                it.copy(
                    playerPos = newPlayerPos,
                    isGameOver = true
                )
            }
            return
        }

        // 4. Check Level Finish
        if (newZ >= levelData.totalLength) {
            stopGameLoop()
            soundManager.playLevelWin()

            val totalPossibleTiles = levelData.tiles.size
            val accuracy = if (totalPossibleTiles > 0) ((newTilesPainted.toFloat() / totalPossibleTiles) * 100).toInt() else 100
            val stars = when {
                accuracy >= 85 -> 3
                accuracy >= 60 -> 2
                else -> 1
            }

            val user = currentState.currentUser
            if (user != null) {
                viewModelScope.launch {
                    repository.recordLevelResult(
                        userId = user.id,
                        levelNumber = levelData.levelNumber,
                        score = newScore,
                        stars = stars,
                        accuracy = accuracy,
                        tilesColored = newTilesPainted.toLong(),
                        coinsEarned = newCoins + 50
                    )
                }
            }

            _uiState.update {
                it.copy(
                    playerPos = newPlayerPos,
                    score = newScore,
                    tilesPainted = newTilesPainted,
                    coinsEarnedInSession = newCoins + 50,
                    isLevelComplete = true
                )
            }
            return
        }

        // Update State
        _uiState.update {
            it.copy(
                playerPos = newPlayerPos,
                score = newScore,
                combo = newCombo,
                tilesPainted = newTilesPainted,
                coinsEarnedInSession = newCoins,
                activePowerups = currentPowerups,
                gameTimeSeconds = it.gameTimeSeconds + dt
            )
        }
    }

    fun reviveWithCoins() {
        val user = uiState.value.currentUser ?: return
        val cost = 50
        if (user.totalCoins >= cost) {
            viewModelScope.launch {
                repository.updateUser(user.copy(totalCoins = user.totalCoins - cost))
                _uiState.update {
                    it.copy(
                        isGameOver = false,
                        activePowerups = setOf(PowerupType.RAINBOW_SHIELD)
                    )
                }
                startGameLoop()
            }
        }
    }

    fun unlockSkin(skinId: String, cost: Int) {
        val user = uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.unlockSkin(user.id, skinId, cost)
        }
    }

    fun selectSkin(skinId: String) {
        val user = uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.selectSkin(user.id, skinId)
        }
    }
}
