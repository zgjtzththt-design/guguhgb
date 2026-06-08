package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onStartGame: (brandName: String, playerName: String) -> Unit
) {
    var brandName by remember { mutableStateFlowOf("") }
    var playerName by remember { mutableStateFlowOf("") }
    var showError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A), // Slate 900
                            Color(0xFF1E1B4B), // Indigo 950
                            Color(0xFF311042)  // Deep purple/magenta
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val isWide = maxWidth > 600.dp

                if (isWide) {
                    // Side-by-side split landscape/wide layout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left/Right split panel: Info Branding (Weight 1.1)
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFFB703).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = "جوال",
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "صانع الهواتف الذكية",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "ابدأ رحلة المليار دولار وصمم أجهزتك الخاصة وتحدّ كبار السوق العالمية مع إستراتيجية تصنيع وإبداع غير مسبوقة.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Right/Left split panel: Form & Actions (Weight 0.9)
                        Column(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "تأسيس الشركة الجديدة",
                                        color = Color(0xFFFFB703),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )

                                    // Corporate/Brand Name
                                    OutlinedTextField(
                                        value = brandName,
                                        onValueChange = {
                                            brandName = it
                                            showError = false
                                        },
                                        label = { Text("اسم الشركة التجارية (مثال: ألفا تك)", color = Color.White.copy(alpha = 0.6f)) },
                                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = "شركة") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("brand_name_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFFFB703),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            cursorColor = Color(0xFFFFB703)
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Player name
                                    OutlinedTextField(
                                        value = playerName,
                                        onValueChange = {
                                            playerName = it
                                            showError = false
                                        },
                                        label = { Text("اسم المدير التنفيذي (CEO)", color = Color.White.copy(alpha = 0.6f)) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "شخص") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ceo_name_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFFFB703),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            cursorColor = Color(0xFFFFB703)
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    if (showError) {
                                        Text(
                                            text = "الرجاء تعبئة اسم الشركة واسم المدير للمتابعة!",
                                            color = Color(0xFFEF233C),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (brandName.isNotBlank() && playerName.isNotBlank()) {
                                        onStartGame(brandName, playerName)
                                    } else {
                                        showError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFB703),
                                    contentColor = Color(0xFF1E1B4B)
                                ),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("start_game_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "بدء")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تأسيس وبدء اللعب",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Compact/Vertical Portrait Layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon header
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFFFB703).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "جوال",
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(45.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "صانع الهواتف الذكية",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "ابدأ رحلة المليار دولار وصمم أجهزتك الخاصة وتحدّ كبار السوق",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Text Inputs Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                              ) {
                                Text(
                                    text = "تأسيس الشركة الجديدة",
                                    color = Color(0xFFFFB703),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )

                                // Corporate/Brand Name String
                                OutlinedTextField(
                                    value = brandName,
                                    onValueChange = {
                                        brandName = it
                                        showError = false
                                    },
                                    label = { Text("اسم الشركة التجارية (مثال: ألفا تك)", color = Color.White.copy(alpha = 0.6f)) },
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = "شركة") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("brand_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFB703),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        cursorColor = Color(0xFFFFB703)
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Player string name
                                OutlinedTextField(
                                    value = playerName,
                                    onValueChange = {
                                        playerName = it
                                        showError = false
                                    },
                                    label = { Text("اسم المدير التنفيذي (CEO)", color = Color.White.copy(alpha = 0.6f)) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "شخص") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ceo_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFB703),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        cursorColor = Color(0xFFFFB703)
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (showError) {
                                    Text(
                                        text = "الرجاء تعبئة اسم الشركة واسم المدير للمتابعة!",
                                        color = Color(0xFFEF233C),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                if (brandName.isNotBlank() && playerName.isNotBlank()) {
                                    onStartGame(brandName, playerName)
                                } else {
                                    showError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB703),
                                contentColor = Color(0xFF1E1B4B)
                            ),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("start_game_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "بدء")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تأسيس وبدء اللعب",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper block to make mutable states flowable nicely
private fun <T> mutableStateFlowOf(value: T) = mutableStateOf(value)
