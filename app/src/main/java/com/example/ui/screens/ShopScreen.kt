package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DimText
import com.example.ui.theme.LightText

data class SkinShopItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val previewColor: Color
)

val SHOP_ITEMS = listOf(
    SkinShopItem("skin_cyber_ball", "Neon Cyber Sphere", "Standard 3D glowing magenta sphere", 0, CyberMagenta),
    SkinShopItem("skin_flame_orb", "Solar Flame Orb", "Vibrant solar flare orange sphere", 150, Color(0xFFFF5722)),
    SkinShopItem("skin_plasma_cube", "Plasma Voxel Cube", "Rotating violet plasma cube skin", 300, Color(0xFF9C27B0)),
    SkinShopItem("skin_rainbow_star", "Golden Star Prism", "Luminous 3D crystal star skin", 500, CyberGold),
    SkinShopItem("skin_diamond", "Cyber Diamond", "Glowing blue gemstone octahedron", 800, Color(0xFF00E5FF))
)

@Composable
fun ShopScreen(
    currentUser: UserEntity,
    onUnlockSkin: (String, Int) -> Unit,
    onSelectSkin: (String) -> Unit,
    onBack: () -> Unit
) {
    val unlockedSet = currentUser.unlockedSkins.split(",").toSet()

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("shop_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = LightText)
                    }
                    Column {
                        Text(text = "3D SKIN SHOP", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                        Text(text = "Customize Player Ball & Trails", fontSize = 12.sp, color = DimText)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "🪙 ${currentUser.totalCoins}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = CyberGreen)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(SHOP_ITEMS) { item ->
                    val isUnlocked = unlockedSet.contains(item.id)
                    val isActive = currentUser.activeSkin == item.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_item_${item.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(item.previewColor)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightText)
                                Text(text = item.description, fontSize = 12.sp, color = DimText)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyberGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "EQUIPPED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                                }
                            } else if (isUnlocked) {
                                Button(
                                    onClick = { onSelectSkin(item.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("equip_${item.id}")
                                ) {
                                    Text("EQUIP", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            } else {
                                Button(
                                    onClick = { onUnlockSkin(item.id, item.price) },
                                    enabled = currentUser.totalCoins >= item.price,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("unlock_${item.id}")
                                ) {
                                    Text("🪙 ${item.price}", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
