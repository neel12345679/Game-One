package com.example.game.engine

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class ObstacleType {
    SPIKE_CUBE,
    MOVING_BARRIER,
    COLOR_SWAPPER,
    LASER_GATE
}

enum class PowerupType {
    COIN_CRYSTAL,
    RAINBOW_SHIELD,
    COLOR_MAGNET,
    SPEED_BOOST,
    DOUBLE_SCORE
}

data class ColorPalette3D(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val unpaintedTile: Color = Color(0xFF1F1A30),
    val backgroundStart: Color,
    val backgroundEnd: Color
)

data class GameTile3D(
    val id: Int,
    val z: Float,
    val lane: Int, // -2 to 2
    var isPainted: Boolean = false,
    val paintColor: Color
)

data class GameObstacle3D(
    val id: Int,
    var z: Float,
    var lane: Int,
    val type: ObstacleType,
    var laneDirection: Int = 1,
    var minLane: Int = -2,
    var maxLane: Int = 2
)

data class GamePowerup3D(
    val id: Int,
    val z: Float,
    val lane: Int,
    val type: PowerupType,
    var isCollected: Boolean = false
)

data class LevelData(
    val levelNumber: Int,
    val palette: ColorPalette3D,
    val baseSpeed: Float,
    val totalLength: Float,
    val tiles: List<GameTile3D>,
    val obstacles: List<GameObstacle3D>,
    val powerups: List<GamePowerup3D>,
    val trophyModel: VoxelModel
)

object LevelGenerator {

    fun generateLevel(levelNumber: Int): LevelData {
        // Deterministic pseudo-random seed based on level number
        val rng = Random(levelNumber * 10007L)

        // Generate theme palette
        val palette = generatePalette(levelNumber, rng)

        // Calculate scaling parameters for infinite levels
        val baseSpeed = 16f + kotlin.math.min(levelNumber * 0.8f, 25f)
        val totalLength = 180f + kotlin.math.min(levelNumber * 25f, 1500f)

        val tiles = mutableListOf<GameTile3D>()
        val obstacles = mutableListOf<GameObstacle3D>()
        val powerups = mutableListOf<GamePowerup3D>()

        var tileId = 1
        var obstacleId = 1
        var powerupId = 1

        val laneCount = 5 // lanes: -2, -1, 0, 1, 2
        val tileSpacing = 4f
        val totalRows = (totalLength / tileSpacing).toInt()

        for (r in 0 until totalRows) {
            val z = 20f + r * tileSpacing
            if (z >= totalLength - 20f) break // Safe finish zone

            // Generate tiles across lanes
            for (lane in -2..2) {
                // Determine paint color variant
                val tileColor = when ((lane + r) % 3) {
                    0 -> palette.primary
                    1 -> palette.secondary
                    else -> palette.accent
                }
                tiles.add(
                    GameTile3D(
                        id = tileId++,
                        z = z,
                        lane = lane,
                        isPainted = false,
                        paintColor = tileColor
                    )
                )
            }

            // Generate obstacles based on difficulty scaling
            val obstacleProb = (0.15f + kotlin.math.min(levelNumber * 0.02f, 0.45f))
            if (r > 5 && rng.nextFloat() < obstacleProb && r % 3 == 0) {
                val obsLane = rng.nextInt(-2, 3)
                val type = when (rng.nextInt(100)) {
                    in 0..40 -> ObstacleType.SPIKE_CUBE
                    in 41..70 -> ObstacleType.MOVING_BARRIER
                    in 71..85 -> ObstacleType.COLOR_SWAPPER
                    else -> ObstacleType.LASER_GATE
                }
                obstacles.add(
                    GameObstacle3D(
                        id = obstacleId++,
                        z = z,
                        lane = obsLane,
                        type = type,
                        laneDirection = if (rng.nextBoolean()) 1 else -1
                    )
                )
            }

            // Generate powerups and collectible gems
            if (rng.nextFloat() < 0.30f && r % 2 == 1) {
                val pLane = rng.nextInt(-2, 3)
                val pType = when (rng.nextInt(100)) {
                    in 0..65 -> PowerupType.COIN_CRYSTAL
                    in 66..80 -> PowerupType.COLOR_MAGNET
                    in 81..90 -> PowerupType.RAINBOW_SHIELD
                    in 91..95 -> PowerupType.SPEED_BOOST
                    else -> PowerupType.DOUBLE_SCORE
                }
                powerups.add(
                    GamePowerup3D(
                        id = powerupId++,
                        z = z,
                        lane = pLane,
                        type = pType
                    )
                )
            }
        }

        val trophyModel = VoxelModels.getModelForLevel(levelNumber)

        return LevelData(
            levelNumber = levelNumber,
            palette = palette,
            baseSpeed = baseSpeed,
            totalLength = totalLength,
            tiles = tiles,
            obstacles = obstacles,
            powerups = powerups,
            trophyModel = trophyModel
        )
    }

    private fun generatePalette(levelNumber: Int, rng: Random): ColorPalette3D {
        val themeIndex = (levelNumber - 1) % 6
        return when (themeIndex) {
            0 -> ColorPalette3D( // Cyber Neon
                primary = Color(0xFFFF2A85),
                secondary = Color(0xFF00F0FF),
                accent = Color(0xFFFFD700),
                backgroundStart = Color(0xFF0F0C1B),
                backgroundEnd = Color(0xFF1D0B36)
            )
            1 -> ColorPalette3D( // Sunset Flare
                primary = Color(0xFFFF5722),
                secondary = Color(0xFFFFC107),
                accent = Color(0xFFE91E63),
                backgroundStart = Color(0xFF1B0C13),
                backgroundEnd = Color(0xFF3B1023)
            )
            2 -> ColorPalette3D( // Emerald Matrix
                primary = Color(0xFF00E676),
                secondary = Color(0xFF18FFFF),
                accent = Color(0xFF76FF03),
                backgroundStart = Color(0xFF081812),
                backgroundEnd = Color(0xFF0E2E20)
            )
            3 -> ColorPalette3D( // Royal Gold
                primary = Color(0xFFFFD700),
                secondary = Color(0xFFFF4081),
                accent = Color(0xFF7C4DFF),
                backgroundStart = Color(0xFF181205),
                backgroundEnd = Color(0xFF302008)
            )
            4 -> ColorPalette3D( // Deep Space Plasma
                primary = Color(0xFF7B2CBF),
                secondary = Color(0xFF00F0FF),
                accent = Color(0xFFFF007F),
                backgroundStart = Color(0xFF0A0818),
                backgroundEnd = Color(0xFF1A123D)
            )
            else -> ColorPalette3D( // Arctic Crystal
                primary = Color(0xFF00E5FF),
                secondary = Color(0xFF80D8FF),
                accent = Color(0xFFB388FF),
                backgroundStart = Color(0xFF08141F),
                backgroundEnd = Color(0xFF0F263B)
            )
        }
    }
}
