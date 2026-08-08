package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    val color: Color,
    var alpha: Float = 1f,
    var size: Float = 12f
)

class Game3DRenderer {
    val camera = Camera3D()
    private val particles = mutableListOf<Particle3D>()

    fun updateParticles(deltaTime: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * deltaTime
            p.y += p.vy * deltaTime
            p.z += p.vz * deltaTime
            p.alpha -= deltaTime * 1.5f
            if (p.alpha <= 0f) {
                iterator.remove()
            }
        }
    }

    fun spawnPaintSplash(position: Vector3D, color: Color) {
        for (i in 0..12) {
            val angle = (i * 30) * Math.PI / 180.0
            val speed = 4f + (i % 3) * 2f
            particles.add(
                Particle3D(
                    x = position.x,
                    y = position.y + 0.5f,
                    z = position.z,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (2f + (i % 4) * 2f),
                    vz = (sin(angle) * speed).toFloat(),
                    color = color,
                    alpha = 1f,
                    size = 14f
                )
            )
        }
    }

    fun renderScene(
        drawScope: DrawScope,
        screenWidth: Float,
        screenHeight: Float,
        playerPos: Vector3D,
        playerSkin: String,
        activePowerups: Set<PowerupType>,
        levelData: LevelData,
        gameTimeSeconds: Float
    ) {
        // Update camera position to follow player smoothly
        camera.position = Vector3D(
            x = playerPos.x * 0.4f,
            y = playerPos.y + 4.2f,
            z = playerPos.z - 11.5f
        )

        // 1. Draw 3D Background Gradient
        val bgBrush = Brush.verticalGradient(
            colors = listOf(levelData.palette.backgroundStart, levelData.palette.backgroundEnd)
        )
        drawScope.drawRect(brush = bgBrush, size = Size(screenWidth, screenHeight))

        // 2. Draw Distance Horizon Grid Glow
        val horizonProj = camera.project(Vector3D(0f, 0f, playerPos.z + 120f), screenWidth, screenHeight)
        if (horizonProj.isVisible) {
            drawScope.drawCircle(
                color = levelData.palette.primary.copy(alpha = 0.25f),
                radius = 280f * (horizonProj.scale / 100f).coerceIn(0.2f, 2f),
                center = horizonProj.offset
            )
        }

        // 3. Render 3D Track Tiles sorted by Z depth (far to near)
        val visibleTiles = levelData.tiles.filter {
            it.z >= playerPos.z - 15f && it.z <= playerPos.z + 100f
        }.sortedByDescending { it.z }

        val laneWidth = 2.4f
        val tileDepth = 3.6f

        for (tile in visibleTiles) {
            val tileX = tile.lane * laneWidth
            val p0 = camera.project(Vector3D(tileX - laneWidth / 2f, 0f, tile.z), screenWidth, screenHeight)
            val p1 = camera.project(Vector3D(tileX + laneWidth / 2f, 0f, tile.z), screenWidth, screenHeight)
            val p2 = camera.project(Vector3D(tileX + laneWidth / 2f, 0f, tile.z + tileDepth), screenWidth, screenHeight)
            val p3 = camera.project(Vector3D(tileX - laneWidth / 2f, 0f, tile.z + tileDepth), screenWidth, screenHeight)

            if (p0.isVisible || p1.isVisible || p2.isVisible || p3.isVisible) {
                val path = Path().apply {
                    moveTo(p0.offset.x, p0.offset.y)
                    lineTo(p1.offset.x, p1.offset.y)
                    lineTo(p2.offset.x, p2.offset.y)
                    lineTo(p3.offset.x, p3.offset.y)
                    close()
                }

                val tileColor = if (tile.isPainted) tile.paintColor else levelData.palette.unpaintedTile
                val alpha = ((100f - (tile.z - playerPos.z)) / 100f).coerceIn(0.15f, 1f)

                drawScope.drawPath(path = path, color = tileColor.copy(alpha = alpha))

                // Tile border grid
                val borderStrokeColor = if (tile.isPainted) Color.White.copy(alpha = 0.8f * alpha) else levelData.palette.primary.copy(alpha = 0.35f * alpha)
                drawScope.drawPath(
                    path = path,
                    color = borderStrokeColor,
                    style = Stroke(width = if (tile.isPainted) 3f else 1.5f)
                )
            }
        }

        // 4. Render Powerups & Collectibles
        val visiblePowerups = levelData.powerups.filter {
            !it.isCollected && it.z >= playerPos.z - 10f && it.z <= playerPos.z + 90f
        }.sortedByDescending { it.z }

        for (p in visiblePowerups) {
            val px = p.lane * laneWidth
            val py = 1.2f + sin((gameTimeSeconds * 4f + p.id).toDouble()).toFloat() * 0.4f
            val proj = camera.project(Vector3D(px, py, p.z), screenWidth, screenHeight)

            if (proj.isVisible && proj.scale > 0f) {
                val size = 28f * (proj.scale / 50f).coerceIn(0.3f, 2.5f)
                val pColor = when (p.type) {
                    PowerupType.COIN_CRYSTAL -> levelData.palette.accent
                    PowerupType.RAINBOW_SHIELD -> Color(0xFF00F0FF)
                    PowerupType.COLOR_MAGNET -> Color(0xFFFF2A85)
                    PowerupType.SPEED_BOOST -> Color(0xFF00FF66)
                    PowerupType.DOUBLE_SCORE -> Color(0xFF9C27B0)
                }

                drawScope.drawCircle(color = pColor, radius = size, center = proj.offset)
                drawScope.drawCircle(color = Color.White, radius = size * 0.4f, center = proj.offset)
            }
        }

        // 5. Render 3D Obstacles
        val visibleObstacles = levelData.obstacles.filter {
            it.z >= playerPos.z - 10f && it.z <= playerPos.z + 90f
        }.sortedByDescending { it.z }

        for (obs in visibleObstacles) {
            val ox = obs.lane * laneWidth
            val oy = 1.0f
            val proj = camera.project(Vector3D(ox, oy, obs.z), screenWidth, screenHeight)

            if (proj.isVisible && proj.scale > 0f) {
                val size = 32f * (proj.scale / 50f).coerceIn(0.3f, 2.5f)
                when (obs.type) {
                    ObstacleType.SPIKE_CUBE -> {
                        drawScope.drawRect(
                            color = Color(0xFFFF1744),
                            topLeft = Offset(proj.offset.x - size, proj.offset.y - size),
                            size = Size(size * 2, size * 2)
                        )
                        drawScope.drawRect(
                            color = Color.White,
                            topLeft = Offset(proj.offset.x - size * 0.5f, proj.offset.y - size * 0.5f),
                            size = Size(size, size)
                        )
                    }
                    ObstacleType.MOVING_BARRIER -> {
                        drawScope.drawRect(
                            color = Color(0xFFFF9100),
                            topLeft = Offset(proj.offset.x - size * 1.5f, proj.offset.y - size * 0.8f),
                            size = Size(size * 3f, size * 1.6f)
                        )
                    }
                    ObstacleType.COLOR_SWAPPER -> {
                        drawScope.drawCircle(
                            color = Color(0xFFD500F9),
                            radius = size * 1.3f,
                            center = proj.offset,
                            style = Stroke(width = 6f)
                        )
                    }
                    ObstacleType.LASER_GATE -> {
                        drawScope.drawLine(
                            color = Color(0xFF00E5FF),
                            start = Offset(proj.offset.x - size * 2, proj.offset.y),
                            end = Offset(proj.offset.x + size * 2, proj.offset.y),
                            strokeWidth = 8f
                        )
                    }
                }
            }
        }

        // 6. Render Player 3D Sphere / Skin
        val playerProj = camera.project(playerPos, screenWidth, screenHeight)
        if (playerProj.isVisible && playerProj.scale > 0f) {
            val radius = 34f * (playerProj.scale / 50f).coerceIn(0.5f, 2.8f)

            // Shield Orbit Ring if shield active
            if (activePowerups.contains(PowerupType.RAINBOW_SHIELD)) {
                drawScope.drawCircle(
                    color = Color(0xFF00F0FF),
                    radius = radius * 1.6f,
                    center = playerProj.offset,
                    style = Stroke(width = 6f)
                )
            }

            val mainColor = when (playerSkin) {
                "skin_flame_orb" -> Color(0xFFFF5722)
                "skin_plasma_cube" -> Color(0xFF9C27B0)
                "skin_rainbow_star" -> Color(0xFFFFD700)
                "skin_diamond" -> Color(0xFF00E5FF)
                else -> levelData.palette.primary
            }

            // Outer Glow
            drawScope.drawCircle(
                color = mainColor.copy(alpha = 0.4f),
                radius = radius * 1.35f,
                center = playerProj.offset
            )

            // Core Sphere Body
            val ballBrush = Brush.radialGradient(
                colors = listOf(Color.White, mainColor, Color.Black),
                center = Offset(playerProj.offset.x - radius * 0.3f, playerProj.offset.y - radius * 0.3f),
                radius = radius * 1.2f
            )
            drawScope.drawCircle(
                brush = ballBrush,
                radius = radius,
                center = playerProj.offset
            )

            // Specular Highlight Spot
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = radius * 0.3f,
                center = Offset(playerProj.offset.x - radius * 0.35f, playerProj.offset.y - radius * 0.35f)
            )
        }

        // 7. Render 3D Particles
        for (p in particles) {
            val proj = camera.project(Vector3D(p.x, p.y, p.z), screenWidth, screenHeight)
            if (proj.isVisible && proj.scale > 0f) {
                val pSize = p.size * (proj.scale / 50f).coerceIn(0.2f, 2f)
                drawScope.drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = pSize,
                    center = proj.offset
                )
            }
        }

        // 8. End Trophy Voxel Sculpture in Distance
        if (playerPos.z >= levelData.totalLength - 120f) {
            val trophyZ = levelData.totalLength + 20f
            val trophyModel = levelData.trophyModel
            val rotAngle = gameTimeSeconds * 1.5f

            for (cube in trophyModel.cubes) {
                val rad = rotAngle
                val rx = cube.x * cos(rad) - cube.z * sin(rad)
                val rz = cube.x * sin(rad) + cube.z * cos(rad)
                val ry = cube.y + 4.0f

                val proj = camera.project(
                    Vector3D(rx.toFloat() * 1.8f, ry.toFloat() * 1.8f, trophyZ + rz.toFloat() * 1.8f),
                    screenWidth,
                    screenHeight
                )

                if (proj.isVisible && proj.scale > 0f) {
                    val cubeSize = 16f * (proj.scale / 50f).coerceIn(0.2f, 3f)
                    drawScope.drawRect(
                        color = cube.color,
                        topLeft = Offset(proj.offset.x - cubeSize / 2f, proj.offset.y - cubeSize / 2f),
                        size = Size(cubeSize, cubeSize)
                    )
                }
            }
        }
    }
}
