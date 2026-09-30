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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gameplay.GameViewModel
import com.example.model.ItemStack

@Composable
fun ChestModal(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val chestInv by viewModel.chestInventory.collectAsState()
    val playerInv by viewModel.inventory.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF3E2723))
                .border(3.dp, Color(0xFF8D6E63), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 COFRE DE MADERA",
                        color = Color(0xFFFFD54F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chest 27 slots (3 rows of 9)
                Text("Almacén del Cofre (Toca para retirar)", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(9),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(130.dp)
                ) {
                    items(27) { idx ->
                        val stack = chestInv[idx]
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF261814))
                                .border(1.dp, Color(0xFFA1887F), RoundedCornerShape(4.dp))
                                .clickable {
                                    if (!stack.isEmpty) {
                                        if (viewModel.addItemToInventory(stack)) {
                                            val c = viewModel.chestInventory.value.copyOf()
                                            c[idx] = ItemStack.EMPTY
                                            viewModel.chestInventory.value = c
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!stack.isEmpty) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(stack.def.iconColor), RoundedCornerShape(3.dp))
                                )
                                if (stack.count > 1) {
                                    Text(
                                        text = "${stack.count}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Player Inventory 36 slots (Toca para guardar en cofre)
                Text("Tu Inventario (Toca para guardar en el cofre)", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(9),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(36) { idx ->
                        val stack = playerInv[idx]
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (idx < 9) Color(0xFF4E342E) else Color(0xFF212121))
                                .border(1.dp, Color(0xFF616161), RoundedCornerShape(4.dp))
                                .clickable {
                                    if (!stack.isEmpty) {
                                        // Transfer to chest
                                        val c = viewModel.chestInventory.value.copyOf()
                                        for (ci in c.indices) {
                                            if (c[ci].isEmpty) {
                                                c[ci] = stack.copy()
                                                val p = viewModel.inventory.value.copyOf()
                                                p[idx] = ItemStack.EMPTY
                                                viewModel.inventory.value = p
                                                viewModel.chestInventory.value = c
                                                break
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!stack.isEmpty) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(stack.def.iconColor), RoundedCornerShape(3.dp))
                                )
                                if (stack.count > 1) {
                                    Text(
                                        text = "${stack.count}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}
