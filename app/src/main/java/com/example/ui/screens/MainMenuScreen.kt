package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.game.ScreenType
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DimText
import com.example.ui.theme.LightText

@Composable
fun MainMenuScreen(
    currentUser: UserEntity,
    selectedLevelNumber: Int,
    isMuted: Boolean,
    onSelectLevel: (Int) -> Unit,
    onStartLevel: (Int) -> Unit,
    onNavigate: (ScreenType) -> Unit,
    onToggleMute: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLogout() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberViolet),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currentUser.username,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                        Text(
                            text = "Tap to switch profile",
                            fontSize = 11.sp,
                            color = DimText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleMute, modifier = Modifier.testTag("mute_toggle_button")) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute Toggle",
                            tint = CyberCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Player Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "HIGH SCORE", fontSize = 11.sp, color = DimText, fontWeight = FontWeight.Bold)
                        Text(text = "${currentUser.highScore} PTS", fontSize = 22.sp, fontWeight = FontWeight.Black, color = CyberGold)
                    }

                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(color = DimText.copy(alpha = 0.3f)))

                    Column {
                        Text(text = "COINS", fontSize = 11.sp, color = DimText, fontWeight = FontWeight.Bold)
                        Text(text = "🪙 ${currentUser.totalCoins}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = CyberGreen)
                    }

                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(color = DimText.copy(alpha = 0.3f)))

                    Column {
                        Text(text = "MAX LEVEL", fontSize = 11.sp, color = DimText, fontWeight = FontWeight.Bold)
                        Text(text = "Lvl ${currentUser.maxLevelReached}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Giant PLAY BUTTON
            Button(
                onClick = { onStartLevel(selectedLevelNumber) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .testTag("play_game_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "START LEVEL $selectedLevelNumber",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Tap to roll into 3D world!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Infinite Level Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INFINITE LEVELS (1 to ∞)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGold
                )
                Text(
                    text = "Max: Level ${currentUser.maxLevelReached}",
                    fontSize = 12.sp,
                    color = DimText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Infinite Level Carousel
            val displayLevels = (1..(currentUser.maxLevelReached + 5)).toList()
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(displayLevels) { lvl ->
                    val isUnlocked = lvl <= currentUser.maxLevelReached
                    val isSelected = lvl == selectedLevelNumber

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) Brush.linearGradient(listOf(CyberMagenta, CyberViolet))
                                else if (isUnlocked) SolidColor(DarkSurface)
                                else SolidColor(DarkBackground)
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) CyberCyan else if (isUnlocked) CyberMagenta else DimText,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable(enabled = isUnlocked) { onSelectLevel(lvl) }
                            .testTag("level_item_$lvl"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isUnlocked) {
                                Text(
                                    text = "LVL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else DimText
                                )
                                Text(
                                    text = "$lvl",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) CyberCyan else LightText
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = DimText,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Lvl $lvl",
                                    fontSize = 11.sp,
                                    color = DimText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Hub Navigation Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HubCard(
                    title = "3D GALLERY",
                    subtitle = "Voxel Trophy Room",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = CyberGold,
                    modifier = Modifier.weight(1f).testTag("gallery_nav_card"),
                    onClick = { onNavigate(ScreenType.VOXEL_GALLERY) }
                )

                HubCard(
                    title = "SKIN SHOP",
                    subtitle = "Ball & Trail Skins",
                    icon = Icons.Default.ShoppingBag,
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f).testTag("shop_nav_card"),
                    onClick = { onNavigate(ScreenType.SHOP) }
                )

                HubCard(
                    title = "LEADERBOARD",
                    subtitle = "Global Ranks",
                    icon = Icons.Default.Leaderboard,
                    accentColor = CyberMagenta,
                    modifier = Modifier.weight(1f).testTag("leaderboard_nav_card"),
                    onClick = { onNavigate(ScreenType.LEADERBOARD) }
                )
            }
        }
    }
}

@Composable
fun HubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LightText
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = DimText
            )
        }
    }
}
