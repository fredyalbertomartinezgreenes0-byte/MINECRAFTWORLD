package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.AppDatabase
import com.example.data.WorldEntity
import com.example.data.WorldRepository
import com.example.gameplay.GameViewModel
import com.example.ui.game.GameScreen
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.CreateWorldDialog
import com.example.ui.screens.DressingRoomScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.PlayTabsScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.TexturePackScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class AppNavScreen {
    MAIN_MENU,
    PLAY_TABS,
    DRESSING_ROOM,
    TEXTURE_PACKS,
    ACHIEVEMENTS,
    STORE,
    GAME
}

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = WorldRepository(db.worldDao())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    // 1920 × 1080 (16:9) Aspect Ratio Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(16f / 9f)
                        ) {
                            MineCraftWorldApp(
                                viewModel = gameViewModel,
                                repository = repository
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MineCraftWorldApp(
    viewModel: GameViewModel,
    repository: WorldRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(AppNavScreen.MAIN_MENU) }
    var isCreateWorldDialogOpen by remember { mutableStateOf(false) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    val settings by viewModel.settings.collectAsState()

    when (currentScreen) {
        AppNavScreen.MAIN_MENU -> {
            MainMenuScreen(
                onPlay = { currentScreen = AppNavScreen.PLAY_TABS },
                onCreateWorld = { isCreateWorldDialogOpen = true },
                onServers = { currentScreen = AppNavScreen.PLAY_TABS },
                onStore = { currentScreen = AppNavScreen.STORE },
                onSkins = { currentScreen = AppNavScreen.DRESSING_ROOM },
                onTexturePacks = { currentScreen = AppNavScreen.TEXTURE_PACKS },
                onAchievements = { currentScreen = AppNavScreen.ACHIEVEMENTS },
                onSettings = { isSettingsDialogOpen = true },
                onVoidlandsQuickPlay = {
                    // Quick launch dedicated Voidlands dimension
                    coroutineScope.launch {
                        val voidlandsWorld = WorldEntity(
                            name = "🌌 The Voidlands - End Update",
                            seed = 987654321L,
                            gameMode = "SURVIVAL",
                            dimension = "voidlands",
                            playerX = 0f,
                            playerY = 36f,
                            playerZ = 8f,
                            lastPlayed = System.currentTimeMillis()
                        )
                        val id = repository.createWorld(voidlandsWorld)
                        val saved = repository.getWorld(id) ?: voidlandsWorld.copy(id = id)
                        viewModel.loadOrInitWorld(saved)
                        currentScreen = AppNavScreen.GAME
                    }
                }
            )
        }

        AppNavScreen.PLAY_TABS -> {
            PlayTabsScreen(
                worldsFlow = repository.allWorlds,
                onSelectWorld = { world ->
                    viewModel.loadOrInitWorld(world)
                    currentScreen = AppNavScreen.GAME
                },
                onDeleteWorld = { worldId ->
                    coroutineScope.launch {
                        repository.deleteWorld(worldId)
                    }
                },
                onCreateNewWorld = { isCreateWorldDialogOpen = true },
                onBack = { currentScreen = AppNavScreen.MAIN_MENU }
            )
        }

        AppNavScreen.DRESSING_ROOM -> {
            DressingRoomScreen(
                onBack = { currentScreen = AppNavScreen.MAIN_MENU }
            )
        }

        AppNavScreen.TEXTURE_PACKS -> {
            TexturePackScreen(
                viewModel = viewModel,
                onBack = { currentScreen = AppNavScreen.MAIN_MENU }
            )
        }

        AppNavScreen.ACHIEVEMENTS -> {
            AchievementsScreen(
                onBack = { currentScreen = AppNavScreen.MAIN_MENU }
            )
        }

        AppNavScreen.STORE -> {
            StoreScreen(
                onBack = { currentScreen = AppNavScreen.MAIN_MENU }
            )
        }

        AppNavScreen.GAME -> {
            GameScreen(
                viewModel = viewModel,
                onExitToMenu = {
                    viewModel.saveGame()
                    currentScreen = AppNavScreen.MAIN_MENU
                },
                onOpenSettings = { isSettingsDialogOpen = true }
            )
        }
    }

    // Create World Dialog
    if (isCreateWorldDialogOpen) {
        CreateWorldDialog(
            onDismiss = { isCreateWorldDialogOpen = false },
            onCreateWorld = { world ->
                isCreateWorldDialogOpen = false
                coroutineScope.launch {
                    val id = repository.createWorld(world)
                    val saved = repository.getWorld(id) ?: world.copy(id = id)
                    viewModel.loadOrInitWorld(saved)
                    currentScreen = AppNavScreen.GAME
                }
            }
        )
    }

    // Settings Dialog
    if (isSettingsDialogOpen) {
        SettingsDialog(
            initialSettings = settings,
            onSaveSettings = { newSettings ->
                viewModel.settings.value = newSettings
            },
            onDismiss = { isSettingsDialogOpen = false }
        )
    }
}
