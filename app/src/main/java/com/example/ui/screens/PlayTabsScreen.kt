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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun PlayTabsScreen(
    worldsFlow: Flow<List<WorldEntity>>,
    onSelectWorld: (WorldEntity) -> Unit,
    onDeleteWorld: (Long) -> Unit,
    onCreateNewWorld: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val worlds by worldsFlow.collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mundos, 1: Amigos/Realms, 2: Servidores
    var selectedWorld by remember { mutableStateOf<WorldEntity?>(null) }

    val servers = listOf(
        ServerEntry("v1", "VoidCraft Oficial [END UPDATE]", "play.voidcraft.net", "92/100", 18, "Servidor oficial de The Voidlands con jefes comunitarios.", "voidlands"),
        ServerEntry("v2", "Survival SMP Español", "smp.minecraftworld.es", "48/60", 28, "Supervivencia pura, economía y clanes activos.", "overworld"),
        ServerEntry("v3", "SkyBlock Dimensions", "sky.voidlands.gg", "145/200", 14, "Islas flotantes en el abismo y desafíos celestiales.", "voidlands"),
        ServerEntry("v4", "Constructores Creativos", "build.craftworld.io", "32/50", 35, "Vuelo libre y parcelas gigantes para tus obras.", "overworld")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E140C)) // Dirt background
            .padding(horizontal = 32.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Back button and 3 Bedrock Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("play_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // The 3 Bedrock Tabs: Mundos | Amigos / Realms | Servidores
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PlayTabButton(
                        title = "Mundos",
                        badge = "${worlds.size}",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PlayTabButton(
                        title = "Amigos / Realms",
                        badge = "2",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PlayTabButton(
                        title = "Servidores",
                        badge = "${servers.size}",
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }
            }

            // Central Area
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    .border(2.dp, Color(0xFF373737), RoundedCornerShape(2.dp))
                    .padding(10.dp)
            ) {
                when (selectedTab) {
                    // TAB 0: MUNDOS
                    0 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // "Crear Nuevo" button
                            MinecraftButton(
                                text = "+ Crear Nuevo Mundo",
                                onClick = onCreateNewWorld,
                                modifier = Modifier.fillMaxWidth(),
                                height = 38.dp,
                                tag = "create_new_world_button"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (worlds.isEmpty()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("No tienes mundos guardados. ¡Crea uno nuevo!", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(worlds, key = { it.id }) { world ->
                                        val isSelected = selectedWorld?.id == world.id
                                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        val dateStr = dateFormat.format(Date(world.lastPlayed))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSelected) Color(0xFF333333) else Color(0xFF1E1E1E), RoundedCornerShape(2.dp))
                                                .border(1.5.dp, if (isSelected) Color(0xFFFFD54F) else Color(0xFF424242), RoundedCornerShape(2.dp))
                                                .clickable {
                                                    if (isSelected) onSelectWorld(world)
                                                    else selectedWorld = world
                                                }
                                                .padding(10.dp)
                                                .testTag("world_row_${world.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(world.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                        if (world.dimension == "voidlands") {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("🌌 THE VOIDLANDS", color = Color(0xFFFF55FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                        }
                                                    }
                                                    Text("${world.gameMode} • $dateStr • Semilla: ${world.seed}", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (world.bossDefeated) {
                                                        Text("👑", fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    IconButton(onClick = { onDeleteWorld(world.id) }, modifier = Modifier.size(32.dp)) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color(0xFFE57373), modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: REALMS & AMIGOS
                    1 -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Tus Reinos Dedicados (Realms) y Amigos en Línea",
                                color = Color(0xFFFFD54F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Realm Item 1
                            RealmCard(
                                title = "Mi Realm de Supervivencia",
                                description = "Mundo en la nube persistente 24/7 con amigos.",
                                online = "3/10 amigos jugando ahora",
                                onJoin = {
                                    val rWorld = WorldEntity(
                                        id = 888001L,
                                        name = "Mi Realm de Supervivencia",
                                        seed = 555444333L,
                                        gameMode = "SURVIVAL",
                                        dimension = "overworld"
                                    )
                                    onSelectWorld(rWorld)
                                }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Realm Item 2
                            RealmCard(
                                title = "The Voidlands Realm [Oficial]",
                                description = "Desafío comunitario contra el Void King.",
                                online = "7/10 amigos jugando ahora",
                                onJoin = {
                                    val rWorld = WorldEntity(
                                        id = 888002L,
                                        name = "The Voidlands Realm",
                                        seed = 999888777L,
                                        gameMode = "SURVIVAL",
                                        dimension = "voidlands",
                                        playerY = 35f
                                    )
                                    onSelectWorld(rWorld)
                                }
                            )
                        }
                    }

                    // TAB 2: SERVIDORES
                    2 -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(servers) { s ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E1E1E), RoundedCornerShape(2.dp))
                                        .border(1.5.dp, Color(0xFF424242), RoundedCornerShape(2.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Dns, contentDescription = null, tint = Color(0xFFB388FF), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(s.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Text(s.motd, color = Color(0xFFCE93D8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                            Text("${s.ip} • 👥 ${s.players} • 📶 ${s.ping}ms", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }

                                        MinecraftButton(
                                            text = "Entrar",
                                            onClick = {
                                                val sWorld = WorldEntity(
                                                    id = 777000L + s.name.hashCode().toLong().coerceAtLeast(0L),
                                                    name = s.name,
                                                    seed = s.name.hashCode().toLong(),
                                                    gameMode = if (s.id == "v4") "CREATIVE" else "SURVIVAL",
                                                    dimension = s.dimension,
                                                    playerY = if (s.dimension == "voidlands") 35f else 22f
                                                )
                                                onSelectWorld(sWorld)
                                            },
                                            height = 34.dp,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == 0) {
                    MinecraftButton(
                        text = "Jugar en el Mundo",
                        enabled = (selectedWorld != null || worlds.isNotEmpty()),
                        onClick = {
                            val toPlay = selectedWorld ?: worlds.firstOrNull()
                            if (toPlay != null) onSelectWorld(toPlay)
                        },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                MinecraftButton(
                    text = "Atrás",
                    onClick = onBack,
                    modifier = Modifier.weight(0.8f)
                )
            }
        }
    }
}

@Composable
fun PlayTabButton(
    title: String,
    badge: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(if (isSelected) Color(0xFF333333) else Color(0xFF222222), RoundedCornerShape(2.dp))
            .border(1.5.dp, if (isSelected) Color.White else Color(0xFF4A4A4A), RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFFAAAAAA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF555555), RoundedCornerShape(2.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RealmCard(
    title: String,
    description: String,
    online: String,
    onJoin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(2.dp))
            .border(1.5.dp, Color(0xFF424242), RoundedCornerShape(2.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(description, color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("🟢 $online", color = Color(0xFF76FF03), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            MinecraftButton(
                text = "Unirse",
                onClick = onJoin,
                height = 34.dp,
                fontSize = 11.sp
            )
        }
    }
}
