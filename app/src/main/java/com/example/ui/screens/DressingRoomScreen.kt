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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.MinecraftButton
import com.example.ui.theme.MinecraftColors

data class CapeItem(val id: String, val name: String, val color: Long, val desc: String)
data class EmoteItem(val id: String, val name: String, val icon: String)

@Composable
fun DressingRoomScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Skins, 1: Capas, 2: Emotes

    val skins = listOf(
        CharacterSkin("steve_classic", "Steve", "Aspecto clásico original.", 0xFF1976D2, 0xFF455A64),
        CharacterSkin("alex_ranger", "Alex", "Aspecto clásico explorador.", 0xFF388E3C, 0xFF8D6E63),
        CharacterSkin("void_slayer", "Señor del Vacío", "Armadura espacial con corona cósmica.", 0xFF7C4DFF, 0xFF311B92, true),
        CharacterSkin("shadow_rogue", "Pícaro de la Sombra", "Capa negra con capucha de sigilo.", 0xFF212121, 0xFFD50000),
        CharacterSkin("cyber_miner", "Ciberminero", "Exotraje de alta tecnología para minas.", 0xFF00E5FF, 0xFF263238)
    )

    val capes = listOf(
        CapeItem("cape_void", "Capa de The Voidlands", 0xFF6200EA, "Tejida con partículas de estrellas y nebulosas."),
        CapeItem("cape_founder", "Capa del Fundador", 0xFFFFD700, "Insignia dorada de los primeros mineros."),
        CapeItem("cape_dragon", "Capa del Dragón Cósmico", 0xFF9C27B0, "Escamas violetas reflectantes."),
        CapeItem("cape_classic", "Capa Clásica Roja", 0xFFD32F2F, "Emblema tradicional de aventurero.")
    )

    val emotes = listOf(
        EmoteItem("wave", "Saludar", "👋"),
        EmoteItem("clap", "Aplaudir", "👏"),
        EmoteItem("victory", "Victoria del Vacío", "👑"),
        EmoteItem("dance", "Baile Voxel", "🕺")
    )

    var equippedSkin by remember { mutableStateOf(skins[2]) }
    var equippedCape by remember { mutableStateOf(capes[0]) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E140C)) // Dirt background
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("dressing_room_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box {
                    Text(
                        text = "Vestidor y Perfil",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.5.dp, 1.5.dp)
                    )
                    Text(
                        text = "Vestidor y Perfil",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 16:9 Landscape Layout: Left character pedestal, Right customization tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT: 3D Character Pedestal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .border(2.dp, Color(0xFF373737), RoundedCornerShape(2.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Character Avatar Representation
                        Box(
                            modifier = Modifier
                                .size(110.dp, 160.dp)
                                .background(Color(equippedSkin.primaryColor), RoundedCornerShape(4.dp))
                                .border(3.dp, Color(equippedCape.color), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👤", fontSize = 56.sp)
                                Text(
                                    text = equippedSkin.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Capa equipada: ${equippedCape.name}",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // RIGHT: Customization Tabs (Skins, Capas, Emotes)
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .border(2.dp, Color(0xFF373737), RoundedCornerShape(2.dp))
                        .padding(10.dp)
                ) {
                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MinecraftButton(
                            text = "Aspectos",
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f),
                            height = 36.dp,
                            fontSize = 11.sp
                        )
                        MinecraftButton(
                            text = "Capas",
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f),
                            height = 36.dp,
                            fontSize = 11.sp
                        )
                        MinecraftButton(
                            text = "Gestos",
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.weight(1f),
                            height = 36.dp,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Content
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (selectedTab) {
                            0 -> items(skins) { skin ->
                                val isSelected = skin.id == equippedSkin.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color(0xFF373737) else Color(0xFF222222))
                                        .border(1.dp, if (isSelected) Color(0xFFFFD54F) else Color(0xFF444444))
                                        .clickable { equippedSkin = skin }
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(skin.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(skin.description, color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        if (isSelected) {
                                            Text("Equipado", color = Color(0xFF76FF03), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                            1 -> items(capes) { cape ->
                                val isSelected = cape.id == equippedCape.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color(0xFF373737) else Color(0xFF222222))
                                        .border(1.dp, if (isSelected) Color(0xFFFFD54F) else Color(0xFF444444))
                                        .clickable { equippedCape = cape }
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(cape.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(cape.desc, color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        if (isSelected) {
                                            Text("Equipado", color = Color(0xFF76FF03), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                            2 -> items(emotes) { emote ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF222222))
                                        .border(1.dp, Color(0xFF444444))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(emote.icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(emote.name, color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Done Button
            MinecraftButton(
                text = "Hecho",
                onClick = onBack,
                modifier = Modifier.width(260.dp),
                tag = "dressing_room_done_button"
            )
        }
    }
}
