package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProgress
import com.example.game.LevelChallenge
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelBriefScreen(
    progress: PlayerProgress,
    challenge: LevelChallenge,
    onStartCreator: () -> Unit,
    onBack: () -> Unit
) {
    val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF0F1E36)  // Slate dark blue
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with back arrow and HQ home button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, // Standard back arrow
                        contentDescription = "الرجوع",
                        tint = Color.White
                    )
                }

                // Back to Headquarters Map Button
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB703).copy(alpha = 0.15f),
                        contentColor = Color(0xFFFFB703)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFFB703).copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CorporateFare,
                        contentDescription = "مقر الشركة",
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "العودة للمقر 🏢",
                        color = Color(0xFFFFB703),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "تفاصيل المستوى والتحدي",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right
                )
            }

            // Challenge Title Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFB703).copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, Color(0xFFFFB703).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = challenge.titleAr,
                        color = Color(0xFFFFB703),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = challenge.subtitleAr,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    Text(
                        text = "ديموغرافية السوق المستهدفة:",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = challenge.targetSegmentAr,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            }

            // Detailed Story Summary
            Text(
                text = challenge.descriptionAr,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Stage Targets Metric Header
            Text(
                text = "الأهداف المطلوبة لاجتياز التحدي",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            // Metric List Grid
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Min sales target
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${challenge.requiredUnitsSold} جهاز",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "مبيعات الهواتف المطلوبة", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF5CC4FF), modifier = Modifier.size(18.dp))
                        }
                    }

                    // Profit target
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = usdFormatter.format(challenge.requiredNetProfit),
                            color = Color(0xFF4AD66D),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "صافي الأرباح المستهدف", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF4AD66D), modifier = Modifier.size(18.dp))
                        }
                    }

                    // Rating target
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${challenge.ratingRequirement}★ أو أعلى",
                            color = Color(0xFFFFB703),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "التقييم المطلوب من الجمهور", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Competitor Details Title
            Text(
                text = "المنافسون الفاعلون في السوق حالياً",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            // Two competitors cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Competitor 1
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFEF233C), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = challenge.competitor1.brandName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "جهاز: ${challenge.competitor1.phoneName}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "السعر المقدر: ${usdFormatter.format(challenge.competitor1.marketPrice)}",
                            color = Color(0xFFFFB703),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Competitor 2
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFFEF233C), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = challenge.competitor2.brandName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "جهاز: ${challenge.competitor2.phoneName}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "السعر المقدر: ${usdFormatter.format(challenge.competitor2.marketPrice)}",
                            color = Color(0xFFFFB703),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = onStartCreator,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB703),
                    contentColor = Color(0xFF0F172A)
                ),
                contentPadding = PaddingValues(16.dp),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_lab_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.DesignServices, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دخول مختبر التطوير والإنشاء",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
