package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DimText
import com.example.ui.theme.LightText

val AVATARS = listOf(
    "avatar_cyber_1",
    "avatar_cyber_2",
    "avatar_cyber_3",
    "avatar_cyber_4",
    "avatar_cyber_5",
    "avatar_cyber_6"
)

@Composable
fun LoginScreen(
    allUsers: List<UserEntity>,
    loginError: String?,
    onLogin: (String, String) -> Unit,
    onSelectUser: (UserEntity) -> Unit
) {
    var usernameText by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(AVATARS.first()) }

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
            // Hero Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_3d_game_banner_1786216130728),
                        contentDescription = "Game Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DarkBackground.copy(alpha = 0.85f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "COLOR RUSH 3D",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberCyan
                        )
                        Text(
                            text = "Infinite Levels • 3D Voxel Coloring",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Switcher if users exist
            if (allUsers.isNotEmpty()) {
                Text(
                    text = "SELECT PLAYER PROFILE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DimText,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allUsers) { user ->
                        Card(
                            modifier = Modifier
                                .testTag("user_profile_card_${user.id}")
                                .clickable { onSelectUser(user) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(CyberViolet),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.username,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightText
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = CyberGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Lvl ${user.maxLevelReached} • ${user.highScore} pts",
                                            fontSize = 11.sp,
                                            color = DimText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Create / Login Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "NEW PLAYER LOGIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberMagenta
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = usernameText,
                        onValueChange = { usernameText = it },
                        label = { Text("Player Name / Handle") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DimText,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = DimText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loginError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CHOOSE AVATAR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DimText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AVATARS.forEach { avatarKey ->
                            val isSelected = avatarKey == selectedAvatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyberMagenta else DarkBackground)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) CyberCyan else DimText,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedAvatar = avatarKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = if (isSelected) Color.White else DimText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onLogin(usernameText, selectedAvatar) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START 3D COLOR GAME",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onLogin("GuestPlayer_${(100..999).random()}", AVATARS.first()) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("guest_login_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "QUICK GUEST PLAY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberCyan
                        )
                    }
                }
            }
        }
    }
}
