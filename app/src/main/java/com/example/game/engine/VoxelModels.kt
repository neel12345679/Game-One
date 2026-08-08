package com.example.game.engine

import androidx.compose.ui.graphics.Color

data class VoxelCube(
    val x: Int,
    val y: Int,
    val z: Int,
    val color: Color
)

data class VoxelModel(
    val id: String,
    val name: String,
    val cubes: List<VoxelCube>
)

object VoxelModels {

    fun getModelForLevel(levelNumber: Int): VoxelModel {
        val type = (levelNumber - 1) % 6
        return when (type) {
            0 -> createCrystalStar(levelNumber)
            1 -> createCrownTrophy(levelNumber)
            2 -> createDragonMech(levelNumber)
            3 -> createGemstonePrism(levelNumber)
            4 -> createCyberRobot(levelNumber)
            else -> createInfinityRing(levelNumber)
        }
    }

    private fun createCrystalStar(level: Int): VoxelModel {
        val cubes = mutableListOf<VoxelCube>()
        val gold = Color(0xFFFFD700)
        val cyan = Color(0xFF00F0FF)
        val magenta = Color(0xFFFF2A85)

        // Center core
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    cubes.add(VoxelCube(x, y, z, gold))
                }
            }
        }
        // Points
        for (i in 2..4) {
            cubes.add(VoxelCube(i, 0, 0, cyan))
            cubes.add(VoxelCube(-i, 0, 0, cyan))
            cubes.add(VoxelCube(0, i, 0, magenta))
            cubes.add(VoxelCube(0, -i, 0, magenta))
            cubes.add(VoxelCube(0, 0, i, cyan))
            cubes.add(VoxelCube(0, 0, -i, cyan))
        }

        return VoxelModel("star_$level", "3D Star Prism Lvl $level", cubes)
    }

    private fun createCrownTrophy(level: Int): VoxelModel {
        val cubes = mutableListOf<VoxelCube>()
        val gold = Color(0xFFFFC107)
        val ruby = Color(0xFFE91E63)
        val emerald = Color(0xFF00E676)

        // Base ring
        for (x in -2..2) {
            for (z in -2..2) {
                if (x == -2 || x == 2 || z == -2 || z == 2) {
                    cubes.add(VoxelCube(x, 0, z, gold))
                }
            }
        }
        // Spikes
        val spikePositions = listOf(-2 to -2, -2 to 2, 2 to -2, 2 to 2, 0 to 2, 0 to -2)
        for ((sx, sz) in spikePositions) {
            cubes.add(VoxelCube(sx, 1, sz, gold))
            cubes.add(VoxelCube(sx, 2, sz, ruby))
            cubes.add(VoxelCube(sx, 3, sz, emerald))
        }

        return VoxelModel("crown_$level", "Imperial Crown Lvl $level", cubes)
    }

    private fun createDragonMech(level: Int): VoxelModel {
        val cubes = mutableListOf<VoxelCube>()
        val cyan = Color(0xFF00F0FF)
        val purple = Color(0xFF9C27B0)

        // Body
        for (z in -3..3) {
            cubes.add(VoxelCube(0, 0, z, cyan))
            cubes.add(VoxelCube(0, 1, z, purple))
        }
        // Wings
        for (w in 1..3) {
            cubes.add(VoxelCube(w, 1, 0, cyan))
            cubes.add(VoxelCube(-w, 1, 0, cyan))
            cubes.add(VoxelCube(w + 1, 2, 0, purple))
            cubes.add(VoxelCube(-(w + 1), 2, 0, purple))
        }
        // Head
        cubes.add(VoxelCube(0, 2, 3, Color.Yellow))
        cubes.add(VoxelCube(0, 3, 3, Color.Red))

        return VoxelModel("dragon_$level", "Cyber Dragon Lvl $level", cubes)
    }

    private fun createGemstonePrism(level: Int): VoxelModel {
        val cubes = mutableListOf<VoxelCube>()
        val violet = Color(0xFF7B2CBF)
        val cyan = Color(0xFF00F0FF)

        val layers = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 2, 4 to 1)
        for ((y, radius) in layers) {
            for (x in -radius..radius) {
                for (z in -radius..radius) {
                    if (kotlin.math.abs(x) + kotlin.math.abs(z) <= radius) {
                        cubes.add(VoxelCube(x, y - 2, z, if (y % 2 == 0) violet else cyan))
                    }
                }
            }
        }

        return VoxelModel("gem_$level", "Plasma Gem Lvl $level", cubes)
    }

    private fun createCyberRobot(level: Int): VoxelModel {
        val cubes = mutableListOf<VoxelCube>()
        val blue = Color(0xFF2196F3)
        val green = Color(0xFF00E676)

        // Feet
        cubes.add(VoxelCube(-1, -2, 0, blue))
        cubes.add(VoxelCube(1, -2, 0, blue))
        // Legs
        cubes.add(VoxelCube(-1, -1, 0, blue))
        cubes.add(VoxelCube(1, -1, 0, blue))
        // Torso
        for (x in -1..1) {
            for (y in 0..1) {
                cubes.add(VoxelCube(x, y, 0, green))
            }
        }
        // Head
        cubes.add(VoxelCube(0, 2, 0, blue))
        cubes.add(VoxelCube(0, 3, 0, Color.Yellow)) // Visor

        return VoxelModel("robot_$level", "Cyber Bot Lvl $level", cubes)
    }

    private fun createInfinityRing(level: Int): VoxelModel {
        val cubes = mutableListOf<VoxelCube>()
        val count = 16
        for (i in 0 until count) {
            val angle = 2.0 * Math.PI * i / count
            val rx = (kotlin.math.sin(angle) * 3.5).toInt()
            val rz = (kotlin.math.cos(angle) * 3.5).toInt()
            val ry = (kotlin.math.sin(angle * 2) * 1.5).toInt()

            val c = when (i % 3) {
                0 -> Color(0xFFFF2A85)
                1 -> Color(0xFF00F0FF)
                else -> Color(0xFFFFD700)
            }
            cubes.add(VoxelCube(rx, ry, rz, c))
        }

        return VoxelModel("infinity_$level", "Infinity Ring Lvl $level", cubes)
    }
}
