package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MinecraftButton
import com.example.ui.theme.MinecraftColors

@Composable
fun MainMenuScreen(
    onPlay: () -> Unit,
    onCreateWorld: () -> Unit,
    onServers: () -> Unit,
    onStore: () -> Unit,
    onSkins: () -> Unit,
    onTexturePacks: () -> Unit,
    onAchievements: () -> Unit,
    onSettings: () -> Unit,
    onVoidlandsQuickPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Oscillating animation for the yellow splash text
    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")
    val splashScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF140D22))
    ) {
        // Panoramic Background Artwork
        Image(
            painter = painterResource(id = R.drawable.voidlands_banner_1790804234911),
            contentDescription = "Fondo MineCraftWorld",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Darkened vignette for genuine Minecraft contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Main Landscape 16:9 Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP: Authentic Minecraft 3D Beveled Title & Splash Text ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main Logo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        // Shadow text
                        Text(
                            text = "MINECRAFTWORLD",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.offset(x = 3.dp, y = 3.dp)
                        )
                        // Stone-textured bevel front text
                        Text(
                            text = "MINECRAFTWORLD",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp,
                            color = Color(0xFFE0E0E0)
                        )
                    }

                    // Subtitle badge
                    Box(
                        modifier = Modifier
                            .offset(y = (-4).dp)
                            .background(Color(0xFF6200EA), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFE040FB), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "END UPDATE — THE VOIDLANDS (16:9)",
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Yellow Minecraft Splash Text (vibrating and rotated)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-30).dp, y = 16.dp)
                        .rotate(-18f)
                        .scale(splashScale)
                ) {
                    Text(
                        text = "¡1920 × 1080 Widescreen!",
                        color = Color(0xFF3F3F00),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.dp, 1.dp)
                    )
                    Text(
                        text = "¡1920 × 1080 Widescreen!",
                        color = MinecraftColors.TextYellowSplash,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // --- CENTER: Authentic Minecraft Buttons Stack (16:9 Landscape) ---
            Column(
                modifier = Modifier.width(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Jugar
                MinecraftButton(
                    text = "Jugar",
                    onClick = onPlay,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "menu_play_button"
                )

                // Crear Mundo
                MinecraftButton(
                    text = "Crear Mundo",
                    onClick = onCreateWorld,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "menu_create_world_button"
                )

                // Servidores & Tienda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftButton(
                        text = "Servidores",
                        onClick = onServers,
                        modifier = Modifier.weight(1f),
                        tag = "menu_servers_button"
                    )
                    MinecraftButton(
                        text = "Tienda",
                        onClick = onStore,
                        modifier = Modifier.weight(1f),
                        tag = "menu_store_button"
                    )
                }

                // Aspectos & Texturas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftButton(
                        text = "Vestidor / Skins",
                        onClick = onSkins,
                        modifier = Modifier.weight(1f),
                        tag = "menu_skins_button"
                    )
                    MinecraftButton(
                        text = "Packs Texturas",
                        onClick = onTexturePacks,
                        modifier = Modifier.weight(1f),
                        tag = "menu_texture_packs_button"
                    )
                }

                // Logros & Ajustes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftButton(
                        text = "🏆 Logros",
                        onClick = onAchievements,
                        modifier = Modifier.weight(1f),
                        tag = "menu_achievements_button"
                    )
                    MinecraftButton(
                        text = "Ajustes",
                        onClick = onSettings,
                        modifier = Modifier.weight(1f),
                        tag = "menu_settings_button"
                    )
                }

                // Quick Launch Voidlands
                MinecraftButton(
                    text = "🌌 Entrar a The Voidlands",
                    onClick = onVoidlandsQuickPlay,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "menu_voidlands_quick_button"
                )
            }

            // --- BOTTOM BAR: Version & Copyright ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MineCraftWorld v1.5 (1920x1080 16:9)",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Diseñado para Android y Pantallas Panorámicas",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
