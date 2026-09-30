package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gameplay.GameViewModel
import com.example.model.Dimension
import com.example.ui.theme.MinecraftButton

@Composable
fun PauseMenuModal(
    viewModel: GameViewModel,
    onResume: () -> Unit,
    onExitToMainMenu: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val currentDim by viewModel.dimension.collectAsState()

    Dialog(onDismissRequest = onResume) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                .border(2.dp, Color(0xFF555555), RoundedCornerShape(4.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                Box {
                    Text(
                        text = "Menú del Juego",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.5.dp, 1.5.dp)
                    )
                    Text(
                        text = "Menú del Juego",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Dimensión: ${if (currentDim == Dimension.VOIDLANDS) "The Voidlands" else "Mundo Principal"}",
                    color = if (currentDim == Dimension.VOIDLANDS) Color(0xFFFF55FF) else Color(0xFF55FF55),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Volver al juego
                MinecraftButton(
                    text = "Volver al Juego",
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "pause_resume_button"
                )

                // Warp to Voidlands / Overworld
                MinecraftButton(
                    text = if (currentDim == Dimension.VOIDLANDS) "🌍 Regresar al Overworld" else "🌌 Viajar a The Voidlands",
                    onClick = {
                        viewModel.warpDimension()
                        onResume()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    tag = "pause_warp_button"
                )

                // Opciones
                MinecraftButton(
                    text = "Opciones...",
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "pause_settings_button"
                )

                // Guardar y Salir al Título
                MinecraftButton(
                    text = "Guardar y Salir al Menú",
                    onClick = {
                        viewModel.saveGame()
                        onExitToMainMenu()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    tag = "pause_exit_button"
                )
            }
        }
    }
}
