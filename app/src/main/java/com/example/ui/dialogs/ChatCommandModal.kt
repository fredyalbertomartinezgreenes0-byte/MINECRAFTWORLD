package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gameplay.GameViewModel
import com.example.model.GameMode
import com.example.model.ItemStack
import com.example.model.Weather

@Composable
fun ChatCommandModal(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            "§eBienvenido a MineCraftWorld — The Voidlands 16:9",
            "§7Usa los botones rápidos o escribe comandos como /gamemode, /time, /tp",
            "§aServidor local iniciado correctamente."
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(340.dp)
                .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                .border(2.dp, Color(0xFF555555), RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💬 Chat y Comandos",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_chat_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Chat Log
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(chatMessages) { msg ->
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Quick Command Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CommandChip("/gamemode c") {
                        executeCommand("/gamemode creative", viewModel, chatMessages)
                    }
                    CommandChip("/gamemode s") {
                        executeCommand("/gamemode survival", viewModel, chatMessages)
                    }
                    CommandChip("/time set day") {
                        executeCommand("/time set day", viewModel, chatMessages)
                    }
                    CommandChip("/time set night") {
                        executeCommand("/time set night", viewModel, chatMessages)
                    }
                    CommandChip("/tp voidlands") {
                        executeCommand("/tp voidlands", viewModel, chatMessages)
                    }
                    CommandChip("/give diamond") {
                        executeCommand("/give diamond", viewModel, chatMessages)
                    }
                }

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Escribe un mensaje o /comando...", color = Color.Gray, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                executeCommand(inputText, viewModel, chatMessages)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFF5E35B1), RoundedCornerShape(4.dp))
                            .testTag("send_chat_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CommandChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color(0xFF333333), RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFF666666), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFFFD54F),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun executeCommand(cmd: String, viewModel: GameViewModel, chatMessages: MutableList<String>) {
    chatMessages.add("§b> $cmd")
    when {
        cmd.contains("creative") -> {
            viewModel.loadOrInitWorld(
                com.example.data.WorldEntity(
                    id = viewModel.currentWorldId,
                    name = viewModel.worldName.value,
                    seed = viewModel.voxelWorld.seed,
                    gameMode = "CREATIVE"
                )
            )
            chatMessages.add("§aTu modo de juego se ha actualizado a Modo Creativo.")
        }
        cmd.contains("survival") -> {
            viewModel.loadOrInitWorld(
                com.example.data.WorldEntity(
                    id = viewModel.currentWorldId,
                    name = viewModel.worldName.value,
                    seed = viewModel.voxelWorld.seed,
                    gameMode = "SURVIVAL"
                )
            )
            chatMessages.add("§aTu modo de juego se ha actualizado a Modo Supervivencia.")
        }
        cmd.contains("day") -> {
            chatMessages.add("§eEstableciendo la hora en el día (1000 ticks).")
        }
        cmd.contains("night") -> {
            chatMessages.add("§9Estableciendo la hora en la noche (13000 ticks).")
        }
        cmd.contains("voidlands") -> {
            viewModel.warpDimension()
            chatMessages.add("§dTeletransportándote a The Voidlands...")
        }
        cmd.contains("diamond") -> {
            viewModel.addItemToInventory(ItemStack("diamond", 16))
            chatMessages.add("§bHas recibido 16 Diamantes.")
        }
        else -> {
            chatMessages.add("§f[Jugador] $cmd")
        }
    }
}
