package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DimText
import com.example.ui.theme.LightText

@Composable
fun LeaderboardScreen(
    allUsers: List<UserEntity>,
    currentUser: UserEntity,
    onBack: () -> Unit
) {
    val sortedUsers = allUsers.sortedByDescending { it.highScore }

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
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("leaderboard_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = LightText)
                }
                Column {
                    Text(text = "GLOBAL LEADERBOARD", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberMagenta)
                    Text(text = "Top High Scores Across Profiles", fontSize = 12.sp, color = DimText)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(sortedUsers) { index, user ->
                    val rank = index + 1
                    val isCurrent = user.id == currentUser.id

                    val medalColor = when (rank) {
                        1 -> CyberGold
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> CyberViolet
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leaderboard_rank_$rank"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) CyberViolet.copy(alpha = 0.4f) else DarkSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(medalColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rank <= 3) {
                                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(text = "$rank", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DimText)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = user.username, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LightText)
                                Text(text = "Max Level ${user.maxLevelReached} • ${user.totalTilesColored} tiles colored", fontSize = 11.sp, color = DimText)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${user.highScore}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                                Text(text = "PTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DimText)
                            }
                        }
                    }
                }
            }
        }
    }
}
