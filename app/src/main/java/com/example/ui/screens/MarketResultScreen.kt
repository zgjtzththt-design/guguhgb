package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.CompetitorPhoneResult
import com.example.game.LevelChallenge
import com.example.game.PhoneStats
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketResultScreen(
    challenge: LevelChallenge,
    phoneName: String,
    specs: PhoneStats,
    retailPrice: Double,
    unitsSold: Int,
    grossRevenue: Double,
    netProfit: Double,
    finalRating: Float,
    customerReviews: List<String>,
    isLevelBeaten: Boolean,
    competitorPhones: List<CompetitorPhoneResult>,
    onFinish: () -> Unit
) {
    val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    // Sort competition list to build a podium of sales
    val allCompetitors = (competitorPhones + CompetitorPhoneResult(
        companyName = "شركتك",
        phoneName = phoneName,
        cost = retailPrice,
        rating = finalRating,
        unitsSold = unitsSold
    )).sortedByDescending { it.unitsSold }

    val myRank = allCompetitors.indexOfFirst { it.companyName == "شركتك" } + 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF020617)  // Slate 950
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
            // Header Title
            Text(
                text = "تقرير نتائج المبيعات والاستجابة",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = phoneName,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // LEVEL BEATEN / FAILED BANNER
            if (isLevelBeaten) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4AD66D).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF4AD66D)),
                    modifier = Modifier.fillMaxWidth().testTag("result_victory_banner")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4AD66D).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF4AD66D), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تم اجتياز التحدي بنجاح باهر! 🎉",
                            color = Color(0xFF4AD66D),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تهانينا! لقد حققت كافة متطلبات زبائن الفئة وأرباح التحدي المطلوبة. تم فتح المستوى القادم!",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF233C).copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.2.dp, Color(0xFFEF233C).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("result_failed_banner")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF233C).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF233C), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لم تتمكن من تحقيق الأهداف! ⚠️",
                            color = Color(0xFFEF233C),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تحتاج لتعديل السعر النهائي ليلائم الفئة، أو ترقية الأداء ليتحسن تقييم الزبائن. صافي الأرباح أو تقييم النجوم لم يستوفِ المتطلبات كلياً.",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            // Sales metrics values
            Text(
                text = "تقرير المبيعات والمالية",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sold devices
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${unitsSold} جهاز مباع (الهدف: ${challenge.requiredUnitsSold})",
                            color = if (unitsSold >= challenge.requiredUnitsSold) Color(0xFF4AD66D) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "عدد الأجهزة المباعة:", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    // Profit gained
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = usdFormatter.format(netProfit) + " (الهدف: " + usdFormatter.format(challenge.requiredNetProfit.toDouble()) + ")",
                            color = if (netProfit >= challenge.requiredNetProfit) Color(0xFF4AD66D) else Color(0xFFEF233C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "صافي ربح الصفقات:", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    // Average rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "★ ${finalRating} (الهدف: ${challenge.ratingRequirement}★)",
                            color = if (finalRating >= challenge.ratingRequirement) Color(0xFFFFB703) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "تقييم المستخدمين:", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
            }

            // Market Competitive Podium Rank
            Text(
                text = "ترتيب الحصة التنافسية لشركتك",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    when (myRank) {
                                        1 -> Color(0xFFFFB703).copy(alpha = 0.2f)
                                        2 -> Color(0xFFCCC2DC).copy(alpha = 0.2f)
                                        else -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "المركز $myRank/3",
                                color = when (myRank) {
                                    1 -> Color(0xFFFFB703)
                                    2 -> Color(0xFFCCC2DC)
                                    else -> Color(0xFFCD7F32)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "الموقع الحالي في السوق المالي",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                    allCompetitors.forEachIndexed { idx, competitor ->
                        val isMe = competitor.companyName == "شركتك"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isMe) Color(0xFFFFB703).copy(alpha = 0.05f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${competitor.unitsSold} مبيعات",
                                color = if (isMe) Color(0xFFFFB703) else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${idx + 1}. ${competitor.companyName} (${competitor.phoneName})",
                                color = if (isMe) Color(0xFFFFB703) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            // Customer comments and reviews
            Text(
                text = "آراء نقاد ومستهلكي الفئات لـ ${phoneName}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                customerReviews.forEachIndexed { i, review ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF5CC4FF).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "${i + 1}", color = Color(0xFF5CC4FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = review,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action return Button
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB703),
                    contentColor = Color(0xFF0F172A)
                ),
                contentPadding = PaddingValues(16.dp),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("back_to_dashboard_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "العودة للوحة القيادة ومتابعة التطور",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
