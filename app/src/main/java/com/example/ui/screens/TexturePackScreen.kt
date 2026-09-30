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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TextureAtlasGenerator
import com.example.engine.TexturePack
import com.example.gameplay.GameViewModel
import com.example.ui.theme.MinecraftButton
import com.example.ui.theme.MinecraftColors

@Composable
fun TexturePackScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var selectedPack by remember { mutableStateOf(TextureAtlasGenerator.currentPack) }

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("texture_pack_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box {
                    Text(
                        text = "Paquetes de Texturas del Mundo",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.5.dp, 1.5.dp)
                    )
                    Text(
                        text = "Paquetes de Texturas del Mundo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Central Texture Pack List Box
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    .border(2.dp, Color(0xFF373737), RoundedCornerShape(2.dp))
                    .padding(10.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TexturePack.entries) { pack ->
                        val isEquipped = (pack == selectedPack)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isEquipped) Color(0xFF333333) else Color(0xFF1A1A1A),
                                    RoundedCornerShape(2.dp)
                                )
                                .border(
                                    1.5.dp,
                                    if (isEquipped) Color(0xFFFFD54F) else Color(0xFF4A4A4A),
                                    RoundedCornerShape(2.dp)
                                )
                                .clickable {
                                    selectedPack = pack
                                    TextureAtlasGenerator.currentPack = pack
                                    viewModel.renderer?.setTexturePack(pack)
                                }
                                .padding(12.dp)
                                .testTag("pack_item_${pack.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Icon preview
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                when (pack) {
                                                    TexturePack.CLASSIC -> Color(0xFF4CAF50)
                                                    TexturePack.VOIDLANDS_COSMIC -> Color(0xFF7C4DFF)
                                                    TexturePack.BARE_BONES -> Color(0xFFFF9800)
                                                    TexturePack.MEDIEVAL_RPG -> Color(0xFF795548)
                                                },
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(1.dp, Color.White, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (pack) {
                                                TexturePack.CLASSIC -> "🌿"
                                                TexturePack.VOIDLANDS_COSMIC -> "🌌"
                                                TexturePack.BARE_BONES -> "🎨"
                                                TexturePack.MEDIEVAL_RPG -> "🏰"
                                            },
                                            fontSize = 20.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = pack.displayName,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = pack.description,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                if (isEquipped) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF76FF03), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Activo", color = Color(0xFF76FF03), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Done Button
            MinecraftButton(
                text = "Hecho / Guardar Pack",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(0.6f),
                tag = "confirm_texture_pack_button"
            )
        }
    }
}
