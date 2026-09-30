package com.example.model

enum class CreatureType(
    val displayName: String,
    val isHostile: Boolean,
    val maxHealth: Float,
    val damage: Float,
    val moveSpeed: Float,
    val dimension: Dimension,
    val isBoss: Boolean = false,
    val primaryColor: Long,
    val secondaryColor: Long
) {
    // Passive Overworld
    WOOLOC("Wooloc", false, 10f, 0f, 0.05f, Dimension.OVERWORLD, false, 0xFFE0E0E0, 0xFF5D4037),
    BOARLING("Boarling", false, 12f, 0f, 0.06f, Dimension.OVERWORLD, false, 0xFFF48FB1, 0xFF8D6E63),
    GLOWHOPPER("Glowhopper", false, 6f, 0f, 0.07f, Dimension.OVERWORLD, false, 0xFF69F0AE, 0xFF00E676),

    // Hostile Overworld
    GLOOMSTALKER("Gloomstalker", true, 20f, 4f, 0.065f, Dimension.OVERWORLD, false, 0xFF212121, 0xFFD50000),
    CREEPLING("Creepling", true, 16f, 15f, 0.055f, Dimension.OVERWORLD, false, 0xFF2E7D32, 0xFF1B5E20),
    BONEGUARD("Boneguard", true, 18f, 3.5f, 0.05f, Dimension.OVERWORLD, false, 0xFFECEFF1, 0xFF78909C),
    DUSKSPIDER("Duskspider", true, 14f, 3f, 0.08f, Dimension.OVERWORLD, false, 0xFF37474F, 0xFFFF1744),

    // The Voidlands Creatures
    VOIDLING("Voidling", true, 25f, 5f, 0.07f, Dimension.VOIDLANDS, false, 0xFF7C4DFF, 0xFFE040FB),
    ABYSS_GOLEM("Abyss Golem", true, 60f, 8f, 0.04f, Dimension.VOIDLANDS, false, 0xFF311B92, 0xFF00E5FF),

    // End Update Final Boss
    VOID_KING("Void King", true, 300f, 12f, 0.06f, Dimension.VOIDLANDS, true, 0xFF4A148C, 0xFFFFD700)
}

data class CreatureEntity(
    val id: String,
    val type: CreatureType,
    var x: Float,
    var y: Float,
    var z: Float,
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var health: Float = type.maxHealth,
    var isHurt: Boolean = false,
    var hurtTimer: Float = 0f,
    var attackCooldown: Float = 0f,
    var stateTimer: Float = 0f,
    var bossPhase: Int = 1,
    var isDead: Boolean = false,
    var fuseTimer: Float = 0f // For Creepling explosion
) {
    fun takeDamage(amount: Float): Boolean {
        if (isDead) return false
        health -= amount
        isHurt = true
        hurtTimer = 0.3f
        if (health <= 0) {
            health = 0f
            isDead = true
            return true
        }
        return false
    }
}

data class Projectile(
    val id: String,
    var x: Float,
    var y: Float,
    var z: Float,
    val vx: Float,
    val vy: Float,
    val vz: Float,
    val isFromPlayer: Boolean,
    val damage: Float,
    val color: Long,
    var lifeTime: Float = 4.0f
)
