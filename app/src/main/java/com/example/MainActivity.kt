package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameViewModel
import com.example.game.ScreenType
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.VoxelGalleryScreen
import com.example.ui.theme.ColorRush3DTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColorRush3DTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ColorRush3DApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ColorRush3DApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.currentScreen) {
        ScreenType.LOGIN -> {
            LoginScreen(
                allUsers = uiState.allUsers,
                loginError = uiState.loginError,
                onLogin = { username, avatar -> viewModel.loginOrRegister(username, avatar) },
                onSelectUser = { user -> viewModel.switchUser(user) }
            )
        }

        ScreenType.MAIN_MENU -> {
            val user = uiState.currentUser
            if (user != null) {
                MainMenuScreen(
                    currentUser = user,
                    selectedLevelNumber = uiState.selectedLevelNumber,
                    isMuted = uiState.isMuted,
                    onSelectLevel = { lvl -> viewModel.selectLevelToPlay(lvl) },
                    onStartLevel = { lvl -> viewModel.startLevel(lvl) },
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onToggleMute = { viewModel.toggleMute() },
                    onLogout = { viewModel.navigateTo(ScreenType.LOGIN) }
                )
            } else {
                viewModel.navigateTo(ScreenType.LOGIN)
            }
        }

        ScreenType.IN_GAME -> {
            GameScreen(
                state = uiState,
                renderer = viewModel.renderer,
                onMoveLaneDelta = { delta -> viewModel.setTargetLaneDelta(delta) },
                onMoveLaneDirect = { lane -> viewModel.setTargetLaneDirect(lane) },
                onPause = { viewModel.pauseGame() },
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.startLevel(uiState.selectedLevelNumber) },
                onRevive = { viewModel.reviveWithCoins() },
                onExitToMenu = { viewModel.navigateTo(ScreenType.MAIN_MENU) },
                onNextLevel = { viewModel.startLevel(uiState.selectedLevelNumber + 1) }
            )
        }

        ScreenType.VOXEL_GALLERY -> {
            val user = uiState.currentUser
            if (user != null) {
                VoxelGalleryScreen(
                    currentUser = user,
                    onBack = { viewModel.navigateTo(ScreenType.MAIN_MENU) }
                )
            }
        }

        ScreenType.SHOP -> {
            val user = uiState.currentUser
            if (user != null) {
                ShopScreen(
                    currentUser = user,
                    onUnlockSkin = { skinId, cost -> viewModel.unlockSkin(skinId, cost) },
                    onSelectSkin = { skinId -> viewModel.selectSkin(skinId) },
                    onBack = { viewModel.navigateTo(ScreenType.MAIN_MENU) }
                )
            }
        }

        ScreenType.LEADERBOARD -> {
            val user = uiState.currentUser
            if (user != null) {
                LeaderboardScreen(
                    allUsers = uiState.allUsers,
                    currentUser = user,
                    onBack = { viewModel.navigateTo(ScreenType.MAIN_MENU) }
                )
            }
        }
    }
}

