package com.example.engine

import com.example.model.BlockFace
import com.example.model.BlockPos
import com.example.model.BlockType
import com.example.model.Dimension
import com.example.model.RaycastHit
import com.example.model.Vec3f
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

class VoxelWorld(
    val seed: Long,
    var currentDimension: Dimension = Dimension.OVERWORLD
) {
    // Delta block modifications: key = (x, y, z), value = BlockType
    private val modifiedBlocks = mutableMapOf<BlockPos, BlockType>()

    // World size limits for active loaded zone
    val minX = -48
    val maxX = 48
    val minZ = -48
    val maxZ = 48
    val minY = 0
    val maxY = 48
    val seaLevel = 10

    fun setBlock(x: Int, y: Int, z: Int, type: BlockType) {
        val pos = BlockPos(x, y, z)
        modifiedBlocks[pos] = type
    }

    fun getBlock(x: Int, y: Int, z: Int): BlockType {
        if (y < minY || y > maxY) return BlockType.AIR

        val pos = BlockPos(x, y, z)
        val modified = modifiedBlocks[pos]
        if (modified != null) return modified

        return generateBaseBlock(x, y, z, currentDimension)
    }

    fun loadDeltas(deltas: Map<Triple<Int, Int, Int>, Int>) {
        deltas.forEach { (coord, blockId) ->
            modifiedBlocks[BlockPos(coord.first, coord.second, coord.third)] = BlockType.fromId(blockId)
        }
    }

    fun getModifiedBlocks(): Map<BlockPos, BlockType> = modifiedBlocks

    private fun pseudoNoise(x: Double, z: Double, s: Long): Double {
        val n = sin(x * 12.9898 + z * 78.233 + s * 0.1) * 43758.5453
        return n - floor(n)
    }

    private fun smoothNoise(x: Double, z: Double, s: Long): Double {
        val ix = floor(x)
        val iz = floor(z)
        val fx = x - ix
        val fz = z - iz

        // Smoothstep
        val sx = fx * fx * (3.0 - 2.0 * fx)
        val sz = fz * fz * (3.0 - 2.0 * fz)

        val n00 = pseudoNoise(ix, iz, s)
        val n10 = pseudoNoise(ix + 1, iz, s)
        val n01 = pseudoNoise(ix, iz + 1, s)
        val n11 = pseudoNoise(ix + 1, iz + 1, s)

        val nx0 = n00 * (1.0 - sx) + n10 * sx
        val nx1 = n01 * (1.0 - sx) + n11 * sx
        return nx0 * (1.0 - sz) + nx1 * sz
    }

    private fun perlin2D(x: Double, z: Double, octaves: Int, s: Long): Double {
        var total = 0.0
        var freq = 1.0
        var amp = 1.0
        var maxAmp = 0.0

        for (i in 0 until octaves) {
            total += smoothNoise(x * freq, z * freq, s + i * 1013) * amp
            maxAmp += amp
            amp *= 0.5
            freq *= 2.0
        }
        return total / maxAmp
    }

    fun getTerrainHeight(x: Int, z: Int, dimension: Dimension): Int {
        if (dimension == Dimension.VOIDLANDS) {
            // Floating islands logic
            val islandDist = sqrt((x * x + z * z).toDouble())
            if (islandDist < 16) {
                // Central boss island
                return 32
            }
            val n = perlin2D(x * 0.08, z * 0.08, 2, seed + 999)
            return if (n > 0.6) 28 + ((n - 0.6) * 20).toInt() else 0
        }

        // Overworld heightmap
        val baseNoise = perlin2D(x * 0.03, z * 0.03, 3, seed)
        val mountainNoise = perlin2D(x * 0.015, z * 0.015, 4, seed + 456)

        var height = (baseNoise * 14.0 + 8.0).toInt()
        if (mountainNoise > 0.65) {
            height += ((mountainNoise - 0.65) * 40.0).toInt()
        }
        return height.coerceIn(minY + 1, maxY - 2)
    }

    private fun generateBaseBlock(x: Int, y: Int, z: Int, dimension: Dimension): BlockType {
        if (dimension == Dimension.VOIDLANDS) {
            return generateVoidlandsBlock(x, y, z)
        }

        // --- OVERWORLD ---
        if (y == 0) return BlockType.BEDROCK

        val height = getTerrainHeight(x, z, dimension)

        // Water in valleys below sea level
        if (y > height) {
            if (y <= seaLevel) {
                return BlockType.WATER
            }
            // Check tree foliage / trunk
            val treeCheck = isTreeBlock(x, y, z, height)
            if (treeCheck != null) return treeCheck
            return BlockType.AIR
        }

        // Desert biome detection
        val tempNoise = perlin2D(x * 0.02, z * 0.02, 2, seed + 888)
        val isDesert = tempNoise > 0.72

        if (y == height) {
            if (height <= seaLevel + 1) return BlockType.SAND
            if (height >= 26) return BlockType.SNOW
            if (isDesert) return BlockType.SAND
            return BlockType.GRASS
        }

        if (y >= height - 3) {
            if (isDesert || height <= seaLevel + 1) return BlockType.SAND
            return BlockType.DIRT
        }

        // Deep stone with ores & caves
        val caveNoise = perlin2D(x * 0.07, y * 0.07 + z * 0.07, 2, seed + 333)
        if (y in 3..14 && caveNoise > 0.82) {
            return BlockType.AIR // Cave pocket
        }

        val oreNoise = perlin2D(x * 0.15, y * 0.15 + z * 0.15, 2, seed + 777)
        if (y in 2..8 && oreNoise > 0.88) return BlockType.DIAMOND_ORE
        if (y in 4..16 && oreNoise > 0.83) return BlockType.GOLD_ORE
        if (y in 4..22 && oreNoise > 0.74) return BlockType.IRON_ORE
        if (y in 4..26 && oreNoise > 0.65) return BlockType.COAL_ORE

        return BlockType.STONE
    }

    private fun generateVoidlandsBlock(x: Int, y: Int, z: Int): BlockType {
        // Void Citadel Arena at center
        val distToCenter = sqrt((x * x + z * z).toDouble())
        if (distToCenter < 16) {
            if (y == 32) {
                // Portal at center
                if (x == 0 && z == 0) return BlockType.VOID_PORTAL
                if (abs(x) == 1 && abs(z) == 1) return BlockType.VOID_CRYSTAL
                return BlockType.VOID_BRICK
            }
            if (y in 26..31) return BlockType.VOIDSTONE
            // Obsidian pillars on perimeter
            if (y in 33..40 && (abs(x) == 10 && abs(z) == 10)) {
                return if (y == 40) BlockType.VOID_CRYSTAL else BlockType.VOID_OBSIDIAN
            }
            return BlockType.AIR
        }

        // Outer floating islands
        val islandNoise = perlin2D(x * 0.08, z * 0.08, 2, seed + 999)
        if (islandNoise > 0.6) {
            val islandBase = 22
            val islandTop = 28 + ((islandNoise - 0.6) * 16).toInt()

            if (y in islandBase..islandTop) {
                if (y == islandTop) {
                    val crystalSprout = pseudoNoise(x.toDouble(), z.toDouble(), seed + 555)
                    if (crystalSprout > 0.92) return BlockType.VOID_CRYSTAL
                    return BlockType.VOIDSTONE
                }
                val voidOreCheck = pseudoNoise(x.toDouble(), (y + z).toDouble(), seed + 444)
                if (voidOreCheck > 0.85) return BlockType.VOID_ORE
                return BlockType.VOIDSTONE
            }
        }

        return BlockType.AIR
    }

    private fun isTreeBlock(x: Int, y: Int, z: Int, groundHeight: Int): BlockType? {
        if (groundHeight < seaLevel + 2 || groundHeight > 24) return null

        // Tree grid every 5-7 blocks based on hash
        val cellX = floor(x / 6.0).toInt()
        val cellZ = floor(z / 6.0).toInt()
        val treeHash = pseudoNoise(cellX.toDouble(), cellZ.toDouble(), seed + 123)

        if (treeHash < 0.65) return null // No tree in this cell

        val trunkX = cellX * 6 + (treeHash * 3).toInt()
        val trunkZ = cellZ * 6 + (treeHash * 4).toInt()

        val trunkHeight = 5
        val treeTopY = groundHeight + trunkHeight

        // Trunk check
        if (x == trunkX && z == trunkZ) {
            if (y in (groundHeight + 1)..treeTopY) {
                return BlockType.OAK_LOG
            }
        }

        // Leaves canopy
        val dx = abs(x - trunkX)
        val dz = abs(z - trunkZ)
        val dy = y - treeTopY

        if (dy in -2..1) {
            val radius = if (dy == 1) 1 else 2
            if (dx <= radius && dz <= radius && !(dx == radius && dz == radius && dy == 1)) {
                return BlockType.OAK_LEAVES
            }
        }

        return null
    }

    // Raycast from camera eye along look direction to find targeted block
    fun raycast(origin: Vec3f, direction: Vec3f, maxDistance: Float = 6.0f): RaycastHit? {
        val step = 0.05f
        var traveled = 0f

        var currX = origin.x
        var currY = origin.y
        var currZ = origin.z

        var prevBx = floor(currX).toInt()
        var prevBy = floor(currY).toInt()
        var prevBz = floor(currZ).toInt()

        while (traveled < maxDistance) {
            currX += direction.x * step
            currY += direction.y * step
            currZ += direction.z * step
            traveled += step

            val bx = floor(currX).toInt()
            val by = floor(currY).toInt()
            val bz = floor(currZ).toInt()

            if (bx != prevBx || by != prevBy || bz != prevBz) {
                val block = getBlock(bx, by, bz)
                if (block.isSolid || block == BlockType.VOID_PORTAL) {
                    val face = when {
                        prevBy > by -> BlockFace.UP
                        prevBy < by -> BlockFace.DOWN
                        prevBx > bx -> BlockFace.EAST
                        prevBx < bx -> BlockFace.WEST
                        prevBz > bz -> BlockFace.SOUTH
                        else -> BlockFace.NORTH
                    }
                    return RaycastHit(
                        blockPos = BlockPos(bx, by, bz),
                        face = face,
                        blockType = block,
                        distance = traveled
                    )
                }
                prevBx = bx
                prevBy = by
                prevBz = bz
            }
        }
        return null
    }

    fun isCollidingWithWorld(x: Float, y: Float, z: Float, width: Float = 0.6f, height: Float = 1.8f): Boolean {
        val minBx = floor(x - width / 2f).toInt()
        val maxBx = floor(x + width / 2f).toInt()
        val minBy = floor(y).toInt()
        val maxBy = floor(y + height).toInt()
        val minBz = floor(z - width / 2f).toInt()
        val maxBz = floor(z + width / 2f).toInt()

        for (by in minBy..maxBy) {
            for (bx in minBx..maxBx) {
                for (bz in minBz..maxBz) {
                    val b = getBlock(bx, by, bz)
                    if (b.isSolid) return true
                }
            }
        }
        return false
    }

    fun isInWater(x: Float, y: Float, z: Float): Boolean {
        val bx = floor(x).toInt()
        val by = floor(y + 0.8f).toInt()
        val bz = floor(z).toInt()
        return getBlock(bx, by, bz) == BlockType.WATER
    }
}
