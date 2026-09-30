package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gameplay.GameViewModel
import com.example.model.ItemStack
import com.example.ui.theme.MinecraftButton
import com.example.ui.theme.MinecraftColors
import com.example.ui.theme.MinecraftSlot

@Composable
fun FurnaceModal(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val input by viewModel.furnaceInput.collectAsState()
    val fuel by viewModel.furnaceFuel.collectAsState()
    val output by viewModel.furnaceOutput.collectAsState()
    val progress by viewModel.furnaceProgress.collectAsState()
    val inventory by viewModel.inventory.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(480.dp)
                .drawBehind {
                    val strokeW = 3.dp.toPx()
                    drawRect(MinecraftColors.GuiBorderLight, Offset.Zero, Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderLight, Offset.Zero, Size(strokeW, size.height))
                    drawRect(MinecraftColors.GuiBorderShadow, Offset(0f, size.height - strokeW), Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderShadow, Offset(size.width - strokeW, 0f), Size(strokeW, size.height))
                }
                .background(MinecraftColors.GuiBackground, RoundedCornerShape(2.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horno",
                        color = MinecraftColors.TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MinecraftColors.TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Smelting Layout (Input over Fuel -> Flame -> Arrow -> Output)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Input and Fuel
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MinecraftSlot(
                            stack = input,
                            size = 38.dp,
                            onClick = { cycleFurnaceInput(viewModel) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Fuego",
                            tint = if (progress > 0f) Color(0xFFFF5722) else Color(0xFF666666),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        MinecraftSlot(
                            stack = fuel,
                            size = 38.dp,
                            onClick = { cycleFurnaceFuel(viewModel) }
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Center: Progress arrow
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(60.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .border(1.dp, Color.Black),
                            color = Color(0xFFFF9800),
                            trackColor = Color(0xFF8B8B8B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("➡️", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Right: Output Slot
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MinecraftSlot(
                            stack = output,
                            size = 46.dp,
                            onClick = {
                                if (!output.isEmpty) {
                                    viewModel.addItemToInventory(output)
                                    viewModel.furnaceOutput.value = ItemStack.EMPTY
                                }
                            },
                            tag = "furnace_output_slot"
                        )
                        if (!output.isEmpty) {
                            Text(
                                text = output.def.name,
                                color = MinecraftColors.TextDark,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                modifier = Modifier.width(60.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Player Inventory (3x9) + Hotbar (1x9)
                Text(
                    text = "Inventario",
                    color = MinecraftColors.TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(2.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until 9) {
                                val idx = 9 + row * 9 + col
                                MinecraftSlot(
                                    stack = inventory[idx],
                                    onClick = {
                                        val cur = inventory[idx]
                                        if (cur.itemId in listOf("raw_iron", "raw_gold", "raw_pork", "sand", "cobblestone", "void_ore_raw")) {
                                            viewModel.furnaceInput.value = cur.copy()
                                            val inv = viewModel.inventory.value.copyOf()
                                            inv[idx] = ItemStack.EMPTY
                                            viewModel.inventory.value = inv
                                        } else if (cur.itemId in listOf("coal", "oak_planks", "oak_log", "stick")) {
                                            viewModel.furnaceFuel.value = cur.copy()
                                            val inv = viewModel.inventory.value.copyOf()
                                            inv[idx] = ItemStack.EMPTY
                                            viewModel.inventory.value = inv
                                        }
                                    },
                                    size = 36.dp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hotbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 9) {
                        MinecraftSlot(
                            stack = inventory[col],
                            size = 36.dp,
                            onClick = {}
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                MinecraftButton(
                    text = "Cerrar",
                    height = 32.dp,
                    fontSize = 11.sp,
                    onClick = onDismiss
                )
            }
        }
    }
}

private fun cycleFurnaceInput(viewModel: GameViewModel) {
    val inv = viewModel.inventory.value.copyOf()
    val cur = viewModel.furnaceInput.value
    if (!cur.isEmpty) {
        viewModel.addItemToInventory(cur)
        viewModel.furnaceInput.value = ItemStack.EMPTY
        return
    }

    val smeltables = listOf("raw_iron", "raw_gold", "raw_pork", "sand", "cobblestone", "void_ore_raw")
    for (i in inv.indices) {
        if (inv[i].itemId in smeltables) {
            viewModel.furnaceInput.value = inv[i].copy()
            inv[i] = ItemStack.EMPTY
            viewModel.inventory.value = inv
            return
        }
    }
}

private fun cycleFurnaceFuel(viewModel: GameViewModel) {
    val inv = viewModel.inventory.value.copyOf()
    val cur = viewModel.furnaceFuel.value
    if (!cur.isEmpty) {
        viewModel.addItemToInventory(cur)
        viewModel.furnaceFuel.value = ItemStack.EMPTY
        return
    }

    val fuels = listOf("coal", "oak_planks", "oak_log", "stick")
    for (i in inv.indices) {
        if (inv[i].itemId in fuels) {
            viewModel.furnaceFuel.value = inv[i].copy()
            inv[i] = ItemStack.EMPTY
            viewModel.inventory.value = inv
            return
        }
    }
}
