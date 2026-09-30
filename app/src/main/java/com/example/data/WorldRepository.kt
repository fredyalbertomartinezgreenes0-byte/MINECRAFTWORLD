package com.example.data

import kotlinx.coroutines.flow.Flow

class WorldRepository(private val worldDao: WorldDao) {
    val allWorlds: Flow<List<WorldEntity>> = worldDao.getAllWorlds()

    suspend fun getWorld(id: Long): WorldEntity? = worldDao.getWorldById(id)

    suspend fun createWorld(world: WorldEntity): Long = worldDao.insertWorld(world)

    suspend fun updateWorld(world: WorldEntity) = worldDao.updateWorld(world)

    suspend fun deleteWorld(worldId: Long) {
        worldDao.deleteBlockDeltas(worldId)
        worldDao.deleteInventorySlots(worldId)
        worldDao.deleteWorld(worldId)
    }

    suspend fun getBlockDeltas(worldId: Long, dimension: String): Map<Triple<Int, Int, Int>, Int> {
        val deltas = worldDao.getBlockDeltas(worldId, dimension)
        return deltas.associate { Triple(it.x, it.y, it.z) to it.blockId }
    }

    suspend fun saveBlockDelta(worldId: Long, dimension: String, x: Int, y: Int, z: Int, blockId: Int) {
        worldDao.insertBlockDelta(
            BlockDeltaEntity(
                worldId = worldId,
                dimension = dimension,
                x = x,
                y = y,
                z = z,
                blockId = blockId
            )
        )
    }

    suspend fun getInventory(worldId: Long): List<InventorySlotEntity> {
        return worldDao.getInventorySlots(worldId)
    }

    suspend fun saveInventory(worldId: Long, slots: List<InventorySlotEntity>) {
        worldDao.insertInventorySlots(slots)
    }
}
