package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.GameSoundSynthesizer
import com.example.model.WorldSettings
import com.example.ui.theme.MinecraftButton
import com.example.ui.theme.MinecraftColors

@Composable
fun SettingsDialog(
    initialSettings: WorldSettings,
    onSaveSettings: (WorldSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var sensitivity by remember { mutableFloatStateOf(initialSettings.sensitivity) }
    var soundVolume by remember { mutableFloatStateOf(initialSettings.soundVolume) }
    var musicVolume by remember { mutableFloatStateOf(initialSettings.musicVolume) }
    var invertY by remember { mutableStateOf(initialSettings.invertY) }
    var fogEnabled by remember { mutableStateOf(initialSettings.isFogEnabled) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(460.dp)
                .drawBehind {
                    val strokeW = 3.dp.toPx()
                    drawRect(MinecraftColors.GuiBorderLight, Offset.Zero, Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderLight, Offset.Zero, Size(strokeW, size.height))
                    drawRect(MinecraftColors.GuiBorderShadow, Offset(0f, size.height - strokeW), Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderShadow, Offset(size.width - strokeW, 0f), Size(strokeW, size.height))
                }
                .background(MinecraftColors.GuiBackground, RoundedCornerShape(2.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        text = "Opciones",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.dp, 1.dp)
                    )
                    Text(
                        text = "Opciones",
                        color = MinecraftColors.TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Volume row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sonido: ${(soundVolume * 100).toInt()}%",
                            color = MinecraftColors.TextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Slider(
                            value = soundVolume,
                            onValueChange = {
                                soundVolume = it
                                GameSoundSynthesizer.volume = it
                                GameSoundSynthesizer.soundEnabled = (it > 0.05f)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF373737),
                                activeTrackColor = Color(0xFF373737)
                            ),
                            modifier = Modifier.testTag("sound_slider")
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Música: ${(musicVolume * 100).toInt()}%",
                            color = MinecraftColors.TextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Slider(
                            value = musicVolume,
                            onValueChange = { musicVolume = it },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF373737),
                                activeTrackColor = Color(0xFF373737)
                            ),
                            modifier = Modifier.testTag("music_slider")
                        )
                    }
                }

                // Sensitivity slider
                Column {
                    Text(
                        text = "Sensibilidad: ${"%.1f".format(sensitivity)}x",
                        color = MinecraftColors.TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = sensitivity,
                        onValueChange = { sensitivity = it },
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF373737),
                            activeTrackColor = Color(0xFF373737)
                        ),
                        modifier = Modifier.testTag("sensitivity_slider")
                    )
                }

                // Toggle Buttons (Invert Y and Fog)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MinecraftButton(
                        text = if (invertY) "Invertir Ratón: SÍ" else "Invertir Ratón: NO",
                        onClick = { invertY = !invertY },
                        modifier = Modifier.weight(1f)
                    )

                    MinecraftButton(
                        text = if (fogEnabled) "Niebla: SÍ" else "Niebla: NO",
                        onClick = { fogEnabled = !fogEnabled },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Done Button
                MinecraftButton(
                    text = "Hecho",
                    onClick = {
                        val newSettings = initialSettings.copy(
                            soundVolume = soundVolume,
                            musicVolume = musicVolume,
                            sensitivity = sensitivity,
                            invertY = invertY,
                            isFogEnabled = fogEnabled
                        )
                        onSaveSettings(newSettings)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    tag = "save_settings_button"
                )
            }
        }
    }
}
