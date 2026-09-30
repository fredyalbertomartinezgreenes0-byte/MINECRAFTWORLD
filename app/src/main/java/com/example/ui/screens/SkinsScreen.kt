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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

data class CharacterSkin(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val isVoidlandsSpecial: Boolean = false
)

@Composable
fun SkinsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val skins = listOf(
        CharacterSkin("alex_ranger", "Alex Explorador", "Equipamiento ligero de cuero y tela verde para selvas y bosques.", 0xFF388E3C, 0xFF8D6E63),
        CharacterSkin("steve_classic", "Steve Minero", "El legendario aventurero con camisa azul y pantalones vaqueros.", 0xFF1976D2, 0xFF455A64),
        CharacterSkin("void_slayer", "Caballero del Vacío", "Armadura forjada con obsidiana cósmica y cristales púrpuras.", 0xFF7C4DFF, 0xFF311B92, true),
        CharacterSkin("shadow_rogue", "Pícaro de la Sombra", "Capa oscura y ojos rojos que acechan en las cavernas subterráneas.", 0xFF212121, 0xFFD50000),
        CharacterSkin("cyber_miner", "Minero Cibernético", "Traje futurista con visor láser y exoesqueleto de titanio.", 0xFF00E5FF, 0xFF263238)
    )

    var selectedSkinId by remember { mutableStateOf("void_slayer") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF140F22))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("skins_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "👕 VESTIDOR DE ASPECTOS (SKINS)",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(skins) { skin ->
                    val isEquipped = (skin.id == selectedSkinId)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isEquipped) Color(0xFF2E1C4E) else Color(0xFF201633))
                            .border(
                                2.dp,
                                if (isEquipped) Color(0xFFFFD54F) else Color(0xFF453066),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedSkinId = skin.id }
                            .padding(14.dp)
                            .testTag("skin_item_${skin.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Avatar Icon graphic
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(skin.primaryColor))
                                        .border(2.dp, Color(skin.secondaryColor), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👤", fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(skin.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        if (skin.isVoidlandsSpecial) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("🌌 THE VOIDLANDS", color = Color(0xFFE040FB), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(skin.description, color = Color.LightGray, fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = { selectedSkinId = skin.id },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) Color(0xFF4CAF50) else Color(0xFF5E35B1)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("equip_skin_${skin.id}")
                            ) {
                                if (isEquipped) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Equipado", fontSize = 12.sp)
                                } else {
                                    Text("Equipar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
