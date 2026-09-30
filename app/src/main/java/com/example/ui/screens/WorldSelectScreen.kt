package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.WorldEntity
import com.example.ui.theme.MinecraftButton
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorldSelectScreen(
    worldsFlow: Flow<List<WorldEntity>>,
    onSelectWorld: (WorldEntity) -> Unit,
    onDeleteWorld: (Long) -> Unit,
    onCreateNewWorld: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val worlds by worldsFlow.collectAsState(initial = emptyList())
    var selectedWorld by remember { mutableStateOf<WorldEntity?>(null) }

    BackHandler { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E140C)) // Dirt background tone
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Box(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Seleccionar Mundo",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.offset(1.5.dp, 1.5.dp)
                )
                Text(
                    text = "Seleccionar Mundo",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // World List Area (darkened central list box like Minecraft)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    .border(2.dp, Color(0xFF373737), RoundedCornerShape(2.dp))
                    .padding(8.dp)
            ) {
                if (worlds.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No se encontraron mundos. ¡Crea uno nuevo!",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(worlds, key = { it.id }) { world ->
                            val isSelected = (selectedWorld?.id == world.id)
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val dateStr = dateFormat.format(Date(world.lastPlayed))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) Color(0xFF333333) else Color(0xFF1A1A1A),
                                        RoundedCornerShape(2.dp)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) Color.White else Color(0xFF4A4A4A),
                                        RoundedCornerShape(2.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            onSelectWorld(world)
                                        } else {
                                            selectedWorld = world
                                        }
                                    }
                                    .padding(10.dp)
                                    .testTag("world_item_${world.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = world.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            if (world.dimension == "voidlands") {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "🌌 THE VOIDLANDS",
                                                    color = Color(0xFFFF55FF),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${world.name} ($dateStr)",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Modo: ${world.gameMode} • Semilla: ${world.seed}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    if (world.bossDefeated) {
                                        Text("👑", fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Buttons (Authentic 16:9 Minecraft 2x2 or 4-button layout)
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinecraftButton(
                    text = "Jugar en el Mundo",
                    enabled = (selectedWorld != null || worlds.isNotEmpty()),
                    onClick = {
                        val toPlay = selectedWorld ?: worlds.firstOrNull()
                        if (toPlay != null) onSelectWorld(toPlay)
                    },
                    modifier = Modifier.weight(1f),
                    tag = "play_selected_world_button"
                )

                MinecraftButton(
                    text = "Crear Mundo Nuevo",
                    onClick = onCreateNewWorld,
                    modifier = Modifier.weight(1f),
                    tag = "create_new_world_button"
                )

                MinecraftButton(
                    text = "Eliminar",
                    enabled = (selectedWorld != null),
                    onClick = {
                        selectedWorld?.let {
                            onDeleteWorld(it.id)
                            selectedWorld = null
                        }
                    },
                    modifier = Modifier.weight(0.7f),
                    tag = "delete_selected_world_button"
                )

                MinecraftButton(
                    text = "Cancelar",
                    onClick = onBack,
                    modifier = Modifier.weight(0.7f),
                    tag = "world_select_cancel_button"
                )
            }
        }
    }
}
