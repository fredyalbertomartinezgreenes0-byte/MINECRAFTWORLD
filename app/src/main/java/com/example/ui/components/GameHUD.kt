package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameplay.GameViewModel
import com.example.model.Dimension
import com.example.model.GameMode
import com.example.ui.theme.MinecraftColors
import com.example.ui.theme.MinecraftSlot
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun GameHUD(
    viewModel: GameViewModel,
    onOpenChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stats by viewModel.playerStats.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val dimension by viewModel.dimension.collectAsState()
    val selectedIndex by viewModel.selectedHotbarIndex.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val breakProgress by viewModel.breakProgress.collectAsState()
    val bossEntity by viewModel.voidKingBoss.collectAsState()
    val isThirdPerson by viewModel.isThirdPerson.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {

        // --- TOP BAR: Coordinates, Pause, Perspective, Boss Bar (16:9 Landscape) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
        ) {
            // Left: Position Coordinates (classic Minecraft Bedrock top-left format)
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Box {
                    Text(
                        text = "Posición: ${floor(viewModel.playerPos.x).toInt()}, ${floor(viewModel.playerPos.y).toInt()}, ${floor(viewModel.playerPos.z).toInt()}",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.dp, 1.dp)
                    )
                    Text(
                        text = "Posición: ${floor(viewModel.playerPos.x).toInt()}, ${floor(viewModel.playerPos.y).toInt()}, ${floor(viewModel.playerPos.z).toInt()}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (dimension == Dimension.VOIDLANDS) {
                    Box(modifier = Modifier.padding(top = 2.dp)) {
                        Text(
                            text = "Dimensión: The Voidlands",
                            color = Color(0xFFE040FB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Center Top: VOID KING Boss Bar (when in The Voidlands)
            if (dimension == Dimension.VOIDLANDS && bossEntity != null && !bossEntity!!.isDead) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(360.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Text(
                            text = "Void King (Fase ${bossEntity!!.bossPhase})",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.offset(1.dp, 1.dp)
                        )
                        Text(
                            text = "Void King (Fase ${bossEntity!!.bossPhase})",
                            color = Color(0xFFFF55FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    // Minecraft textured boss bar
                    val hpRatio = (bossEntity!!.health / bossEntity!!.type.maxHealth).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .border(1.dp, Color.Black),
                        color = Color(0xFFCC00CC),
                        trackColor = Color(0xFF330033)
                    )
                }
            }

            // Right: Chat, Perspective & Pause Buttons
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chat and Commands Button
                MinecraftIconButton(
                    onClick = onOpenChat,
                    tag = "chat_button"
                ) {
                    Text("💬", fontSize = 16.sp)
                }

                // Perspective Toggle
                MinecraftIconButton(
                    onClick = { viewModel.isThirdPerson.value = !isThirdPerson },
                    tag = "camera_view_button"
                ) {
                    Icon(Icons.Default.ViewInAr, contentDescription = "Cámara", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                // Pause Button (Iconic Bedrock pause button with two bars)
                MinecraftIconButton(
                    onClick = { viewModel.isPauseMenuOpen.value = true },
                    tag = "pause_button"
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pausa", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // --- CENTER CROSSHAIR & MINING PROGRESS ---
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            // Authentic Minecraft Crosshair (+)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .drawBehind {
                        val midX = size.width / 2f
                        val midY = size.height / 2f
                        val w = 2.dp.toPx()
                        // Cross lines
                        drawRect(Color.White.copy(alpha = 0.8f), Offset(midX - 7.dp.toPx(), midY - w / 2f), Size(14.dp.toPx(), w))
                        drawRect(Color.White.copy(alpha = 0.8f), Offset(midX - w / 2f, midY - 7.dp.toPx()), Size(w, 14.dp.toPx()))
                    }
            )

            // Mining progress indicator
            if (breakProgress > 0f) {
                LinearProgressIndicator(
                    progress = { breakProgress },
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .offset(y = 20.dp)
                        .border(1.dp, Color.Black),
                    color = Color(0xFFFF5722),
                    trackColor = Color.Black.copy(alpha = 0.6f)
                )
            }
        }

        // --- BOTTOM LEFT: Authentic Minecraft Pocket Edition D-Pad ---
        MinecraftDPad(
            onMove = { forward, strafe ->
                viewModel.onMoveInput(forward, strafe)
            },
            onCenterSneak = {
                viewModel.toggleSneak()
            },
            isSneaking = stats.isSneaking,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 20.dp)
        )

        // --- BOTTOM RIGHT: Bedrock Action Buttons (Jump, Attack, Mine, Place, Fly) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Creative flight controls
            if (gameMode == GameMode.CREATIVE) {
                BedrockActionButton(
                    icon = Icons.Default.Flight,
                    label = "Vuelo",
                    isActive = stats.isFlying,
                    onClick = { viewModel.toggleFly() }
                )
                if (stats.isFlying) {
                    BedrockActionButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "Bajar",
                        onClick = { viewModel.flyDown() }
                    )
                }
            }

            // Attack (Sword / Hit)
            BedrockActionButton(
                icon = Icons.Default.FlashOn,
                label = "Atacar",
                onClick = { viewModel.attack() }
            )

            // Place Block / Use
            BedrockActionButton(
                icon = Icons.Default.ViewInAr,
                label = "Colocar",
                onClick = { viewModel.placeBlockOrUseItem() }
            )

            // Mine / Break Block
            BedrockActionButton(
                icon = Icons.Default.Build,
                label = "Picar",
                onClick = { viewModel.startMining() }
            )

            // Jump (Classic Bedrock Jump circle)
            BedrockActionButton(
                icon = Icons.Default.KeyboardArrowUp,
                label = "Saltar",
                size = 54,
                onClick = { viewModel.jump() }
            )
        }

        // --- BOTTOM CENTER: Authentic Minecraft Hotbar, XP Bar, Hearts & Food (16:9 Landscape) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Survival Health, Hunger & Armor Row (Directly above XP Bar)
            if (gameMode == GameMode.SURVIVAL) {
                Row(
                    modifier = Modifier.width(360.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: 10 Hearts (Health)
                    Row {
                        val hearts = (stats.health / 2f).roundToInt().coerceIn(0, 10)
                        repeat(10) { i ->
                            Text(
                                text = if (i < hearts) "❤️" else "🖤",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 0.5.dp)
                            )
                        }
                    }

                    // Right: 10 Drumsticks (Hunger)
                    Row {
                        val drumsticks = (stats.hunger / 2f).roundToInt().coerceIn(0, 10)
                        repeat(10) { i ->
                            Text(
                                text = if (i < drumsticks) "🍗" else "🦴",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 0.5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Green Experience Bar (Directly between Hearts and Hotbar)
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(5.dp)
                        .background(MinecraftColors.ExpBarBackground)
                        .border(1.dp, Color.Black)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(5.dp)
                            .background(MinecraftColors.ExpBarGreen)
                    )
                }

                // Centered Experience Level Number ("7")
                Box(modifier = Modifier.offset(y = (-4).dp)) {
                    Text(
                        text = "${stats.level}",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.dp, 1.dp)
                    )
                    Text(
                        text = "${stats.level}",
                        color = MinecraftColors.ExpLevelNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // The Classic 9-Slot Connected Hotbar + Bedrock 3-Dots Inventory Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(MinecraftColors.GuiBackground, RoundedCornerShape(2.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    for (i in 0 until 9) {
                        val stack = inventory[i]
                        val isSelected = (i == selectedIndex)

                        MinecraftSlot(
                            stack = stack,
                            isSelected = isSelected,
                            size = 38.dp,
                            onClick = { viewModel.selectHotbarSlot(i) },
                            tag = "hotbar_slot_$i"
                        )
                    }
                }

                // Bedrock 3-Dots Button to open Inventory
                MinecraftIconButton(
                    onClick = { viewModel.isInventoryOpen.value = true },
                    tag = "hotbar_inventory_dots_button"
                ) {
                    Text(
                        text = "···",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Authentic Minecraft Bedrock Translucent D-Pad for Left Thumb
 */
@Composable
fun MinecraftDPad(
    onMove: (forward: Float, strafe: Float) -> Unit,
    onCenterSneak: () -> Unit,
    isSneaking: Boolean,
    modifier: Modifier = Modifier
) {
    val dpadBg = Color(0x66000000)
    val dpadBorder = Color(0x88FFFFFF)

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // UP
        Box(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.TopCenter)
                .background(dpadBg, RoundedCornerShape(4.dp))
                .border(1.dp, dpadBorder, RoundedCornerShape(4.dp))
                .clickable { onMove(1f, 0f) }
                .testTag("dpad_up"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Arriba", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        // DOWN
        Box(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.BottomCenter)
                .background(dpadBg, RoundedCornerShape(4.dp))
                .border(1.dp, dpadBorder, RoundedCornerShape(4.dp))
                .clickable { onMove(-1f, 0f) }
                .testTag("dpad_down"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Abajo", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        // LEFT
        Box(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.CenterStart)
                .background(dpadBg, RoundedCornerShape(4.dp))
                .border(1.dp, dpadBorder, RoundedCornerShape(4.dp))
                .clickable { onMove(0f, -1f) }
                .testTag("dpad_left"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Izquierda", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        // RIGHT
        Box(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.CenterEnd)
                .background(dpadBg, RoundedCornerShape(4.dp))
                .border(1.dp, dpadBorder, RoundedCornerShape(4.dp))
                .clickable { onMove(0f, 1f) }
                .testTag("dpad_right"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Derecha", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        // CENTER (Sneak Toggle Dot)
        Box(
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center)
                .background(if (isSneaking) Color(0xAAFFFFFF) else dpadBg, CircleShape)
                .border(1.dp, dpadBorder, CircleShape)
                .clickable(onClick = onCenterSneak)
                .testTag("dpad_sneak"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (isSneaking) Color.Black else Color.White, CircleShape)
            )
        }
    }
}

/**
 * Bedrock translucent action button
 */
@Composable
fun BedrockActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    size: Int = 46,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(if (isActive) Color(0x9900E676) else Color(0x66000000), CircleShape)
            .border(1.5.dp, Color(0x88FFFFFF), CircleShape)
            .clickable(onClick = onClick)
            .testTag("action_${label.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size((size * 0.52f).dp)
        )
    }
}

/**
 * Minecraft beveled square icon button (e.g. for pause/camera)
 */
@Composable
fun MinecraftIconButton(
    onClick: () -> Unit,
    tag: String = "",
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .drawBehind {
                val strokeW = 2.dp.toPx()
                drawRect(Color(0xFFB0B0B0), Offset.Zero, Size(size.width, strokeW))
                drawRect(Color(0xFFB0B0B0), Offset.Zero, Size(strokeW, size.height))
                drawRect(MinecraftColors.GuiBorderDark, Offset(0f, size.height - strokeW), Size(size.width, strokeW))
                drawRect(MinecraftColors.GuiBorderDark, Offset(size.width - strokeW, 0f), Size(strokeW, size.height))
            }
            .background(MinecraftColors.ButtonBase, RoundedCornerShape(2.dp))
            .border(1.5.dp, Color.Black, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
