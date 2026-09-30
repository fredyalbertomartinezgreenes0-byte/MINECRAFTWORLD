package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "saved_worlds")
data class WorldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val seed: Long,
    val gameMode: String = "SURVIVAL",
    val dimension: String = "overworld",
    val playerX: Float = 0f,
    val playerY: Float = 20f,
    val playerZ: Float = 0f,
    val playerPitch: Float = 0f,
    val playerYaw: Float = 0f,
    val health: Float = 20f,
    val hunger: Float = 20f,
    val timeOfDay: Float = 0.2f, // morning
    val bossDefeated: Boolean = false,
    val lastPlayed: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "block_deltas",
    indices = [Index(value = ["worldId", "dimension", "x", "y", "z"], unique = true)]
)
data class BlockDeltaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val worldId: Long,
    val dimension: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val blockId: Int
)

@Entity(
    tableName = "inventory_slots",
    indices = [Index(value = ["worldId", "slotIndex"], unique = true)]
)
data class InventorySlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val worldId: Long,
    val slotIndex: Int,
    val itemId: String,
    val count: Int,
    val durability: Int
)
