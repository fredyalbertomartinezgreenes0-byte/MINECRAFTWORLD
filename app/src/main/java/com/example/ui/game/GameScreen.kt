package com.example.ui.game

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.VoxelRenderer
import com.example.gameplay.GameViewModel
import com.example.ui.components.GameHUD
import com.example.ui.dialogs.ChatCommandModal
import com.example.ui.dialogs.ChestModal
import com.example.ui.dialogs.CraftingModal
import com.example.ui.dialogs.DeathModal
import com.example.ui.dialogs.FurnaceModal
import com.example.ui.dialogs.InventoryModal
import com.example.ui.dialogs.PauseMenuModal
import com.example.ui.dialogs.VictoryModal

@SuppressLint("ClickableViewAccessibility")
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onExitToMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stats by viewModel.playerStats.collectAsState()
    val isInventoryOpen by viewModel.isInventoryOpen.collectAsState()
    val isCraftingTableOpen by viewModel.isCraftingTableOpen.collectAsState()
    val isFurnaceOpen by viewModel.isFurnaceOpen.collectAsState()
    val isChestOpen by viewModel.isChestOpen.collectAsState()
    val isPauseMenuOpen by viewModel.isPauseMenuOpen.collectAsState()
    val isVictoryOpen by viewModel.isVictoryModalOpen.collectAsState()
    val isWarping by viewModel.isDimensionWarping.collectAsState()
    var isChatOpen by remember { mutableStateOf(false) }

    // Setup GLSurfaceView and Renderer
    val renderer = remember {
        VoxelRenderer(context, viewModel.voxelWorld).also {
            viewModel.renderer = it
        }
    }

    val glSurfaceView = remember {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(2)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveGame()
        }
    }

    BackHandler {
        if (isInventoryOpen) viewModel.isInventoryOpen.value = false
        else if (isCraftingTableOpen) viewModel.isCraftingTableOpen.value = false
        else if (isFurnaceOpen) viewModel.isFurnaceOpen.value = false
        else if (isChestOpen) viewModel.isChestOpen.value = false
        else if (isPauseMenuOpen) viewModel.isPauseMenuOpen.value = false
        else viewModel.isPauseMenuOpen.value = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 3D OpenGL SurfaceView
        AndroidView(
            factory = { glSurfaceView },
            modifier = Modifier.fillMaxSize()
        )

        // Camera Touch Drag Overlay (Right half of the screen handles look direction)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        // Only process camera drag if touch is on right 60% of the screen
                        if (change.position.x > size.width * 0.35f) {
                            change.consume()
                            viewModel.onLookInput(
                                deltaPitch = -dragAmount.y * 0.22f,
                                deltaYaw = -dragAmount.x * 0.22f
                            )
                        }
                    }
                }
        )

        // Warp Overlay effect
        if (isWarping) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC6200EA))
            )
        }

        // Heads Up Display (HUD)
        GameHUD(
            viewModel = viewModel,
            onOpenChat = { isChatOpen = true }
        )

        // In-Game Chat & Commands Modal
        if (isChatOpen) {
            ChatCommandModal(
                viewModel = viewModel,
                onDismiss = { isChatOpen = false }
            )
        }

        // Modals & Popups
        if (isInventoryOpen) {
            InventoryModal(
                viewModel = viewModel,
                onDismiss = { viewModel.isInventoryOpen.value = false }
            )
        }

        if (isCraftingTableOpen) {
            CraftingModal(
                viewModel = viewModel,
                onDismiss = { viewModel.isCraftingTableOpen.value = false }
            )
        }

        if (isFurnaceOpen) {
            FurnaceModal(
                viewModel = viewModel,
                onDismiss = { viewModel.isFurnaceOpen.value = false }
            )
        }

        if (isChestOpen) {
            ChestModal(
                viewModel = viewModel,
                onDismiss = { viewModel.isChestOpen.value = false }
            )
        }

        if (isPauseMenuOpen) {
            PauseMenuModal(
                viewModel = viewModel,
                onResume = { viewModel.isPauseMenuOpen.value = false },
                onExitToMainMenu = onExitToMenu,
                onOpenSettings = onOpenSettings
            )
        }

        if (stats.isDead) {
            DeathModal(
                viewModel = viewModel,
                onExitToMenu = onExitToMenu
            )
        }

        if (isVictoryOpen) {
            VictoryModal(
                onDismiss = { viewModel.isVictoryModalOpen.value = false }
            )
        }
    }
}
