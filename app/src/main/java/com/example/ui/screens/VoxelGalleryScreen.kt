package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.game.engine.Camera3D
import com.example.game.engine.Vector3D
import com.example.game.engine.VoxelModel
import com.example.game.engine.VoxelModels
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DimText
import com.example.ui.theme.LightText
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoxelGalleryScreen(
    currentUser: UserEntity,
    onBack: () -> Unit
) {
    var selectedModelLevel by remember { mutableStateOf(1) }
    var rotationY by remember { mutableFloatStateOf(0f) }
    var rotationX by remember { mutableFloatStateOf(0.2f) }

    val unlockedMaxLevel = currentUser.maxLevelReached
    val modelsList = (1..maxOf(unlockedMaxLevel, 12)).map { lvl ->
        VoxelModels.getModelForLevel(lvl) to (lvl <= unlockedMaxLevel)
    }

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("gallery_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = LightText)
                }
                Column {
                    Text(text = "3D VOXEL GALLERY", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberGold)
                    Text(text = "Completed Level 3D Trophies", fontSize = 12.sp, color = DimText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive 3D Model Viewer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotationY += dragAmount.x * 0.01f
                            rotationX = (rotationX - dragAmount.y * 0.01f).coerceIn(-0.8f, 0.8f)
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                val currentModel = VoxelModels.getModelForLevel(selectedModelLevel)
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val camera = Camera3D(position = Vector3D(0f, 2f, -12f), focalLength = 400f)
                        camera.rotationX = rotationX

                        val screenWidth = size.width
                        val screenHeight = size.height

                        for (cube in currentModel.cubes) {
                            val radY = rotationY
                            val rx = cube.x * cos(radY) - cube.z * sin(radY)
                            val rz = cube.x * sin(radY) + cube.z * cos(radY)
                            val ry = cube.y.toFloat()

                            val proj = camera.project(
                                Vector3D(rx.toFloat() * 1.2f, ry * 1.2f, 15f + rz.toFloat() * 1.2f),
                                screenWidth,
                                screenHeight
                            )

                            if (proj.isVisible && proj.scale > 0f) {
                                val cubeSize = 22f * (proj.scale / 50f).coerceIn(0.2f, 3f)
                                drawRect(
                                    color = cube.color,
                                    topLeft = Offset(proj.offset.x - cubeSize / 2f, proj.offset.y - cubeSize / 2f),
                                    size = Size(cubeSize, cubeSize)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentModel.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(
                            text = "Drag to rotate 3D view",
                            fontSize = 11.sp,
                            color = DimText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "UNLOCKED 3D SCULPTURES", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DimText)
            Spacer(modifier = Modifier.height(10.dp))

            // Grid of Voxel Trophies
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(modelsList) { (model, isUnlocked) ->
                    val lvlNum = model.id.split("_").lastOrNull()?.toIntOrNull() ?: 1
                    val isSelected = lvlNum == selectedModelLevel

                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = isUnlocked) { selectedModelLevel = lvlNum }
                            .testTag("gallery_item_$lvlNum"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberViolet else if (isUnlocked) DarkSurface else DarkBackground
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isUnlocked) {
                                Text(text = "LVL $lvlNum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberGold)
                                Text(text = model.name.take(10), fontSize = 10.sp, color = LightText)
                            } else {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = DimText, modifier = Modifier.size(20.dp))
                                Text(text = "Lvl $lvlNum", fontSize = 10.sp, color = DimText)
                            }
                        }
                    }
                }
            }
        }
    }
}
