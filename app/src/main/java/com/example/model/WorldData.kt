package com.example.model

enum class GameMode(val displayName: String, val description: String) {
    SURVIVAL("Supervivencia", "Recolecta recursos, fabrica herramientas y sobrevive."),
    CREATIVE("Creativo", "Recursos infinitos, invulnerabilidad y vuelo libre."),
    ADVENTURE("Aventura", "Explora mazmorras y derrota al temible Void King.")
}

enum class Dimension(val id: String, val displayName: String, val skyColor: Long) {
    OVERWORLD("overworld", "Mundo Principal", 0xFF78A7FF),
    VOIDLANDS("voidlands", "The Voidlands", 0xFF140D24)
}

enum class Weather(val displayName: String) {
    CLEAR("Despejado"),
    RAIN("Lluvia"),
    VOID_STORM("Tormenta del Vacío")
}

data class PlayerStats(
    val health: Float = 20.0f,
    val maxHealth: Float = 20.0f,
    val hunger: Float = 20.0f,
    val maxHunger: Float = 20.0f,
    val armor: Int = 0,
    val experience: Int = 0,
    val level: Int = 1,
    val oxygen: Float = 20.0f,
    val maxOxygen: Float = 20.0f,
    val isDead: Boolean = false,
    val isFlying: Boolean = false,
    val isSneaking: Boolean = false
)

data class WorldSettings(
    val sensitivity: Float = 1.0f,
    val renderDistance: Int = 3, // chunk radius
    val soundVolume: Float = 0.8f,
    val musicVolume: Float = 0.5f,
    val isFogEnabled: Boolean = true,
    val invertY: Boolean = false
)

data class Vec3f(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {
    fun distanceTo(other: Vec3f): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun copy(): Vec3f = Vec3f(x, y, z)
}

data class BlockPos(
    val x: Int,
    val y: Int,
    val z: Int
)

enum class BlockFace {
    UP,
    DOWN,
    NORTH,
    SOUTH,
    EAST,
    WEST
}

data class RaycastHit(
    val blockPos: BlockPos,
    val face: BlockFace,
    val blockType: BlockType,
    val distance: Float
)
