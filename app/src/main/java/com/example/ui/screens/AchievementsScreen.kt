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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinecraftButton

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val rewardGems: Int = 50
)

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val achievements = listOf(
        AchievementItem("wood", "Tomando Madera", "Tala un árbol y recolecta madera de roble.", "🪵", true),
        AchievementItem("stone", "La Edad de Piedra", "Fabrica tu primer pico con adoquín.", "⛏️", true),
        AchievementItem("bench", "Hora de Construir", "Coloca y utiliza una mesa de trabajo.", "🛠️", true),
        AchievementItem("smelt", "¡Caliente, Caliente!", "Funde lingotes de hierro en el horno.", "🔥", true),
        AchievementItem("diamonds", "¡DIAMANTES!", "Descubre diamantes relucientes en las cavernas profundas.", "💎", false, 100),
        AchievementItem("void_portal", "El Gran Salto", "Construye y activa el Portal a The Voidlands.", "🌀", false, 200),
        AchievementItem("void_king", "La Caída del Rey", "Derrota al Void King en su Ciudadela Flotante.", "👑", false, 500),
        AchievementItem("architect", "Arquitecto del Cosmos", "Coloca más de 200 bloques en The Voidlands.", "🏛️", false, 150)
    )

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("achievements_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box {
                    Text(
                        text = "🏆 Logros de MineCraftWorld",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.5.dp, 1.5.dp)
                    )
                    Text(
                        text = "🏆 Logros de MineCraftWorld",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Central Box List
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
                    items(achievements) { ach ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (ach.isUnlocked) Color(0xFF263238) else Color(0xFF1A1A1A), RoundedCornerShape(2.dp))
                                .border(1.dp, if (ach.isUnlocked) Color(0xFF00E676) else Color(0xFF424242))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(ach.icon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = ach.title,
                                            color = if (ach.isUnlocked) Color(0xFF76FF03) else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = ach.description,
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+${ach.rewardGems} 💎",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (ach.isUnlocked) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Desbloqueado", tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = "Bloqueado", tint = Color.Gray, modifier = Modifier.size(18.dp))
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
                tag = "achievements_done_button"
            )
        }
    }
}
