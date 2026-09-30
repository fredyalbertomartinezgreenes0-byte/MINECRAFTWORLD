package com.example.gameplay

import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameSoundSynthesizer
import com.example.crafting.CraftingManager
import com.example.data.AppDatabase
import com.example.data.InventorySlotEntity
import com.example.data.WorldEntity
import com.example.data.WorldRepository
import com.example.engine.VoxelRenderer
import com.example.engine.VoxelWorld
import com.example.model.ArmorSlot
import com.example.model.BlockFace
import com.example.model.BlockPos
import com.example.model.BlockType
import com.example.model.CreatureEntity
import com.example.model.CreatureType
import com.example.model.Dimension
import com.example.model.GameMode
import com.example.model.ItemCategory
import com.example.model.ItemRegistry
import com.example.model.ItemStack
import com.example.model.PlayerStats
import com.example.model.Projectile
import com.example.model.RaycastHit
import com.example.model.ToolType
import com.example.model.Vec3f
import com.example.model.Weather
import com.example.model.WorldSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorldRepository
    private val vibrator: Vibrator? = application.getSystemService(Vibrator::class.java)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WorldRepository(db.worldDao())
    }

    // Current World Info
    var currentWorldId: Long = 0L
    private val _worldName = MutableStateFlow("Nuevo Mundo")
    val worldName: StateFlow<String> = _worldName.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.SURVIVAL)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _dimension = MutableStateFlow(Dimension.OVERWORLD)
    val dimension: StateFlow<Dimension> = _dimension.asStateFlow()

    private val _weather = MutableStateFlow(Weather.CLEAR)
    val weather: StateFlow<Weather> = _weather.asStateFlow()

    private val _timeOfDay = MutableStateFlow(0.25f) // 0..1 (morning)
    val timeOfDay: StateFlow<Float> = _timeOfDay.asStateFlow()

    // Player position and orientation
    val playerPos = Vec3f(0f, 22f, 0f)
    var playerVelocity = Vec3f(0f, 0f, 0f)
    val playerPitch = MutableStateFlow(0f)
    val playerYaw = MutableStateFlow(0f)
    val isThirdPerson = MutableStateFlow(false)

    // Player Stats
    private val _playerStats = MutableStateFlow(PlayerStats())
    val playerStats: StateFlow<PlayerStats> = _playerStats.asStateFlow()

    // Inventory: 0..8 Hotbar, 9..35 Main Inventory, 36..39 Armor (Helmet, Chest, Legs, Boots)
    val inventory = MutableStateFlow(Array(40) { ItemStack.EMPTY })
    private val _selectedHotbarIndex = MutableStateFlow(0)
    val selectedHotbarIndex: StateFlow<Int> = _selectedHotbarIndex.asStateFlow()

    // Crafting State
    val craftingGrid = MutableStateFlow(Array(9) { ItemStack.EMPTY })
    val craftingResult = MutableStateFlow(ItemStack.EMPTY)
    val isCraftingTableOpen = MutableStateFlow(false)

    // Furnace State
    val furnaceInput = MutableStateFlow(ItemStack.EMPTY)
    val furnaceFuel = MutableStateFlow(ItemStack.EMPTY)
    val furnaceOutput = MutableStateFlow(ItemStack.EMPTY)
    val furnaceProgress = MutableStateFlow(0f)
    val isFurnaceOpen = MutableStateFlow(false)

    // Chest State
    val chestInventory = MutableStateFlow(Array(27) { ItemStack.EMPTY })
    val isChestOpen = MutableStateFlow(false)

    // Mining / Interaction
    val targetedHit = MutableStateFlow<RaycastHit?>(null)
    val breakProgress = MutableStateFlow(0f)
    private var isMining = false
    private var currentMiningPos: BlockPos? = null

    // Creatures & Boss
    val creatures = MutableStateFlow<List<CreatureEntity>>(emptyList())
    val projectiles = MutableStateFlow<List<Projectile>>(emptyList())
    val voidKingBoss = MutableStateFlow<CreatureEntity?>(null)
    val isVictoryModalOpen = MutableStateFlow(false)
    val isDimensionWarping = MutableStateFlow(false)

    // UI Modals
    val isInventoryOpen = MutableStateFlow(false)
    val isPauseMenuOpen = MutableStateFlow(false)
    val settings = MutableStateFlow(WorldSettings())

    // Engine references
    var voxelWorld: VoxelWorld = VoxelWorld(12345L, Dimension.OVERWORLD)
    var renderer: VoxelRenderer? = null
    private var gameLoopJob: Job? = null

    fun loadOrInitWorld(worldEntity: WorldEntity) {
        currentWorldId = worldEntity.id
        _worldName.value = worldEntity.name
        _gameMode.value = try { GameMode.valueOf(worldEntity.gameMode) } catch (_: Exception) { GameMode.SURVIVAL }
        _dimension.value = if (worldEntity.dimension == "voidlands") Dimension.VOIDLANDS else Dimension.OVERWORLD
        _timeOfDay.value = worldEntity.timeOfDay

        voxelWorld = VoxelWorld(worldEntity.seed, _dimension.value)
        renderer?.voxelWorld = voxelWorld

        playerPos.x = worldEntity.playerX
        playerPos.y = worldEntity.playerY
        playerPos.z = worldEntity.playerZ
        playerPitch.value = worldEntity.playerPitch
        playerYaw.value = worldEntity.playerYaw

        _playerStats.value = PlayerStats(
            health = worldEntity.health,
            hunger = worldEntity.hunger,
            isFlying = _gameMode.value == GameMode.CREATIVE
        )

        // Load inventory & deltas asynchronously
        viewModelScope.launch {
            val deltas = repository.getBlockDeltas(currentWorldId, _dimension.value.id)
            voxelWorld.loadDeltas(deltas)

            val slots = repository.getInventory(currentWorldId)
            val newInv = Array(40) { ItemStack.EMPTY }
            if (slots.isEmpty()) {
                // Initial starter kit
                newInv[0] = ItemStack("wooden_pickaxe", 1, 60)
                newInv[1] = ItemStack("wooden_sword", 1, 60)
                newInv[2] = ItemStack("wooden_axe", 1, 60)
                newInv[3] = ItemStack("torch", 16)
                newInv[4] = ItemStack("bread", 6)
                newInv[5] = ItemStack("oak_planks", 32)
                if (_dimension.value == Dimension.VOIDLANDS) {
                    newInv[6] = ItemStack("void_blade", 1)
                    newInv[7] = ItemStack("void_core_spark", 1)
                }
            } else {
                for (s in slots) {
                    if (s.slotIndex in 0 until 40) {
                        newInv[s.slotIndex] = ItemStack(s.itemId, s.count, s.durability)
                    }
                }
            }
            inventory.value = newInv
            renderer?.requestMeshRebuild()

            // Spawn initial creatures
            spawnInitialCreatures()
        }

        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.1f)
                lastTime = now

                if (!_playerStats.value.isDead && !isPauseMenuOpen.value) {
                    updatePlayerPhysics(dt)
                    updateSurvivalStats(dt)
                    updateTargeting()
                    updateMining(dt)
                    updateCreatures(dt)
                    updateProjectiles(dt)
                    updateFurnace(dt)

                    // Time of day cycle
                    _timeOfDay.value = (_timeOfDay.value + dt * 0.005f) % 1.0f
                }

                syncToRenderer()
                delay(16) // ~60 FPS
            }
        }
    }

    fun onMoveInput(forward: Float, strafe: Float) {
        if (_playerStats.value.isDead) return
        val radYaw = Math.toRadians(playerYaw.value.toDouble())
        val sinY = sin(radYaw).toFloat()
        val cosY = cos(radYaw).toFloat()

        val speed = if (_playerStats.value.isFlying) 9.0f else if (_playerStats.value.isSneaking) 2.5f else 5.0f

        val moveX = (-sinY * forward + cosY * strafe) * speed
        val moveZ = (-cosY * forward - sinY * strafe) * speed

        playerVelocity.x = moveX
        playerVelocity.z = moveZ

        if (forward != 0f || strafe != 0f) {
            if (Math.random() < 0.03) {
                GameSoundSynthesizer.playFootstep()
            }
        }
    }

    fun onLookInput(deltaPitch: Float, deltaYaw: Float) {
        val sens = settings.value.sensitivity
        val invert = if (settings.value.invertY) -1f else 1f

        playerPitch.value = (playerPitch.value + deltaPitch * sens * invert).coerceIn(-89f, 89f)
        playerYaw.value = (playerYaw.value + deltaYaw * sens) % 360f
    }

    fun jump() {
        if (_playerStats.value.isDead) return
        if (_playerStats.value.isFlying) {
            playerVelocity.y = 5.0f
            return
        }
        val onGround = voxelWorld.isCollidingWithWorld(playerPos.x, playerPos.y - 0.1f, playerPos.z)
        val inWater = voxelWorld.isInWater(playerPos.x, playerPos.y, playerPos.z)

        if (onGround || inWater) {
            playerVelocity.y = if (inWater) 3.5f else 6.5f
            GameSoundSynthesizer.playFootstep()
        }
    }

    fun toggleFly() {
        if (_gameMode.value == GameMode.CREATIVE) {
            val flying = !_playerStats.value.isFlying
            _playerStats.value = _playerStats.value.copy(isFlying = flying)
            playerVelocity.y = 0f
        }
    }

    fun flyDown() {
        if (_playerStats.value.isFlying) {
            playerVelocity.y = -5.0f
        }
    }

    fun toggleSneak() {
        _playerStats.value = _playerStats.value.copy(isSneaking = !_playerStats.value.isSneaking)
    }

    fun selectHotbarSlot(index: Int) {
        if (index in 0..8) {
            _selectedHotbarIndex.value = index
        }
    }

    fun startMining() {
        isMining = true
    }

    fun stopMining() {
        isMining = false
        breakProgress.value = 0f
        currentMiningPos = null
    }

    fun placeBlockOrUseItem() {
        val currentStack = inventory.value[_selectedHotbarIndex.value]
        if (currentStack.isEmpty) return

        // Check if food
        if (currentStack.def.category == ItemCategory.FOOD) {
            if (_playerStats.value.hunger < _playerStats.value.maxHunger || _playerStats.value.health < _playerStats.value.maxHealth) {
                val newHunger = (_playerStats.value.hunger + currentStack.def.foodValue).coerceAtMost(20f)
                val newHealth = (_playerStats.value.health + 2f).coerceAtMost(20f)
                _playerStats.value = _playerStats.value.copy(hunger = newHunger, health = newHealth)
                consumeHeldItem(1)
                GameSoundSynthesizer.playItemPickup()
                return
            }
        }

        // Check if Void Core Spark to activate portal
        val hit = targetedHit.value ?: return
        if (currentStack.itemId == "void_core_spark" && hit.blockType == BlockType.VOID_OBSIDIAN) {
            activateVoidPortal(hit.blockPos)
            return
        }

        // Open Crafting Table, Furnace, Chest
        when (hit.blockType) {
            BlockType.CRAFTING_TABLE -> {
                isCraftingTableOpen.value = true
                return
            }
            BlockType.FURNACE -> {
                isFurnaceOpen.value = true
                return
            }
            BlockType.CHEST -> {
                isChestOpen.value = true
                return
            }
            BlockType.VOID_PORTAL -> {
                warpDimension()
                return
            }
            else -> {}
        }

        // Block placement
        val blockId = currentStack.def.associatedBlockId ?: return
        val targetPos = when (hit.face) {
            BlockFace.UP -> BlockPos(hit.blockPos.x, hit.blockPos.y + 1, hit.blockPos.z)
            BlockFace.DOWN -> BlockPos(hit.blockPos.x, hit.blockPos.y - 1, hit.blockPos.z)
            BlockFace.NORTH -> BlockPos(hit.blockPos.x, hit.blockPos.y, hit.blockPos.z - 1)
            BlockFace.SOUTH -> BlockPos(hit.blockPos.x, hit.blockPos.y, hit.blockPos.z + 1)
            BlockFace.EAST -> BlockPos(hit.blockPos.x + 1, hit.blockPos.y, hit.blockPos.z)
            BlockFace.WEST -> BlockPos(hit.blockPos.x - 1, hit.blockPos.y, hit.blockPos.z)
        }

        // Prevent placing inside player body
        val pBx = floor(playerPos.x).toInt()
        val pBy = floor(playerPos.y).toInt()
        val pBz = floor(playerPos.z).toInt()
        if (targetPos.x == pBx && (targetPos.y == pBy || targetPos.y == pBy + 1) && targetPos.z == pBz) {
            return
        }

        val placedType = BlockType.fromId(blockId)
        voxelWorld.setBlock(targetPos.x, targetPos.y, targetPos.z, placedType)
        renderer?.requestMeshRebuild()
        GameSoundSynthesizer.playBlockPlace()

        if (_gameMode.value != GameMode.CREATIVE) {
            consumeHeldItem(1)
        }

        // Save delta to Room
        viewModelScope.launch {
            repository.saveBlockDelta(currentWorldId, _dimension.value.id, targetPos.x, targetPos.y, targetPos.z, blockId)
        }
    }

    fun attack() {
        val held = inventory.value[_selectedHotbarIndex.value]
        val damage = if (held.isEmpty) 2.0f else held.def.attackDamage.coerceAtLeast(2.0f)

        // Swing animation trigger
        renderer?.swingAnimation = 1.0f

        // Check if any mob in front of player
        val radPitch = Math.toRadians(playerPitch.value.toDouble())
        val radYaw = Math.toRadians(playerYaw.value.toDouble())
        val dirX = (-sin(radYaw) * cos(radPitch)).toFloat()
        val dirY = (-sin(radPitch)).toFloat()
        val dirZ = (-cos(radYaw) * cos(radPitch)).toFloat()

        var hitMob: CreatureEntity? = null
        for (c in creatures.value) {
            if (c.isDead) continue
            val dist = playerPos.distanceTo(Vec3f(c.x, c.y, c.z))
            if (dist < 4.0f) {
                // Vector dot product check
                val dx = (c.x - playerPos.x) / dist
                val dy = (c.y - playerPos.y) / dist
                val dz = (c.z - playerPos.z) / dist
                val dot = dx * dirX + dy * dirY + dz * dirZ
                if (dot > 0.6f) {
                    hitMob = c
                    break
                }
            }
        }

        if (hitMob != null) {
            GameSoundSynthesizer.playHit()
            val killed = hitMob.takeDamage(damage)
            vibrate(40)

            if (killed) {
                onCreatureKilled(hitMob)
            }
        } else {
            GameSoundSynthesizer.playFootstep()
        }
    }

    private fun onCreatureKilled(creature: CreatureEntity) {
        GameSoundSynthesizer.playItemPickup()
        when (creature.type) {
            CreatureType.WOOLOC -> addItemToInventory(ItemStack("oak_planks", 2))
            CreatureType.BOARLING -> addItemToInventory(ItemStack("raw_pork", 2))
            CreatureType.GLOOMSTALKER -> addItemToInventory(ItemStack("coal", 3))
            CreatureType.CREEPLING -> addItemToInventory(ItemStack("sand", 4))
            CreatureType.BONEGUARD -> addItemToInventory(ItemStack("stick", 2))
            CreatureType.VOIDLING -> addItemToInventory(ItemStack("void_shard", 2))
            CreatureType.ABYSS_GOLEM -> addItemToInventory(ItemStack("void_ore_raw", 2))
            CreatureType.VOID_KING -> {
                // Epic Boss Defeated!
                GameSoundSynthesizer.playBossRoar()
                addItemToInventory(ItemStack("void_crown", 1))
                addItemToInventory(ItemStack("void_blade", 1))
                addItemToInventory(ItemStack("void_elytra", 1))
                isVictoryModalOpen.value = true
                viewModelScope.launch {
                    val currentWorld = repository.getWorld(currentWorldId)
                    if (currentWorld != null) {
                        repository.updateWorld(currentWorld.copy(bossDefeated = true))
                    }
                }
            }
            else -> {}
        }
    }

    private fun activateVoidPortal(pos: BlockPos) {
        voxelWorld.setBlock(pos.x, pos.y + 1, pos.z, BlockType.VOID_PORTAL)
        voxelWorld.setBlock(pos.x, pos.y + 2, pos.z, BlockType.VOID_PORTAL)
        renderer?.requestMeshRebuild()
        GameSoundSynthesizer.playPortalWarp()
        vibrate(80)
        warpDimension()
    }

    fun warpDimension() {
        if (isDimensionWarping.value) return
        isDimensionWarping.value = true
        GameSoundSynthesizer.playPortalWarp()

        viewModelScope.launch {
            delay(1200)
            val newDim = if (_dimension.value == Dimension.OVERWORLD) Dimension.VOIDLANDS else Dimension.OVERWORLD
            _dimension.value = newDim
            voxelWorld = VoxelWorld(voxelWorld.seed, newDim)
            renderer?.voxelWorld = voxelWorld

            // Load new dimension deltas
            val deltas = repository.getBlockDeltas(currentWorldId, newDim.id)
            voxelWorld.loadDeltas(deltas)

            // Spawn player at dimension entry
            if (newDim == Dimension.VOIDLANDS) {
                playerPos.x = 0f
                playerPos.y = 35f
                playerPos.z = 8f
            } else {
                playerPos.x = 0f
                playerPos.y = 22f
                playerPos.z = 0f
            }

            spawnInitialCreatures()
            renderer?.requestMeshRebuild()
            delay(500)
            isDimensionWarping.value = false
        }
    }

    private fun updatePlayerPhysics(dt: Float) {
        // Gravity
        if (!_playerStats.value.isFlying) {
            val inWater = voxelWorld.isInWater(playerPos.x, playerPos.y, playerPos.z)
            val gravity = if (inWater) 4.0f else 16.0f
            playerVelocity.y -= gravity * dt
        }

        // Velocity clamping
        playerVelocity.y = playerVelocity.y.coerceIn(-25f, 25f)

        // Move X with collision
        val targetX = playerPos.x + playerVelocity.x * dt
        if (!voxelWorld.isCollidingWithWorld(targetX, playerPos.y, playerPos.z)) {
            playerPos.x = targetX
        } else {
            playerVelocity.x = 0f
        }

        // Move Y with collision
        val targetY = playerPos.y + playerVelocity.y * dt
        if (!voxelWorld.isCollidingWithWorld(playerPos.x, targetY, playerPos.z)) {
            playerPos.y = targetY
        } else {
            // Hit ground or ceiling
            playerVelocity.y = 0f
        }

        // Move Z with collision
        val targetZ = playerPos.z + playerVelocity.z * dt
        if (!voxelWorld.isCollidingWithWorld(playerPos.x, playerPos.y, targetZ)) {
            playerPos.z = targetZ
        } else {
            playerVelocity.z = 0f
        }

        // Void fall check
        if (playerPos.y < -5f) {
            if (_dimension.value == Dimension.VOIDLANDS) {
                takePlayerDamage(4f * dt)
                if (playerPos.y < -20f) {
                    playerPos.y = 45f // Teleport back to top island
                }
            } else {
                playerPos.y = 25f
            }
        }

        // Friction
        playerVelocity.x *= 0.7f
        playerVelocity.z *= 0.7f
        if (_playerStats.value.isFlying) {
            playerVelocity.y *= 0.7f
        }
    }

    private fun updateSurvivalStats(dt: Float) {
        if (_gameMode.value == GameMode.CREATIVE) return

        var stats = _playerStats.value

        // In water oxygen check
        val inWater = voxelWorld.isInWater(playerPos.x, playerPos.y, playerPos.z)
        if (inWater) {
            val newOx = (stats.oxygen - dt * 3f).coerceAtLeast(0f)
            stats = stats.copy(oxygen = newOx)
            if (newOx == 0f) {
                takePlayerDamage(2f * dt)
            }
        } else {
            val newOx = (stats.oxygen + dt * 10f).coerceAtMost(20f)
            stats = stats.copy(oxygen = newOx)
        }

        // Hunger and health regeneration
        val newHunger = (stats.hunger - dt * 0.02f).coerceAtLeast(0f)
        var newHealth = stats.health
        if (newHunger > 17f && newHealth < 20f) {
            newHealth = (newHealth + dt * 0.5f).coerceAtMost(20f)
        } else if (newHunger <= 0f) {
            newHealth = (newHealth - dt * 0.5f).coerceAtLeast(0f)
            if (newHealth <= 0f) {
                triggerPlayerDeath()
            }
        }

        _playerStats.value = stats.copy(hunger = newHunger, health = newHealth)
    }

    private fun updateTargeting() {
        val radPitch = Math.toRadians(playerPitch.value.toDouble())
        val radYaw = Math.toRadians(playerYaw.value.toDouble())

        val dir = Vec3f(
            (-sin(radYaw) * cos(radPitch)).toFloat(),
            (-sin(radPitch)).toFloat(),
            (-cos(radYaw) * cos(radPitch)).toFloat()
        )

        val eyePos = Vec3f(playerPos.x, playerPos.y + 1.62f, playerPos.z)
        targetedHit.value = voxelWorld.raycast(eyePos, dir, 5.5f)
    }

    private fun updateMining(dt: Float) {
        if (!isMining) return
        val hit = targetedHit.value ?: run {
            breakProgress.value = 0f
            currentMiningPos = null
            return
        }

        if (currentMiningPos != hit.blockPos) {
            currentMiningPos = hit.blockPos
            breakProgress.value = 0f
        }

        if (_gameMode.value == GameMode.CREATIVE) {
            // Instant break in creative
            breakCurrentBlock(hit)
            isMining = false
            return
        }

        val heldItem = inventory.value[_selectedHotbarIndex.value]
        val toolSpeed = heldItem.def.miningSpeedMultiplier.coerceAtLeast(1.0f)
        val hardness = hit.blockType.hardness.coerceAtLeast(0.1f)

        val speed = (toolSpeed / hardness) * dt * 1.8f
        val newProg = breakProgress.value + speed
        breakProgress.value = newProg

        if (newProg >= 1.0f) {
            breakCurrentBlock(hit)
            breakProgress.value = 0f
        }
    }

    private fun breakCurrentBlock(hit: RaycastHit) {
        val block = hit.blockType
        voxelWorld.setBlock(hit.blockPos.x, hit.blockPos.y, hit.blockPos.z, BlockType.AIR)
        renderer?.requestMeshRebuild()
        GameSoundSynthesizer.playBlockBreak()
        vibrate(30)

        // Drop item
        if (block.dropItemId.isNotEmpty() && _gameMode.value != GameMode.CREATIVE) {
            val dropStack = ItemStack(block.dropItemId, block.dropAmount)
            addItemToInventory(dropStack)
        }

        // Save block change to DB
        viewModelScope.launch {
            repository.saveBlockDelta(
                currentWorldId,
                _dimension.value.id,
                hit.blockPos.x,
                hit.blockPos.y,
                hit.blockPos.z,
                BlockType.AIR.id
            )
        }
    }

    private fun updateCreatures(dt: Float) {
        val list = creatures.value.toMutableList()
        val playerV = Vec3f(playerPos.x, playerPos.y, playerPos.z)

        for (c in list) {
            if (c.isDead) continue

            // Hurt timer
            if (c.isHurt) {
                c.hurtTimer -= dt
                if (c.hurtTimer <= 0f) c.isHurt = false
            }

            val dist = playerV.distanceTo(Vec3f(c.x, c.y, c.z))

            // AI Logic
            if (c.type.isHostile) {
                if (dist < 18.0f) {
                    // Turn towards player
                    val dx = playerPos.x - c.x
                    val dz = playerPos.z - c.z
                    c.yaw = Math.toDegrees(kotlin.math.atan2(-dx.toDouble(), -dz.toDouble())).toFloat()

                    // Move towards player
                    if (dist > 1.2f) {
                        val moveSpeed = c.type.moveSpeed
                        c.x += (dx / dist) * moveSpeed
                        c.z += (dz / dist) * moveSpeed
                    } else {
                        // Melee attack
                        c.attackCooldown -= dt
                        if (c.attackCooldown <= 0f) {
                            c.attackCooldown = 1.2f
                            takePlayerDamage(c.type.damage)
                            GameSoundSynthesizer.playHit()
                        }
                    }

                    // Creepling explosion
                    if (c.type == CreatureType.CREEPLING && dist < 3.0f) {
                        c.fuseTimer += dt
                        if (c.fuseTimer > 1.5f) {
                            c.isDead = true
                            GameSoundSynthesizer.playExplosion()
                            vibrate(100)
                            if (dist < 4.5f) {
                                takePlayerDamage(c.type.damage * (1f - dist / 4.5f))
                            }
                        }
                    }
                }
            }

            // Boss specific phases
            if (c.type.isBoss) {
                updateVoidKingBoss(c, dt, dist)
            }
        }
        creatures.value = list
    }

    private fun updateVoidKingBoss(boss: CreatureEntity, dt: Float, distToPlayer: Float) {
        voidKingBoss.value = boss
        boss.attackCooldown -= dt

        // Phase transitions based on HP
        val hpPct = boss.health / boss.type.maxHealth
        boss.bossPhase = when {
            hpPct > 0.75f -> 1
            hpPct > 0.50f -> 2
            hpPct > 0.25f -> 3
            else -> 4
        }

        // Float hover animation
        boss.stateTimer += dt
        boss.y = 35f + sin(boss.stateTimer * 2.5f) * 1.5f

        if (boss.attackCooldown <= 0f) {
            when (boss.bossPhase) {
                1 -> {
                    // Phase 1: Fire Void Energy Orb
                    boss.attackCooldown = 2.5f
                    val dx = (playerPos.x - boss.x) / distToPlayer.coerceAtLeast(1f)
                    val dy = (playerPos.y - boss.y) / distToPlayer.coerceAtLeast(1f)
                    val dz = (playerPos.z - boss.z) / distToPlayer.coerceAtLeast(1f)
                    spawnProjectile(boss.x, boss.y + 1f, boss.z, dx * 9f, dy * 9f, dz * 9f, 6f, 0xFFE040FB)
                    GameSoundSynthesizer.playHit()
                }
                2 -> {
                    // Phase 2: Summon Voidlings
                    boss.attackCooldown = 5.0f
                    spawnCreature(CreatureType.VOIDLING, boss.x + 3f, boss.y, boss.z + 3f)
                    spawnCreature(CreatureType.VOIDLING, boss.x - 3f, boss.y, boss.z - 3f)
                    GameSoundSynthesizer.playBossRoar()
                }
                3 -> {
                    // Phase 3: Teleport & Shockwave
                    boss.attackCooldown = 4.0f
                    val angle = Math.random() * Math.PI * 2.0
                    boss.x = (cos(angle) * 12.0).toFloat()
                    boss.z = (sin(angle) * 12.0).toFloat()
                    GameSoundSynthesizer.playPortalWarp()
                }
                4 -> {
                    // Phase 4: Enraged Void Nova Barrage
                    boss.attackCooldown = 1.6f
                    for (i in 0 until 4) {
                        val a = (i * Math.PI / 2.0) + boss.stateTimer
                        val vx = cos(a).toFloat() * 10f
                        val vz = sin(a).toFloat() * 10f
                        spawnProjectile(boss.x, boss.y, boss.z, vx, 0f, vz, 8f, 0xFFD500F9)
                    }
                    GameSoundSynthesizer.playBossRoar()
                }
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val list = projectiles.value.toMutableList()
        val playerV = Vec3f(playerPos.x, playerPos.y, playerPos.z)
        val it = list.iterator()

        while (it.hasNext()) {
            val p = it.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.z += p.vz * dt
            p.lifeTime -= dt

            if (p.lifeTime <= 0f) {
                it.remove()
                continue
            }

            // Check hit with player
            if (!p.isFromPlayer) {
                val dist = playerV.distanceTo(Vec3f(p.x, p.y, p.z))
                if (dist < 1.0f) {
                    takePlayerDamage(p.damage)
                    GameSoundSynthesizer.playHit()
                    it.remove()
                }
            }
        }
        projectiles.value = list
    }

    private fun spawnProjectile(x: Float, y: Float, z: Float, vx: Float, vy: Float, vz: Float, damage: Float, color: Long) {
        val proj = Projectile(
            id = UUID.randomUUID().toString(),
            x = x, y = y, z = z,
            vx = vx, vy = vy, vz = vz,
            isFromPlayer = false,
            damage = damage,
            color = color
        )
        projectiles.value = projectiles.value + proj
    }

    private fun spawnCreature(type: CreatureType, x: Float, y: Float, z: Float) {
        val entity = CreatureEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            x = x, y = y, z = z
        )
        creatures.value = creatures.value + entity
    }

    private fun spawnInitialCreatures() {
        val list = mutableListOf<CreatureEntity>()
        if (_dimension.value == Dimension.VOIDLANDS) {
            // Spawn VOID KING Boss at Citadel
            val boss = CreatureEntity(
                id = "void_king_boss",
                type = CreatureType.VOID_KING,
                x = 0f, y = 36f, z = 0f
            )
            list.add(boss)
            voidKingBoss.value = boss

            // Voidling guards
            list.add(CreatureEntity("voidling_1", CreatureType.VOIDLING, 6f, 33f, 6f))
            list.add(CreatureEntity("voidling_2", CreatureType.VOIDLING, -6f, 33f, -6f))
            list.add(CreatureEntity("abyss_1", CreatureType.ABYSS_GOLEM, 10f, 33f, -10f))
        } else {
            // Overworld creatures
            list.add(CreatureEntity("wooloc_1", CreatureType.WOOLOC, 5f, 22f, 5f))
            list.add(CreatureEntity("boarling_1", CreatureType.BOARLING, -7f, 22f, 6f))
            list.add(CreatureEntity("gloom_1", CreatureType.GLOOMSTALKER, 12f, 22f, 10f))
            list.add(CreatureEntity("creep_1", CreatureType.CREEPLING, -10f, 22f, -8f))
            list.add(CreatureEntity("bone_1", CreatureType.BONEGUARD, 14f, 22f, -12f))
        }
        creatures.value = list
    }

    private fun updateFurnace(dt: Float) {
        val input = furnaceInput.value
        val fuel = furnaceFuel.value
        if (!input.isEmpty && !fuel.isEmpty) {
            val result = CraftingManager.getSmeltResult(input.itemId)
            if (result != null) {
                val prog = furnaceProgress.value + dt * 0.25f
                if (prog >= 1.0f) {
                    furnaceProgress.value = 0f
                    // Complete smelt
                    furnaceInput.value = if (input.count > 1) input.copy(count = input.count - 1) else ItemStack.EMPTY
                    furnaceFuel.value = if (fuel.count > 1) fuel.copy(count = fuel.count - 1) else ItemStack.EMPTY
                    val out = furnaceOutput.value
                    furnaceOutput.value = if (out.isEmpty) result else out.copy(count = out.count + result.count)
                    GameSoundSynthesizer.playItemPickup()
                } else {
                    furnaceProgress.value = prog
                }
                return
            }
        }
        furnaceProgress.value = 0f
    }

    fun takePlayerDamage(amount: Float) {
        if (_gameMode.value == GameMode.CREATIVE) return
        val stats = _playerStats.value
        val newHp = (stats.health - amount).coerceAtLeast(0f)
        _playerStats.value = stats.copy(health = newHp)
        vibrate(60)
        GameSoundSynthesizer.playHit()
        if (newHp <= 0f) {
            triggerPlayerDeath()
        }
    }

    private fun triggerPlayerDeath() {
        _playerStats.value = _playerStats.value.copy(isDead = true)
        GameSoundSynthesizer.playBossRoar()
    }

    fun respawn() {
        playerPos.x = 0f
        playerPos.y = 22f
        playerPos.z = 0f
        _playerStats.value = _playerStats.value.copy(
            health = 20f,
            hunger = 20f,
            oxygen = 20f,
            isDead = false
        )
    }

    fun addItemToInventory(stack: ItemStack): Boolean {
        val inv = inventory.value.copyOf()
        // Try stacking
        for (i in 0 until 36) {
            if (inv[i].itemId == stack.itemId && inv[i].count < inv[i].def.maxStack) {
                val space = inv[i].def.maxStack - inv[i].count
                val add = stack.count.coerceAtMost(space)
                inv[i] = inv[i].copy(count = inv[i].count + add)
                inventory.value = inv
                return true
            }
        }
        // Place in empty slot
        for (i in 0 until 36) {
            if (inv[i].isEmpty) {
                inv[i] = stack.copy()
                inventory.value = inv
                return true
            }
        }
        return false
    }

    private fun consumeHeldItem(amount: Int) {
        val inv = inventory.value.copyOf()
        val idx = _selectedHotbarIndex.value
        val cur = inv[idx]
        if (cur.count > amount) {
            inv[idx] = cur.copy(count = cur.count - amount)
        } else {
            inv[idx] = ItemStack.EMPTY
        }
        inventory.value = inv
    }

    fun craftItem() {
        val result = craftingResult.value
        if (result.isEmpty) return
        if (addItemToInventory(result)) {
            // Decrement items in grid
            val grid = craftingGrid.value.copyOf()
            for (i in grid.indices) {
                if (!grid[i].isEmpty) {
                    grid[i] = if (grid[i].count > 1) grid[i].copy(count = grid[i].count - 1) else ItemStack.EMPTY
                }
            }
            craftingGrid.value = grid
            updateCraftingResult()
            GameSoundSynthesizer.playItemPickup()
        }
    }

    fun updateCraftingResult() {
        val grid = craftingGrid.value
        val isTable = isCraftingTableOpen.value
        val width = if (isTable) 3 else 2
        val height = if (isTable) 3 else 2

        val itemIds = grid.take(width * height).map { it.itemId }
        val output = CraftingManager.findRecipe(itemIds, width, height)
        craftingResult.value = output ?: ItemStack.EMPTY
    }

    private fun syncToRenderer() {
        renderer?.let { r ->
            r.playerX = playerPos.x
            r.playerY = playerPos.y
            r.playerZ = playerPos.z
            r.playerPitch = playerPitch.value
            r.playerYaw = playerYaw.value
            r.isThirdPerson = isThirdPerson.value
            r.timeOfDay = _timeOfDay.value
            r.targetedBlock = targetedHit.value
            r.breakProgress = breakProgress.value
            r.heldItemId = inventory.value[_selectedHotbarIndex.value].itemId
            r.creatures = creatures.value
            r.projectiles = projectiles.value
            if (r.swingAnimation > 0f) {
                r.swingAnimation = (r.swingAnimation - 0.1f).coerceAtLeast(0f)
            }
        }
    }

    private fun vibrate(ms: Long) {
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    fun saveGame() {
        viewModelScope.launch {
            val stats = _playerStats.value
            repository.updateWorld(
                WorldEntity(
                    id = currentWorldId,
                    name = _worldName.value,
                    seed = voxelWorld.seed,
                    gameMode = _gameMode.value.name,
                    dimension = _dimension.value.id,
                    playerX = playerPos.x,
                    playerY = playerPos.y,
                    playerZ = playerPos.z,
                    playerPitch = playerPitch.value,
                    playerYaw = playerYaw.value,
                    health = stats.health,
                    hunger = stats.hunger,
                    timeOfDay = _timeOfDay.value,
                    lastPlayed = System.currentTimeMillis()
                )
            )

            // Save inventory slots
            val invSlots = inventory.value.mapIndexed { idx, stack ->
                InventorySlotEntity(
                    worldId = currentWorldId,
                    slotIndex = idx,
                    itemId = stack.itemId,
                    count = stack.count,
                    durability = stack.durability
                )
            }
            repository.saveInventory(currentWorldId, invSlots)
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
