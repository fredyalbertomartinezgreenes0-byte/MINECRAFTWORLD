package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ItemStack

object MinecraftColors {
    val GuiBackground = Color(0xFFC6C6C6)
    val GuiBorderDark = Color(0xFF373737)
    val GuiBorderLight = Color(0xFFFFFFFF)
    val GuiBorderShadow = Color(0xFF555555)

    val ButtonBase = Color(0xFF737373)
    val ButtonHover = Color(0xFF8B8B8B)
    val ButtonPressed = Color(0xFF555555)
    val ButtonBorderOuter = Color(0xFF000000)

    val SlotDarkInset = Color(0xFF373737)
    val SlotLightInset = Color(0xFFFFFFFF)
    val SlotBase = Color(0xFF8B8B8B)

    val ExpBarGreen = Color(0xFF76FF03)
    val ExpBarBackground = Color(0xFF1B5E20)
    val ExpLevelNumber = Color(0xFF76FF03)

    val TextDark = Color(0xFF3F3F3F)
    val TextYellowSplash = Color(0xFFFFFF55)
}

/**
 * Authentic Minecraft Stone-beveled Button
 */
@Composable
fun MinecraftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    fontSize: TextUnit = 14.sp,
    enabled: Boolean = true,
    tag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        !enabled -> Color(0xFF4A4A4A)
        isPressed -> MinecraftColors.ButtonPressed
        else -> MinecraftColors.ButtonBase
    }

    Box(
        modifier = modifier
            .height(height)
            .border(2.dp, MinecraftColors.ButtonBorderOuter, RoundedCornerShape(2.dp))
            .drawBehind {
                val strokeW = 3.dp.toPx()
                if (isPressed) {
                    // Dark top-left when pressed
                    drawRect(MinecraftColors.GuiBorderDark, Offset.Zero, Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderDark, Offset.Zero, Size(strokeW, size.height))
                    drawRect(MinecraftColors.GuiBorderLight, Offset(0f, size.height - strokeW), Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderLight, Offset(size.width - strokeW, 0f), Size(strokeW, size.height))
                } else {
                    // Classic 3D bevel: white top-left, dark bottom-right
                    drawRect(Color(0xFFB0B0B0), Offset.Zero, Size(size.width, strokeW))
                    drawRect(Color(0xFFB0B0B0), Offset.Zero, Size(strokeW, size.height))
                    drawRect(MinecraftColors.GuiBorderDark, Offset(0f, size.height - strokeW), Size(size.width, strokeW))
                    drawRect(MinecraftColors.GuiBorderDark, Offset(size.width - strokeW, 0f), Size(strokeW, size.height))
                }
            }
            .background(bgColor, RoundedCornerShape(2.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 12.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        // Text with Minecraft drop-shadow
        Box {
            Text(
                text = text,
                color = if (enabled) Color(0xFF3F3F3F) else Color(0xFF222222),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.offset(x = 1.5.dp, y = 1.5.dp)
            )
            Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFFAAAAAA),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Authentic Minecraft Inset Inventory Slot
 */
@Composable
fun MinecraftSlot(
    stack: ItemStack,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tag: String = ""
) {
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                val strokeW = 2.dp.toPx()
                // Inset bevel: Dark on top/left, White on bottom/right
                drawRect(MinecraftColors.SlotDarkInset, Offset.Zero, Size(this.size.width, strokeW))
                drawRect(MinecraftColors.SlotDarkInset, Offset.Zero, Size(strokeW, this.size.height))
                drawRect(MinecraftColors.SlotLightInset, Offset(0f, this.size.height - strokeW), Size(this.size.width, strokeW))
                drawRect(MinecraftColors.SlotLightInset, Offset(this.size.width - strokeW, 0f), Size(strokeW, this.size.height))
            }
            .background(MinecraftColors.SlotBase)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else Color(0xFF2E2E2E),
                shape = RoundedCornerShape(1.dp)
            )
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        if (!stack.isEmpty) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.65f).dp)
                    .background(Color(stack.def.iconColor), RoundedCornerShape(2.dp))
                    .border(1.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )

            if (stack.count > 1) {
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 2.dp, bottom = 1.dp)) {
                    Text(
                        text = "${stack.count}",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(1.dp, 1.dp)
                    )
                    Text(
                        text = "${stack.count}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
