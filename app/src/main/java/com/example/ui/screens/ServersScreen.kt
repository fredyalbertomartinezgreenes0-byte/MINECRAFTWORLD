package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorldEntity

data class ServerEntry(
    val id: String,
    val name: String,
    val ip: String,
    val players: String,
    val ping: Int,
    val motd: String,
    val dimension: String = "overworld"
)

@Composable
fun ServersScreen(
    onJoinServer: (WorldEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var servers by remember {
        mutableStateOf(
            listOf(
                ServerEntry(
                    id = "voidcraft",
                    name = "VoidCraft Oficial [EXPANSIÓN 1.5]",
                    ip = "play.voidcraft.network:25565",
                    players = "84/100",
                    ping = 22,
                    motd = "¡Evento Void King activo! Servidor dedicado de supervivencia y The Voidlands.",
                    dimension = "voidlands"
                ),
                ServerEntry(
                    id = "survival_legends",
                    name = "Survival Legends SMP",
                    ip = "smp.minecraftworld.lat:19132",
                    players = "42/60",
                    ping = 35,
                    motd = "Economía justa, clanes, parcelas seguras y exploración sin límites.",
                    dimension = "overworld"
                ),
                ServerEntry(
                    id = "skyblock",
                    name = "SkyBlock & Floating Islands",
                    ip = "sky.blockgames.gg",
                    players = "128/150",
                    ping = 18,
                    motd = "Construye tu imperio en islas flotantes en el vacío.",
                    dimension = "voidlands"
                ),
                ServerEntry(
                    id = "creative_builders",
                    name = "Constructores Creativos Pro",
                    ip = "build.mineart.io",
                    players = "25/50",
                    ping = 45,
                    motd = "Vuelo libre, parcelas infinitas y concurso de construcción semanal.",
                    dimension = "overworld"
                )
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF130E20))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("servers_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Text(
                    text = "🌐 SERVIDORES MULTIJUGADOR",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        val newServer = ServerEntry(
                            id = "custom_${System.currentTimeMillis()}",
                            name = "Servidor Privado Amigos",
                            ip = "lan.local:25565",
                            players = "1/10",
                            ping = 12,
                            motd = "Partida cooperativa local"
                        )
                        servers = servers + newServer
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_server_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(servers, key = { it.id }) { server ->
                    ServerCard(
                        server = server,
                        onJoin = {
                            val world = WorldEntity(
                                id = 999000L + server.name.hashCode().toLong().coerceAtLeast(0L),
                                name = server.name,
                                seed = server.name.hashCode().toLong(),
                                gameMode = if (server.id == "creative_builders") "CREATIVE" else "SURVIVAL",
                                dimension = server.dimension,
                                playerX = 0f,
                                playerY = if (server.dimension == "voidlands") 35f else 22f,
                                playerZ = 0f,
                                lastPlayed = System.currentTimeMillis()
                            )
                            onJoinServer(world)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ServerCard(
    server: ServerEntry,
    onJoin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF201733))
            .border(1.5.dp, Color(0xFF4A3470), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = Color(0xFFB388FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = server.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Ping",
                        tint = if (server.ping < 30) Color(0xFF00E676) else Color(0xFFFFB74D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${server.ping}ms", color = Color.LightGray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = server.motd,
                color = Color(0xFFCE93D8),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${server.ip} • 👥 ${server.players}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("join_server_${server.id}")
                ) {
                    Text("Entrar al Servidor", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
