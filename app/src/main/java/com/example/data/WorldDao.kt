package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldDao {
    @Query("SELECT * FROM saved_worlds ORDER BY lastPlayed DESC")
    fun getAllWorlds(): Flow<List<WorldEntity>>

    @Query("SELECT * FROM saved_worlds WHERE id = :worldId LIMIT 1")
    suspend fun getWorldById(worldId: Long): WorldEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorld(world: WorldEntity): Long

    @Update
    suspend fun updateWorld(world: WorldEntity)

    @Query("DELETE FROM saved_worlds WHERE id = :worldId")
    suspend fun deleteWorld(worldId: Long)

    @Query("DELETE FROM block_deltas WHERE worldId = :worldId")
    suspend fun deleteBlockDeltas(worldId: Long)

    @Query("DELETE FROM inventory_slots WHERE worldId = :worldId")
    suspend fun deleteInventorySlots(worldId: Long)

    // Block deltas
    @Query("SELECT * FROM block_deltas WHERE worldId = :worldId AND dimension = :dimension")
    suspend fun getBlockDeltas(worldId: Long, dimension: String): List<BlockDeltaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockDelta(delta: BlockDeltaEntity)

    // Inventory
    @Query("SELECT * FROM inventory_slots WHERE worldId = :worldId ORDER BY slotIndex ASC")
    suspend fun getInventorySlots(worldId: Long): List<InventorySlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventorySlots(slots: List<InventorySlotEntity>)
}
