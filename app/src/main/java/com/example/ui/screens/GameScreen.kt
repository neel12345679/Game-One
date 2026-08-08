package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.game.GameUiState
import com.example.game.engine.Game3DRenderer
import com.example.game.engine.PowerupType
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DimText
import com.example.ui.theme.LightText

@Composable
fun GameScreen(
    state: GameUiState,
    renderer: Game3DRenderer,
    onMoveLaneDelta: (Int) -> Unit,
    onMoveLaneDirect: (Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onRevive: () -> Unit,
    onExitToMenu: () -> Unit,
    onNextLevel: () -> Unit
) {
    val levelData = state.levelData ?: return
    val currentUser = state.currentUser
    val progress = (state.playerPos.z / levelData.totalLength).coerceIn(0f, 1f)

    var accumulatedDragX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { accumulatedDragX = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDragX += dragAmount.x
                        if (accumulatedDragX > 45f) {
                            onMoveLaneDelta(1)
                            accumulatedDragX = 0f
                        } else if (accumulatedDragX < -45f) {
                            onMoveLaneDelta(-1)
                            accumulatedDragX = 0f
                        }
                    }
                )
            }
            .testTag("game_3d_canvas_container")
    ) {
        // 1. Live 3D Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val screenHeight = size.height

            renderer.renderScene(
                drawScope = this,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                playerPos = state.playerPos,
                playerSkin = currentUser?.activeSkin ?: "skin_cyber_ball",
                activePowerups = state.activePowerups,
                levelData = levelData,
                gameTimeSeconds = state.gameTimeSeconds
            )
        }

        // 2. HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LEVEL ${levelData.levelNumber}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberCyan
                    )
                    Text(
                        text = "SCORE: ${state.score}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGold
                    )
                }

                if (state.combo > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberMagenta)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${state.combo}x COMBO!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .testTag("pause_button")
                ) {
                    Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = LightText)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = levelData.palette.primary,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            // On-screen Touch Lane Controls for easy play
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onMoveLaneDelta(-1) },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.8f))
                        .testTag("move_left_button")
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Left", tint = CyberCyan, modifier = Modifier.size(36.dp))
                }

                IconButton(
                    onClick = { onMoveLaneDelta(1) },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.8f))
                        .testTag("move_right_button")
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Right", tint = CyberCyan, modifier = Modifier.size(36.dp))
                }
            }
        }

        // 3. Pause Modal
        if (state.isPaused) {
            GameDialogContainer {
                Text(text = "PAUSED", fontSize = 24.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                    modifier = Modifier.fillMaxWidth().testTag("resume_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESUME GAME", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onExitToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth().testTag("exit_menu_button")
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = DimText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXIT TO MENU", color = DimText)
                }
            }
        }

        // 4. Game Over Modal
        if (state.isGameOver) {
            GameDialogContainer {
                Text(text = "CRASHED!", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF1744))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Score: ${state.score} pts", fontSize = 16.sp, color = LightText)
                Spacer(modifier = Modifier.height(20.dp))

                if ((currentUser?.totalCoins ?: 0) >= 50) {
                    Button(
                        onClick = onRevive,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                        modifier = Modifier.fillMaxWidth().testTag("revive_button")
                    ) {
                        Text("REVIVE (🪙 50 Coins)", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                    modifier = Modifier.fillMaxWidth().testTag("restart_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TRY AGAIN", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onExitToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth().testTag("exit_menu_gameover_button")
                ) {
                    Text("MAIN MENU", color = DimText)
                }
            }
        }

        // 5. Level Complete Victory Modal
        if (state.isLevelComplete) {
            GameDialogContainer {
                Text(text = "LEVEL COMPLETE!", fontSize = 26.sp, fontWeight = FontWeight.Black, color = CyberGold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "3D Voxel Sculpture Auto-Colored & Unlocked!",
                    fontSize = 12.sp,
                    color = CyberCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CyberGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Score: ${state.score}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightText)
                    Text(text = "Tiles Painted: ${state.tilesPainted}", fontSize = 13.sp, color = DimText)
                    Text(text = "Reward: 🪙 +${state.coinsEarnedInSession} Coins", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                    modifier = Modifier.fillMaxWidth().testTag("next_level_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NEXT LEVEL (${levelData.levelNumber + 1})", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onExitToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                    modifier = Modifier.fillMaxWidth().testTag("victory_menu_button")
                ) {
                    Text("COLLECTION MENU", color = DimText)
                }
            }
        }
    }
}

@Composable
fun GameDialogContainer(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}
