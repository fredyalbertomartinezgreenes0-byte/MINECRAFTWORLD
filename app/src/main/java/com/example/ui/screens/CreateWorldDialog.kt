package com.example.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.WorldEntity
import com.example.model.GameMode
import com.example.ui.theme.MinecraftButton
import com.example.ui.theme.MinecraftColors

@Composable
fun CreateWorldDialog(
    onDismiss: () -> Unit,
    onCreateWorld: (WorldEntity) -> Unit
) {
    var worldName by remember { mutableStateOf("Nuevo Mundo") }
    var seedText by remember { mutableStateOf("${(10000..99999).random()}") }
    var selectedMode by remember { mutableStateOf(GameMode.SURVIVAL) }
    var startInVoidlands by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(440.dp)
                .drawBehind {
                    val strokeW = 3.dp.toPx()
                    drawRect(MinecraftColors.GuiBorderLight, Offset.Zero, Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderLight, Offset.Zero, Size(strokeW, size.height))
                    drawRect(MinecraftColors.GuiBorderShadow, Offset(0f, size.height - strokeW), Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderShadow, Offset(size.width - strokeW, 0f), Size(strokeW, size.height))
                }
                .background(MinecraftColors.GuiBackground, RoundedCornerShape(2.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                Box {
                    Text(
                        text = "Crear Mundo Nuevo",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.dp, 1.dp)
                    )
                    Text(
                        text = "Crear Mundo Nuevo",
                        color = MinecraftColors.TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Name input
                OutlinedTextField(
                    value = worldName,
                    onValueChange = { worldName = it },
                    label = { Text("Nombre del mundo", fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedContainerColor = Color(0xFFAAAAAA),
                        unfocusedContainerColor = Color(0xFFAAAAAA)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("world_name_input")
                )

                // Seed input
                OutlinedTextField(
                    value = seedText,
                    onValueChange = { seedText = it },
                    label = { Text("Semilla para el generador", fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedContainerColor = Color(0xFFAAAAAA),
                        unfocusedContainerColor = Color(0xFFAAAAAA)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("seed_input")
                )

                // Game Mode Toggle
                MinecraftButton(
                    text = "Modo de Juego: ${selectedMode.displayName}",
                    onClick = {
                        selectedMode = when (selectedMode) {
                            GameMode.SURVIVAL -> GameMode.CREATIVE
                            GameMode.CREATIVE -> GameMode.ADVENTURE
                            GameMode.ADVENTURE -> GameMode.SURVIVAL
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Dimension Toggle
                MinecraftButton(
                    text = if (startInVoidlands) "Dimensión: 🌌 The Voidlands" else "Dimensión: 🌍 Overworld",
                    onClick = { startInVoidlands = !startInVoidlands },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftButton(
                        text = "Crear Mundo Nuevo",
                        onClick = {
                            val seed = seedText.toLongOrNull() ?: System.currentTimeMillis()
                            val world = WorldEntity(
                                name = worldName.ifBlank { "Nuevo Mundo" },
                                seed = seed,
                                gameMode = selectedMode.name,
                                dimension = if (startInVoidlands) "voidlands" else "overworld",
                                playerX = 0f,
                                playerY = if (startInVoidlands) 35f else 22f,
                                playerZ = 0f,
                                lastPlayed = System.currentTimeMillis()
                            )
                            onCreateWorld(world)
                        },
                        modifier = Modifier.weight(1.2f),
                        tag = "confirm_create_world_button"
                    )

                    MinecraftButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f),
                        tag = "cancel_create_world_button"
                    )
                }
            }
        }
    }
}
