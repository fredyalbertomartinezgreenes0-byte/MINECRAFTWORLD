package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.crafting.CraftingManager
import com.example.crafting.CraftingRecipe
import com.example.gameplay.GameViewModel
import com.example.model.ItemStack
import com.example.ui.theme.MinecraftButton
import com.example.ui.theme.MinecraftColors
import com.example.ui.theme.MinecraftSlot

@Composable
fun CraftingModal(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val craftingGrid by viewModel.craftingGrid.collectAsState()
    val craftingResult by viewModel.craftingResult.collectAsState()
    val inventory by viewModel.inventory.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(540.dp)
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mesa de Trabajo",
                        color = MinecraftColors.TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp).testTag("close_crafting_table_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MinecraftColors.TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // TOP ROW: 3x3 Grid and Recipe Book sidebar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // LEFT: Recipe Book (Quick one-click crafting)
                    Column(
                        modifier = Modifier
                            .width(180.dp)
                            .height(130.dp)
                            .background(Color(0xFFB0B0B0), RoundedCornerShape(2.dp))
                            .border(1.5.dp, Color(0xFF555555), RoundedCornerShape(2.dp))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "📖 Recetario",
                            color = MinecraftColors.TextDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(CraftingManager.recipes) { recipe ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF8B8B8B), RoundedCornerShape(2.dp))
                                        .clickable { applyRecipe(viewModel, recipe) }
                                        .padding(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(Color(recipe.output.def.iconColor), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = recipe.name,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // CENTER & RIGHT: 3x3 Grid -> Arrow -> Output
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        // 3x3 Grid
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (row in 0 until 3) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    for (col in 0 until 3) {
                                        val idx = row * 3 + col
                                        MinecraftSlot(
                                            stack = craftingGrid[idx],
                                            size = 36.dp,
                                            onClick = {
                                                val grid = viewModel.craftingGrid.value.copyOf()
                                                if (!grid[idx].isEmpty) {
                                                    viewModel.addItemToInventory(grid[idx])
                                                    grid[idx] = ItemStack.EMPTY
                                                    viewModel.craftingGrid.value = grid
                                                    viewModel.updateCraftingResult()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))
                        Text("➡️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(14.dp))

                        // Output slot
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MinecraftSlot(
                                stack = craftingResult,
                                size = 46.dp,
                                onClick = {
                                    if (!craftingResult.isEmpty) {
                                        viewModel.craftItem()
                                    }
                                },
                                tag = "craft_table_result_button"
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

                Spacer(modifier = Modifier.height(8.dp))

                // BOTTOM AREA: Player Inventory (3x9) + Hotbar (1x9)
                Text(
                    text = "Inventario",
                    color = MinecraftColors.TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
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
                                    onClick = { putIn3x3Crafting(viewModel, idx) },
                                    size = 36.dp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hotbar Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 9) {
                        MinecraftSlot(
                            stack = inventory[col],
                            onClick = { putIn3x3Crafting(viewModel, col) },
                            size = 36.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MinecraftButton(
                        text = "Limpiar Rejilla",
                        height = 30.dp,
                        fontSize = 10.sp,
                        onClick = {
                            val grid = viewModel.craftingGrid.value
                            for (s in grid) {
                                if (!s.isEmpty) viewModel.addItemToInventory(s)
                            }
                            viewModel.craftingGrid.value = Array(9) { ItemStack.EMPTY }
                            viewModel.updateCraftingResult()
                        }
                    )

                    MinecraftButton(
                        text = "Listo",
                        height = 30.dp,
                        fontSize = 10.sp,
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

private fun putIn3x3Crafting(viewModel: GameViewModel, invIdx: Int) {
    val inv = viewModel.inventory.value.copyOf()
    val stack = inv[invIdx]
    if (stack.isEmpty) return

    val grid = viewModel.craftingGrid.value.copyOf()
    for (i in 0 until 9) {
        if (grid[i].isEmpty) {
            grid[i] = ItemStack(stack.itemId, 1)
            viewModel.craftingGrid.value = grid
            if (stack.count > 1) {
                inv[invIdx] = stack.copy(count = stack.count - 1)
            } else {
                inv[invIdx] = ItemStack.EMPTY
            }
            viewModel.inventory.value = inv
            viewModel.updateCraftingResult()
            return
        }
    }
}

private fun applyRecipe(viewModel: GameViewModel, recipe: CraftingRecipe) {
    val inv = viewModel.inventory.value.copyOf()
    val newGrid = Array(9) { ItemStack.EMPTY }

    val neededMap = mutableMapOf<String, Int>()
    for (itemId in recipe.pattern) {
        if (itemId != "air") {
            neededMap[itemId] = (neededMap[itemId] ?: 0) + 1
        }
    }

    for ((itemId, neededCount) in neededMap) {
        val available = inv.filter { it.itemId == itemId }.sumOf { it.count }
        if (available < neededCount) return
    }

    for ((itemId, neededCount) in neededMap) {
        var remaining = neededCount
        for (i in inv.indices) {
            if (inv[i].itemId == itemId && remaining > 0) {
                if (inv[i].count <= remaining) {
                    remaining -= inv[i].count
                    inv[i] = ItemStack.EMPTY
                } else {
                    inv[i] = inv[i].copy(count = inv[i].count - remaining)
                    remaining = 0
                }
            }
        }
    }

    for (r in 0 until recipe.gridHeight) {
        for (c in 0 until recipe.gridWidth) {
            val itemId = recipe.pattern[r * recipe.gridWidth + c]
            newGrid[r * 3 + c] = if (itemId != "air") ItemStack(itemId, 1) else ItemStack.EMPTY
        }
    }

    viewModel.inventory.value = inv
    viewModel.craftingGrid.value = newGrid
    viewModel.updateCraftingResult()
}
