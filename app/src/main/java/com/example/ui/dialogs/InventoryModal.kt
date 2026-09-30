package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun InventoryModal(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val inventory by viewModel.inventory.collectAsState()
    val craftingGrid by viewModel.craftingGrid.collectAsState()
    val craftingResult by viewModel.craftingResult.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        // Authentic Minecraft Grey GUI Window
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
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inventario y Fabricación",
                        color = MinecraftColors.TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp).testTag("close_inventory_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MinecraftColors.TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TOP ROW: 4 Armor slots + Character Paper Doll + 2x2 Crafting + Result
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 4 Armor Slots on the left
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        MinecraftSlot(stack = inventory[36], onClick = { unequipArmor(viewModel, 36) }, size = 32.dp)
                        MinecraftSlot(stack = inventory[37], onClick = { unequipArmor(viewModel, 37) }, size = 32.dp)
                        MinecraftSlot(stack = inventory[38], onClick = { unequipArmor(viewModel, 38) }, size = 32.dp)
                        MinecraftSlot(stack = inventory[39], onClick = { unequipArmor(viewModel, 39) }, size = 32.dp)
                    }

                    // Player Model Paper Doll Box
                    Box(
                        modifier = Modifier
                            .size(72.dp, 134.dp)
                            .background(Color(0xFF1E1E1E))
                            .border(2.dp, Color(0xFF373737), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👤", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Jugador",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // 2x2 Crafting Grid -> Arrow -> Output
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                text = "Fabricación",
                                color = MinecraftColors.TextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // 2x2 Grid
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    MinecraftSlot(stack = craftingGrid[0], onClick = { cycleGridItem(viewModel, 0) }, size = 34.dp)
                                    MinecraftSlot(stack = craftingGrid[1], onClick = { cycleGridItem(viewModel, 1) }, size = 34.dp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    MinecraftSlot(stack = craftingGrid[3], onClick = { cycleGridItem(viewModel, 3) }, size = 34.dp)
                                    MinecraftSlot(stack = craftingGrid[4], onClick = { cycleGridItem(viewModel, 4) }, size = 34.dp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Text("➡️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))

                        // Result Slot
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MinecraftSlot(
                                stack = craftingResult,
                                onClick = {
                                    if (!craftingResult.isEmpty) {
                                        viewModel.craftItem()
                                    }
                                },
                                size = 42.dp,
                                tag = "craft_result_button"
                            )
                            if (!craftingResult.isEmpty) {
                                Text(
                                    text = craftingResult.def.name,
                                    color = MinecraftColors.TextDark,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(60.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // BOTTOM AREA: 3x9 Main Inventory (slots 9..35)
                Text(
                    text = "Inventario",
                    color = MinecraftColors.TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(3.dp))

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
                                    onClick = { putInCraftingOrEquip(viewModel, idx) },
                                    size = 38.dp,
                                    tag = "inv_slot_$idx"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 1x9 Hotbar Slots (0..8) separated like in real Minecraft
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 9) {
                        MinecraftSlot(
                            stack = inventory[col],
                            onClick = { putInCraftingOrEquip(viewModel, col) },
                            size = 38.dp,
                            tag = "inv_hotbar_$col"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clear Grid Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    MinecraftButton(
                        text = "Limpiar Rejilla",
                        height = 32.dp,
                        fontSize = 11.sp,
                        onClick = {
                            val grid = viewModel.craftingGrid.value
                            for (s in grid) {
                                if (!s.isEmpty) viewModel.addItemToInventory(s)
                            }
                            viewModel.craftingGrid.value = Array(9) { ItemStack.EMPTY }
                            viewModel.updateCraftingResult()
                        },
                        tag = "clear_crafting_button"
                    )
                }
            }
        }
    }
}

private fun cycleGridItem(viewModel: GameViewModel, gridIndex: Int) {
    val grid = viewModel.craftingGrid.value.copyOf()
    val cur = grid[gridIndex]
    if (!cur.isEmpty) {
        viewModel.addItemToInventory(cur)
        grid[gridIndex] = ItemStack.EMPTY
        viewModel.craftingGrid.value = grid
        viewModel.updateCraftingResult()
    }
}

private fun putInCraftingOrEquip(viewModel: GameViewModel, invIndex: Int) {
    val inv = viewModel.inventory.value.copyOf()
    val stack = inv[invIndex]
    if (stack.isEmpty) return

    val grid = viewModel.craftingGrid.value.copyOf()
    val slots2x2 = listOf(0, 1, 3, 4)
    for (s in slots2x2) {
        if (grid[s].isEmpty) {
            grid[s] = ItemStack(stack.itemId, 1)
            viewModel.craftingGrid.value = grid
            if (stack.count > 1) {
                inv[invIndex] = stack.copy(count = stack.count - 1)
            } else {
                inv[invIndex] = ItemStack.EMPTY
            }
            viewModel.inventory.value = inv
            viewModel.updateCraftingResult()
            return
        }
    }
}

private fun unequipArmor(viewModel: GameViewModel, slotIdx: Int) {
    val inv = viewModel.inventory.value.copyOf()
    val cur = inv[slotIdx]
    if (!cur.isEmpty) {
        if (viewModel.addItemToInventory(cur)) {
            inv[slotIdx] = ItemStack.EMPTY
            viewModel.inventory.value = inv
        }
    }
}
