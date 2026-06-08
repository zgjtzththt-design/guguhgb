package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreatedPhone
import com.example.data.PlayerProgress
import com.example.game.GameDefaults
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

enum class DashboardPopup {
    NONE,
    NEW_PHONE,    // Level select & new phone planner
    PROFITS,      // Earnings history & statistics
    COMPETITORS,  // Actual market competitor specifications
    WORKERS,      // Employee training & upgrades
    FACTORY,      // Production lines & speed upgrades
    NEWS          // Market reports & dynamic pricing studio
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    progress: PlayerProgress,
    history: List<CreatedPhone>,
    onSelectLevel: (levelNum: Int) -> Unit,
    onRestartGame: () -> Unit,
    onUpgradeWorker: (workerKey: String, cost: Double) -> Unit,
    onUpgradeFactory: () -> Unit,
    onUpgradeEfficiency: () -> Unit,
    onUpgradeMachineSpeed: () -> Unit,
    onUpgradeRD: () -> Unit,
    activeNews: com.example.game.NewsItem?,
    onRefreshNews: () -> Unit
) {
    val context = LocalContext.current
    val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    // State to refresh shared preferences readings
    var upgradeCounter by remember { mutableStateOf(0) }

    // Read worker levels from SharedPreferences
    val getWorkerLevel = { key: String ->
        // Triggered by upgradeCounter changes to recompose
        val trigger = upgradeCounter
        context.getSharedPreferences("HqWorkers", android.content.Context.MODE_PRIVATE).getInt(key, 1)
    }

    // Current active popup overlay state
    var activePopup by rememberSaveable { mutableStateOf(DashboardPopup.NONE) }

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
        // Main Screen Interface with Headquarters Map & Lateral Menu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // -------------------------------------------------------------
            // UPPER BAR (اسم الشركة والمبالغ والمدير)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: CEO name and Reset icon
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onRestartGame,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "مسح اللعبة والبدء من جديد",
                                tint = Color(0xFFEF233C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "المدير التنفيذي",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Left
                            )
                            Text(
                                text = progress.playerName,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Left
                            )
                        }
                    }
                }

                // Right side: Brand Name & Funds (المبالغ)
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = progress.brandName,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Right
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CorporateFare,
                            contentDescription = null,
                            tint = Color(0xFF5CC4FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    // Funds (مبالغ الشركة)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = usdFormatter.format(progress.funds),
                            color = Color(0xFF4AD66D),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.testTag("funds_label_top")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "💵",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // CENTRAL ARENA WITH LATERAL CIRCULAR BUTTONS
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lateral Side Column containing small circular menu buttons
                Column(
                    modifier = Modifier
                        .width(68.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Button 1: Smartphone (هاتف جديد)
                    LateralCircularButton(
                        icon = Icons.Default.Smartphone,
                        label = "هاتف جديد",
                        tintColor = Color(0xFFFFB703),
                        testTag = "lateral_make_phone_btn",
                        onClick = { activePopup = DashboardPopup.NEW_PHONE }
                    )

                    // Button 2: Profits (الأرباح)
                    LateralCircularButton(
                        icon = Icons.Default.TrendingUp,
                        label = "الأرباح",
                        tintColor = Color(0xFF4AD66D),
                        testTag = "lateral_profits_btn",
                        onClick = { activePopup = DashboardPopup.PROFITS }
                    )

                    // Button 3: Competitors (المنافسون)
                    LateralCircularButton(
                        icon = Icons.Default.Compare,
                        label = "المنافسون",
                        tintColor = Color(0xFF5CC4FF),
                        testTag = "lateral_competitors_btn",
                        onClick = { activePopup = DashboardPopup.COMPETITORS }
                    )

                    // Button 4: Workers (الطاقم)
                    LateralCircularButton(
                        icon = Icons.Default.School,
                        label = "الطاقم",
                        tintColor = Color(0xFFE07A5F),
                        testTag = "lateral_workers_btn",
                        onClick = { activePopup = DashboardPopup.WORKERS }
                    )

                    // Button 5: Factory (المصنع)
                    LateralCircularButton(
                        icon = Icons.Default.PrecisionManufacturing,
                        label = "المصنع",
                        tintColor = Color(0xFF8D99AE),
                        testTag = "lateral_factory_btn",
                        onClick = { activePopup = DashboardPopup.FACTORY }
                    )

                    // Button 6: News (الأخبار)
                    LateralCircularButton(
                        icon = Icons.Default.Newspaper,
                        label = "الأخبار",
                        tintColor = Color(0xFFE2E8F0),
                        testTag = "lateral_news_btn",
                        onClick = { activePopup = DashboardPopup.NEWS }
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("hq_map_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF5CC4FF).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "المستوى الحالي: ${progress.currentLevel}",
                                    color = Color(0xFF5CC4FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Text(
                                text = "🏢 خريطة المقر الرئيسي والمصانع",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                        }

                        // Map grid containing the facilities
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. Research & Strategic Planning
                                MapBuildingTile(
                                    modifier = Modifier.weight(1f),
                                    title = "مبنى البحوث والتخطيط",
                                    description = "انقر لطلب هاتف ذكي جديد",
                                    statusColor = Color(0xFFFFB703),
                                    isGlowing = true,
                                    icon = Icons.Default.Leaderboard,
                                    onClick = { activePopup = DashboardPopup.NEW_PHONE }
                                )

                                // 2. High-Tech Factory & Assembly Line
                                MapBuildingTile(
                                    modifier = Modifier.weight(1f),
                                    title = "مصنع تجميع الأجهزة",
                                    description = "${history.size} هاتف تم إطلاقه",
                                    statusColor = Color(0xFF4AD66D),
                                    isGlowing = history.isNotEmpty(),
                                    icon = Icons.Default.Factory,
                                    onClick = { activePopup = DashboardPopup.PROFITS }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 3. Workers Housing Complex
                                MapBuildingTile(
                                    modifier = Modifier.weight(1f),
                                    title = "مجمع رعاية وتدريب العمال",
                                    description = "تطوير كفاءة المهندسين ومصممي الهياكل",
                                    statusColor = Color(0xFFE07A5F),
                                    isGlowing = progress.funds >= 12000.0,
                                    icon = Icons.Default.Groups,
                                    onClick = { activePopup = DashboardPopup.WORKERS }
                                )

                                // 4. Competitor Monitoring Station
                                MapBuildingTile(
                                    modifier = Modifier.weight(1f),
                                    title = "رادار مراقبة المنافسين بالسوق",
                                    description = "تحليل الأداء وأسعار أجهزة Rivals",
                                    statusColor = Color(0xFF5CC4FF),
                                    isGlowing = true,
                                    icon = Icons.Default.Troubleshoot,
                                    onClick = { activePopup = DashboardPopup.COMPETITORS }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 5. News Studio (Added)
                                MapBuildingTile(
                                    modifier = Modifier.weight(1f),
                                    title = "استديو أخبار السوق العالمي",
                                    description = "تقارير تقلبات أسعار قطع الغيار وأزمات الشحن",
                                    statusColor = Color(0xFFE2E8F0),
                                    isGlowing = true,
                                    icon = Icons.Default.RecordVoiceOver,
                                    onClick = { activePopup = DashboardPopup.NEWS }
                                )

                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        // Map interactive tip
                        Text(
                            text = "💡 انقر على أحد مرافق المقر بالخريطة أعلاه للفتح فورا، أو استخدم الأزرار الدائرية الجانبية.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // FULL SCREEN OVERLAY PANEL OVER THE MAP (واجهات كاملة مدمجة)
        // -------------------------------------------------------------
        if (activePopup != DashboardPopup.NONE) {
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
                    .clickable { /* Prevents clicks through the modal */ }
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Header of active popup modal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activePopup = DashboardPopup.NONE },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = when (activePopup) {
                                DashboardPopup.NEW_PHONE -> "تحديات الإنتاج وصنع هاتف جديد"
                                DashboardPopup.PROFITS -> "الأرباح والأجهزة المطروحة"
                                DashboardPopup.COMPETITORS -> "مراقبة وتحليل أجهزة المنافسين"
                                DashboardPopup.WORKERS -> "تطوير كفاءة وسكن الموظفين"
                                DashboardPopup.FACTORY -> "تطوير المصنع والبحث العلمي (R&D)"
                                DashboardPopup.NEWS -> "استديو أخبار السوق التقني"
                                else -> ""
                            },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Right
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    )

                        // Interactive content scrollable container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (activePopup) {
                                DashboardPopup.NEW_PHONE -> {
                                    NewPhonePopupContent(
                                        progress = progress,
                                        onSelectLevel = { lvl ->
                                            activePopup = DashboardPopup.NONE
                                            onSelectLevel(lvl)
                                        }
                                    )
                                }
                                DashboardPopup.PROFITS -> {
                                    ProfitsPopupContent(
                                        progress = progress,
                                        history = history,
                                        usdFormatter = usdFormatter
                                    )
                                }
                                DashboardPopup.COMPETITORS -> {
                                    CompetitorsPopupContent(
                                        progress = progress,
                                        usdFormatter = usdFormatter
                                    )
                                }
                                DashboardPopup.WORKERS -> {
                                    WorkersPopupContent(
                                        progress = progress,
                                        getWorkerLevel = getWorkerLevel,
                                        onUpgradeWorker = onUpgradeWorker,
                                        usdFormatter = usdFormatter,
                                        onRefresh = { upgradeCounter++ }
                                    )
                                }
                                DashboardPopup.FACTORY -> {
                                    FactoryPopupContent(
                                        progress = progress,
                                        onUpgradeFactory = onUpgradeFactory,
                                        onUpgradeEfficiency = onUpgradeEfficiency,
                                        onUpgradeMachineSpeed = onUpgradeMachineSpeed,
                                        onUpgradeRD = onUpgradeRD,
                                        usdFormatter = usdFormatter
                                    )
                                }
                                DashboardPopup.NEWS -> {
                                    NewsPopupContent(
                                        activeNews = activeNews,
                                        onRefreshNews = onRefreshNews
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

// -------------------------------------------------------------
// REUSABLE SUB-COMPONENTS & HELPERS
// -------------------------------------------------------------

@Composable
fun MapBuildingTile(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    statusColor: Color,
    isGlowing: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.04f)
        ),
        border = BorderStroke(
            width = if (isGlowing) 1.5.dp else 1.dp,
            color = if (isGlowing) statusColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal status bulb
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isGlowing) statusColor else Color.White.copy(alpha = 0.2f))
                )

                // Building icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Right,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NewPhonePopupContent(
    progress: PlayerProgress,
    onSelectLevel: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "🎯 خطوة التخطيط الاستراتيجي بالأقسام",
                        color = Color(0xFFFFB703),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = "اختر التحدي الذي يناسب قوتك وطاقتك الحالية لتبدأ هندسة الأجهزة وتصميم الهاردوير فورا.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        items(GameDefaults.Levels) { level ->
            val isUnlocked = level.levelNumber <= progress.currentLevel
            val isCompleted = level.levelNumber < progress.currentLevel

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) {
                        if (isCompleted) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFF1E1B4B).copy(alpha = 0.7f)
                    } else {
                        Color.White.copy(alpha = 0.02f)
                    }
                ),
                border = if (!isCompleted && isUnlocked) {
                    BorderStroke(1.2.dp, Color(0xFFFFB703))
                } else null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isUnlocked) {
                        onSelectLevel(level.levelNumber)
                    }
                    .testTag("level_card_${level.levelNumber}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "مغلق",
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4AD66D).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "تم الاكتمال",
                                tint = Color(0xFF4AD66D),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFB703))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "الحالي",
                                color = Color(0xFF1E1B4B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = level.titleAr,
                            color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "المستهدف: ${level.targetSegmentAr}",
                            color = if (isUnlocked) Color(0xFFFFB703).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitsPopupContent(
    progress: PlayerProgress,
    history: List<CreatedPhone>,
    usdFormatter: NumberFormat
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overall corporate summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color(0xFF5CC4FF)
                        )
                        Text(
                            text = "${progress.fans} عضو",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "قاعدة الجمهور",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = Color(0xFFFFB703)
                        )
                        Text(
                            text = "${progress.phonesCreatedCount} أجهزة",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الهواتف المبتكرة",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "سجل الأجهزة المطروحة بالأسواق",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (history.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لم تطلق الشركة أي هواتف تجارية حتى الآن!",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(history) { phone ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_item_${phone.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    text = usdFormatter.format(phone.retailPrice),
                                color = Color(0xFFFFB703),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(phone.colorHex)))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = phone.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "المواصفات: ${phone.designTypeName} • ${phone.processorName}",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.05f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            _RatingDisplay(phone.starRating)

                            Text(
                                text = "مبيعات: ${phone.unitsSold}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )

                            Text(
                                text = "ربح: ${usdFormatter.format(phone.netProfit)}",
                                color = Color(0xFF4AD66D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompetitorsPopupContent(
    progress: PlayerProgress,
    usdFormatter: NumberFormat
) {
    val level = GameDefaults.Levels.firstOrNull { it.levelNumber == progress.currentLevel } ?: GameDefaults.Levels.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Market target Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5CC4FF).copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, Color(0xFF5CC4FF).copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "🎯 وضع المنافسة وسوق المستهلك",
                    color = Color(0xFF5CC4FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = level.targetSegmentAr,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = level.descriptionAr,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Text(
            text = "الشركات المنافسة وأجهزتها النشطة",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        // Rival 1
        _CompetitorCard(
            brandName = level.competitor1.brandName,
            productName = level.competitor1.phoneName,
            performance = level.competitor1.speedRating,
            camera = level.competitor1.cameraRating,
            price = level.competitor1.marketPrice,
            usdFormatter = usdFormatter
        )

        // Rival 2
        _CompetitorCard(
            brandName = level.competitor2.brandName,
            productName = level.competitor2.phoneName,
            performance = level.competitor2.speedRating,
            camera = level.competitor2.cameraRating,
            price = level.competitor2.marketPrice,
            usdFormatter = usdFormatter
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "🕵️ نصيحة مرقب السوق المالي",
                    color = Color(0xFF4AD66D),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "احرص على تصميم هاتف يفوق مواصفات أجهزة المنافسين (الأداء والكاميرا) بسعر مقارب أو أدنى لجذب المشترين فورا والحصول على حصة مبيعات قياسية!",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun WorkersPopupContent(
    progress: PlayerProgress,
    getWorkerLevel: (String) -> Int,
    onUpgradeWorker: (String, Double) -> Unit,
    usdFormatter: NumberFormat,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current

    val specialistsList = listOf(
        SpecialistItem(
            name = "أحمد - خبير معالجات السيليكون",
            avatar = "👨‍💻",
            key = "worker_performance_lvl",
            statBoostName = "أداء ومعالجة الأجهزة",
            description = "مختص بتحسين تردد المعالجات ومطابقة معمارية الذاكرة للحماية من البطء."
        ),
        SpecialistItem(
            name = "سارة - رائدة التصميم والهياكل",
            avatar = "👩‍🎨",
            key = "worker_design_lvl",
            statBoostName = "أناقة وتصميم ونحافة الهيكل",
            description = "تعتني بالجمال والوزن والخامات المترفة لإضفاء أناقة رائعة."
        ),
        SpecialistItem(
            name = "خالد - خبير مستشعرات الرؤية",
            avatar = "📸",
            key = "worker_camera_lvl",
            statBoostName = "قوة التقريب والوضوح للكاميرات",
            description = "ضبط فتحة العدسة ومعالجة بكسلات التصوير بدقة فائقة."
        ),
        SpecialistItem(
            name = "عمر - مهندس السوفتوير والشحن",
            avatar = "⚡",
            key = "worker_tech_lvl",
            statBoostName = "جاذبية التقنيات والميزات المبتكرة",
            description = "مواءمة واجهة التشغيل وتسريع بروتوكول نقل وتخزين الشحن."
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "تدريب العمال يعطي زيادة +6.0 نقاط لكل مستوى مجانًا لكل الهواتف المستقبلية!",
                        color = Color(0xFFE07A5F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        items(specialistsList) { spec ->
            val curLvl = getWorkerLevel(spec.key)
            val upgradeCost = when (curLvl) {
                1 -> 12000.0
                2 -> 28000.0
                3 -> 55000.0
                else -> 0.0
            }
            val canUpgrade = curLvl < 4
            val hasFunds = progress.funds >= upgradeCost

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Level Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE07A5F).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "المستوى $curLvl/4",
                                color = Color(0xFFE07A5F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = spec.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = spec.avatar,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = spec.description,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Boost display banner
                    val boostValue = (curLvl - 1) * 6
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.02f))
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "+$boostValue نقاط إضافية",
                                color = Color(0xFF4AD66D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "دعم: ${spec.statBoostName}",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Upgrade buttons
                    if (canUpgrade) {
                        Button(
                            onClick = {
                                if (hasFunds) {
                                    onUpgradeWorker(spec.key, upgradeCost)
                                    onRefresh()
                                    Toast.makeText(context, "تم تدريب الموظف بنجاح! 🚀", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = hasFunds,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasFunds) Color(0xFFE07A5F) else Color.White.copy(alpha = 0.05f),
                                contentColor = if (hasFunds) Color.White else Color.White.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (hasFunds) "تدريب بكلفة ${usdFormatter.format(upgradeCost)}"
                                       else "رصيد غير كافٍ للترقية (${usdFormatter.format(upgradeCost)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4AD66D).copy(alpha = 0.12f))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF4AD66D),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تم الوصول للكفاءة التجميعية القصوى! ⭐",
                                    color = Color(0xFF4AD66D),
                                    fontSize = 10.sp,
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

@Composable
fun _CompetitorCard(
    brandName: String,
    productName: String,
    performance: Double,
    camera: Double,
    price: Double,
    usdFormatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = usdFormatter.format(price),
                    color = Color(0xFFFFB703),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = brandName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "جهاز: $productName",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "العدسات والكاميرات",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp
                    )
                    Text(
                        text = "$camera / 100",
                        color = Color(0xFF5CC4FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "قوة المعالجة والسرعة",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp
                    )
                    Text(
                        text = "$performance / 100",
                        color = Color(0xFF4AD66D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun _RatingDisplay(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "★ ${String.format(Locale.US, "%.1f", rating)}",
            color = Color(0xFFFFB703),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper Class to present worker specialists bios
data class SpecialistItem(
    val name: String,
    val avatar: String,
    val key: String,
    val statBoostName: String,
    val description: String
)

@Composable
fun LateralCircularButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tintColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
            .padding(vertical = 4.dp)
            .width(62.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.5.dp, tintColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FactoryVisualization(
    productionSpeedMultiplier: Double,
    onUpgradeFactory: () -> Unit,
    onUpgradeEfficiency: () -> Unit,
    onUpgradeMachineSpeed: () -> Unit,
    canAffordFactory: Boolean,
    progress: PlayerProgress
) {
    val infiniteTransition = rememberInfiniteTransition(label = "factory")
    
    // Conveyor speed is based on multiplier
    val cycleTime = (2000.0 / productionSpeedMultiplier).toInt().coerceAtLeast(500)
    
    val boxProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(cycleTime, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "box"
    )

    val armRotation by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween((cycleTime / 4).coerceAtLeast(150), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arm"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main Visualization Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            // Factory Floor Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 20.dp.toPx()
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.03f),
                        start = Offset(x * step, 0f),
                        end = Offset(x * step, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.03f),
                        start = Offset(0f, y * step),
                        end = Offset(size.width, y * step),
                        strokeWidth = 1f
                    )
                }
            }

            // Assembly Point Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛠️", fontSize = 24.sp, modifier = Modifier.graphicsLayer(alpha = 0.3f))
            }

            // Component Feeders (Top)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                ComponentPart("🔋", boxProgress)
                ComponentPart("🔲", (boxProgress + 0.3f) % 1f)
                ComponentPart("📷", (boxProgress + 0.6f) % 1f)
            }

            // Primary Conveyor Belt (Bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 30.dp)
                    .height(30.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(4.dp))
                    .border(2.dp, Color(0xFF475569), RoundedCornerShape(4.dp))
            ) {
                // Belt Moving Texture
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val segmentWidth = 15.dp.toPx()
                    val offset = (boxProgress * segmentWidth)
                    for (i in -1..(size.width / segmentWidth).toInt() + 1) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.1f),
                            start = Offset(i * segmentWidth + offset, 0f),
                            end = Offset(i * segmentWidth + offset, size.height),
                            strokeWidth = 2f
                        )
                    }
                }

                // Finished Products moving to carton
                for (i in 0..2) {
                    val individualProgress = (boxProgress + (i * 0.33f)) % 1f
                    Box(
                        modifier = Modifier
                            .offset(x = (20 + (individualProgress * 180)).dp, y = 4.dp)
                            .size(22.dp)
                            .background(Color(0xFF94A3B8), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White.copy(alpha = 0.5f))
                    }
                }
            }

            // Robotic Assembly Arm
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-30).dp)
                    .graphicsLayer(rotationZ = armRotation, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f))
                    .size(width = 8.dp, height = 60.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFF334155))),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(12.dp)
                        .background(Color(0xFF475569), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )
            }

            // Packing Carton (Receipt Point)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 20.dp)
                    .size(44.dp)
                    .background(Color(0xFFB45309), RoundedCornerShape(6.dp))
                    .border(2.dp, Color(0xFF92400E), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📦", fontSize = 20.sp)
                if (boxProgress > 0.9f) {
                    Text(text = "✨", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp))
                }
            }

            // Live status tag
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF4AD66D).copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF4AD66D), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "تجميع نشط", color = Color(0xFF4AD66D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Side Control Buttons Area
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FactorySideButton(
                icon = Icons.Default.PrecisionManufacturing,
                label = "كفاءة",
                color = Color(0xFF8D99AE),
                onClick = onUpgradeEfficiency,
                enabled = progress.funds >= (GameDefaults.MachineUpgrades.find { it.id == "up_efficiency" }?.baseCost?.times(progress.efficiencyLevel.toDouble().pow(1.6)) ?: 0.0) && progress.efficiencyLevel < 10
            )
            
            FactorySideButton(
                icon = Icons.Default.ElectricBolt,
                label = "تسريع",
                color = Color(0xFFFFB703),
                onClick = onUpgradeMachineSpeed,
                enabled = progress.funds >= (GameDefaults.MachineUpgrades.find { it.id == "up_speed" }?.baseCost?.times(progress.machineSpeedLevel.toDouble().pow(1.6)) ?: 0.0) && progress.machineSpeedLevel < 10
            )

            Spacer(modifier = Modifier.weight(1f))

            // Manual Assembly Clicker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4AD66D).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF4AD66D).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { /* Fast produce logic if state allowed */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "👆", fontSize = 18.sp)
                    Text(text = "دفع", color = Color(0xFF4AD66D), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ComponentPart(emoji: String, progress: Float) {
    val yOffset = (progress * 50).dp
    Box(
        modifier = Modifier
            .offset(y = yOffset)
            .size(24.dp)
            .graphicsLayer(alpha = 1f - (progress * 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 16.sp)
    }
}

@Composable
fun FactorySideButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (enabled) color.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = if (enabled) color else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
            Text(text = label, color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FactoryPopupContent(
    progress: com.example.data.PlayerProgress,
    onUpgradeFactory: () -> Unit,
    onUpgradeEfficiency: () -> Unit,
    onUpgradeMachineSpeed: () -> Unit,
    onUpgradeRD: () -> Unit,
    usdFormatter: java.text.NumberFormat
) {
    val currentFactory = GameDefaults.FactoryConfigs.firstOrNull { it.level == progress.factoryLevel } ?: GameDefaults.FactoryConfigs[0]
    val nextFactory = GameDefaults.FactoryConfigs.getOrNull(progress.factoryLevel) // index is level for 0-based
    
    val currentRDLevel = progress.rdLevel
    val rdUpgradeCost = (currentRDLevel * 75000.0)

    val speedUpgrade = GameDefaults.MachineUpgrades.find { it.id == "up_speed" }!!
    val efficiencyUpgrade = GameDefaults.MachineUpgrades.find { it.id == "up_efficiency" }!!

    val speedCost = speedUpgrade.baseCost * progress.machineSpeedLevel.toDouble().pow(1.6)
    val efficiencyCost = efficiencyUpgrade.baseCost * progress.efficiencyLevel.toDouble().pow(1.6)

    Column(modifier = Modifier.fillMaxSize()) {
        // The Map / Visualization at the Top
        FactoryVisualization(
            productionSpeedMultiplier = currentFactory.productionSpeedMultiplier * (1.0 + (progress.machineSpeedLevel - 1) * 0.10),
            onUpgradeFactory = onUpgradeFactory,
            onUpgradeEfficiency = onUpgradeEfficiency,
            onUpgradeMachineSpeed = onUpgradeMachineSpeed,
            canAffordFactory = nextFactory?.let { progress.funds >= it.upgradeCost } ?: false,
            progress = progress
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF8D99AE).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color(0xFF8D99AE).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                        Text(text = "🏭 إدارة منشآت التصنيع والبحوث", color = Color(0xFF8D99AE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "طور مصنعك لزيادة سرعة الإنتاج وتقليل تكاليف المواد الخام لكل وحدة، واستثمر في R&D لتقليل تكاليف تطوير النماذج الأولية للهواتف.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Factory Section
            item {
                Text(text = "خطوط الإنتاج والتصنيع (المصنع الكبير)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            }

            item {
                _FactoryUpgradeCard(
                    title = "مستوى المصنع: ${currentFactory.nameAr}",
                    currentStats = "سرعة أساسية: x${currentFactory.productionSpeedMultiplier} • خصم للمواد: -${usdFormatter.format(currentFactory.unitCostReduction)}/وحدة",
                    nextStats = nextFactory?.let { "التالي: x${it.productionSpeedMultiplier} سرعة • -${usdFormatter.format(it.unitCostReduction)} خصم" } ?: "تم الوصول لأقصى مستوى",
                    cost = nextFactory?.upgradeCost ?: 0.0,
                    canAfford = nextFactory?.let { progress.funds >= it.upgradeCost } ?: false,
                    isMaxed = nextFactory == null,
                    icon = Icons.Default.PrecisionManufacturing,
                    color = Color(0xFF8D99AE),
                    usdFormatter = usdFormatter,
                    onUpgrade = onUpgradeFactory
                )
            }

            // Machine Upgrades
            item {
                Text(text = "ترقيات الآلات (خط التجميع)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            }

            item {
                _FactoryUpgradeCard(
                    title = "سرعة حركة الآلات (Lvl ${progress.machineSpeedLevel})",
                    currentStats = "زيادة السرعة الحالية: +${(progress.machineSpeedLevel - 1) * 10}%",
                    nextStats = "التطوير التالي يضيف 10% سرعة تجميع إضافية",
                    cost = speedCost,
                    canAfford = progress.funds >= speedCost,
                    isMaxed = progress.machineSpeedLevel >= speedUpgrade.maxLevel,
                    icon = Icons.Default.ElectricBolt,
                    color = Color(0xFFFFB703),
                    usdFormatter = usdFormatter,
                    onUpgrade = onUpgradeMachineSpeed
                )
            }

            item {
                _FactoryUpgradeCard(
                    title = "كفاءة التجميع الذكي (Lvl ${progress.efficiencyLevel})",
                    currentStats = "خصم إضافي للمواد: -${usdFormatter.format((progress.efficiencyLevel - 1) * 2.5)}/وحدة",
                    nextStats = "التطوير التالي يقلل هالك المواد ويزيد الربح بكل وحدة",
                    cost = efficiencyCost,
                    canAfford = progress.funds >= efficiencyCost,
                    isMaxed = progress.efficiencyLevel >= efficiencyUpgrade.maxLevel,
                    icon = Icons.Default.SettingsSuggest,
                    color = Color(0xFF4AD66D),
                    usdFormatter = usdFormatter,
                    onUpgrade = onUpgradeEfficiency
                )
            }

            // R&D Section
            item {
                Text(text = "قسم البحث والتطوير (R&D)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            }

            item {
                _FactoryUpgradeCard(
                    title = "مستوى البحوث: المستوى $currentRDLevel",
                    currentStats = "تخفيض تكلفة تطوير الهواتف: ${currentRDLevel * 5}%",
                    nextStats = "التالي: ${(currentRDLevel + 1) * 5}% تخفيض لتكاليف النماذج",
                    cost = rdUpgradeCost,
                    canAfford = progress.funds >= rdUpgradeCost,
                    isMaxed = currentRDLevel >= 10,
                    icon = Icons.Default.Science,
                    color = Color(0xFF5CC4FF),
                    usdFormatter = usdFormatter,
                    onUpgrade = onUpgradeRD
                )
            }
        }
    }
}

@Composable
fun NewsPopupContent(
    activeNews: com.example.game.NewsItem?,
    onRefreshNews: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // News Studio Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "بث مباشر من ستوديو الاقتصاد التقني",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (activeNews != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeNews.isGoodNews) Color(0xFF064E3B) else Color(0xFF451A03)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (activeNews.isGoodNews) Color(0xFF10B981) else Color(0xFFF59E0B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeNews.isGoodNews) "خبر إيجابي ✅" else "خبر عاجل 🚨",
                            color = if (activeNews.isGoodNews) Color(0xFF34D399) else Color(0xFFFBBF24),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeNews.titleAr,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Right
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = activeNews.contentAr,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = activeNews.impactDescAr,
                                color = if (activeNews.isGoodNews) Color(0xFF34D399) else Color(0xFFF87171),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (activeNews.isGoodNews) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (activeNews.isGoodNews) Color(0xFF34D399) else Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRefreshNews,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "تحديث تقرير السوق العالمي", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "تتغير أخبار السوق بشكل دوري عند إطلاق هواتف جديدة أو تحديث البث يدويًا. تابع الأخبار لتقليل تكاليف الإنتاج!",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun _FactoryUpgradeCard(
    title: String,
    currentStats: String,
    nextStats: String,
    cost: Double,
    canAfford: Boolean,
    isMaxed: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    usdFormatter: java.text.NumberFormat,
    onUpgrade: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = currentStats, color = color.copy(alpha = 0.8f), fontSize = 10.sp)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
            
            Text(text = nextStats, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isMaxed) {
                Button(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        contentColor = Color(0xFF0F172A),
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "تطوير الآن (${usdFormatter.format(cost)})", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "وصلت لأقصى تطوير متاح",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

