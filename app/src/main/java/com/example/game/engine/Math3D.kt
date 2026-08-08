package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3D(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3D(x * scalar, y * scalar, z * scalar)

    fun dot(other: Vector3D): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3D): Vector3D = Vector3D(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vector3D {
        val len = length()
        return if (len > 0f) Vector3D(x / len, y / len, z / len) else Vector3D(0f, 0f, 0f)
    }
}

data class Projected2D(
    val offset: Offset,
    val scale: Float,
    val depthZ: Float,
    val isVisible: Boolean
)

class Camera3D(
    var position: Vector3D = Vector3D(0f, 4f, -12f),
    var rotationX: Float = 0.25f, // Pitch down slightly
    var rotationY: Float = 0f,
    var focalLength: Float = 550f
) {
    fun project(point: Vector3D, screenWidth: Float, screenHeight: Float): Projected2D {
        // Translate relative to camera
        val relX = point.x - position.x
        val relY = point.y - position.y
        val relZ = point.z - position.z

        // Rotate Y (Yaw)
        val cosY = cos(rotationY)
        val sinY = sin(rotationY)
        val x1 = relX * cosY + relZ * sinY
        val z1 = -relX * sinY + relZ * cosY

        // Rotate X (Pitch)
        val cosX = cos(rotationX)
        val sinX = sin(rotationX)
        val y2 = relY * cosX - z1 * sinX
        val z2 = relY * sinX + z1 * cosX

        if (z2 <= 0.5f) {
            return Projected2D(Offset.Zero, 0f, z2, false)
        }

        val scale = focalLength / z2
        val projX = screenWidth / 2f + x1 * scale
        val projY = screenHeight / 2f - y2 * scale

        return Projected2D(
            offset = Offset(projX, projY),
            scale = scale,
            depthZ = z2,
            isVisible = projX in -200f..(screenWidth + 200f) && projY in -200f..(screenHeight + 200f)
        )
    }
}

object Lighting3D {
    val lightDirection = Vector3D(0.5f, 1f, -0.8f).normalize()

    fun calculateShadedColor(baseColor: Color, normal: Vector3D, ambient: Float = 0.35f): Color {
        val n = normal.normalize()
        val intensity = (n.dot(lightDirection)).coerceIn(0f, 1f)
        val factor = ambient + (1f - ambient) * intensity

        return Color(
            red = (baseColor.red * factor).coerceIn(0f, 1f),
            green = (baseColor.green * factor).coerceIn(0f, 1f),
            blue = (baseColor.blue * factor).coerceIn(0f, 1f),
            alpha = baseColor.alpha
        )
    }
}
