package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.game.*
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValuationScreen(
    progress: PlayerProgress,
    challenge: LevelChallenge,
    stats: PhoneStats,
    phoneName: String,
    selectedColor: PhoneColor,
    selectedProcessor: ProcessorOption,
    selectedCamera: CameraOption,
    selectedLens: LensOption,
    selectedDesign: DesignStyle,
    selectedBattery: BatteryOption,
    selectedDisplay: DisplayOption,
    selectedOs: OsOption,
    selectedTechs: Set<CustomTechnology>,
    proposedPrice: Double,
    researchTier: Int,
    manufacturingQuality: Int,
    marketingCampaign: Int,
    onPriceChanged: (Double) -> Unit,
    onBudgetChanged: (research: Int?, quality: Int?, marketing: Int?) -> Unit,
    onLaunch: () -> Unit,
    onBackToCreator: () -> Unit,
    onBackToDashboard: () -> Unit
) {
    val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    val qMultiplier = when (manufacturingQuality) {
        0 -> 0.80
        2 -> 1.25
        else -> 1.0
    }
    val adjustedCostOfGoods = stats.manufacturingCost * qMultiplier

    val flatRDPremium = when (researchTier) {
        0 -> 500.0
        1 -> 2500.0
        2 -> 8000.0
        else -> 2500.0
    }
    val flatMktPremium = when (marketingCampaign) {
        0 -> 0.0
        1 -> 1500.0
        2 -> 6500.0
        3 -> 22000.0
        else -> 1500.0
    }
    val totalDirectInvestment = flatRDPremium + flatMktPremium

    val minPriceLimit = maxOf(30.0, (adjustedCostOfGoods * 0.8).roundToInt().toDouble())
    val maxPriceLimit = maxOf(200.0, (adjustedCostOfGoods * 4.0).roundToInt().toDouble())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF0F1932)  // Slate dark blue
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
                IconButton(onClick = onBackToCreator) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "تعديل والمراجعة",
                        tint = Color.White
                    )
                }

                // Return to HQ button
                Button(
                    onClick = onBackToDashboard,
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
                    text = "تسعير ومراجعة الهاتف النهائي",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Phone Title Badge
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small spec points summary box
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF5CC4FF).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(stats.performance + stats.cameraPower).roundToInt()} نقطة",
                            color = Color(0xFF5CC4FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = phoneName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "هيكل: ${selectedColor.nameAr}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }

            // Specs Breakdown
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
                    Text(
                        text = "عناصر العتاد المحددة",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Divider(color = Color.White.copy(alpha = 0.06f))

                    // Design item Row
                    SpecItemAr(label = "نوع الشاشة والدقة", value = "${selectedDisplay.typeAr} (${selectedDisplay.resolutionAr})", priceText = usdFormatter.format(selectedDisplay.baseCost))

                    // Border style item Row
                    SpecItemAr(label = "شكل الوجهة والحواف", value = selectedDesign.nameAr, priceText = usdFormatter.format(selectedDesign.baseCost))

                    // Processor item Row
                    SpecItemAr(label = "شريحة المعالجة", value = selectedProcessor.nameAr, priceText = usdFormatter.format(selectedProcessor.baseCost))

                    // Camera item Row
                    SpecItemAr(label = "نوع مستشعر الكاميرا", value = selectedCamera.nameAr, priceText = usdFormatter.format(selectedCamera.baseCost))

                    // Lens item Row
                    SpecItemAr(label = "تكنولوجيا العدسات البصرية", value = selectedLens.nameAr, priceText = usdFormatter.format(selectedLens.baseCost))

                    // Battery item Row
                    SpecItemAr(label = "مخزون الطاقة والبطارية", value = selectedBattery.nameAr, priceText = usdFormatter.format(selectedBattery.baseCost))

                    // OS item Row
                    SpecItemAr(label = "نظام التشغيل المثبت", value = selectedOs.nameAr, priceText = usdFormatter.format(selectedOs.baseCost))

                    // Color item Row
                    SpecItemAr(label = "خامة ولون الغشاء", value = selectedColor.nameAr, priceText = usdFormatter.format(selectedColor.colorCost))

                    if (selectedTechs.isNotEmpty()) {
                        Divider(color = Color.White.copy(alpha = 0.06f))
                        Text(
                            text = "التقنيات المتطورة الإضافية: ${selectedTechs.joinToString("، ") { it.nameAr }}",
                            color = Color(0xFFFFB703),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // CORPORATE BUDGET MANAGEMENT PANEL
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "US$ ${usdFormatter.format(progress.funds)}",
                            color = if (progress.funds < totalDirectInvestment) Color(0xFFEF233C) else Color(0xFF4AD66D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CorporateFare,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إدارة ميزانية واستثمارات الجوال",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "خصص الميزانيات المطلوبة للأبحاث، المواد، والتسويق لرفع فاعلية وجودة الجوال في السوق.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = Color.White.copy(alpha = 0.06f))

                    // 1. Research & Development Tier
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تأثير على الكفاءة العامة",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ميزانية الأبحاث والتطوير (R&D)",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val rTiers = listOf(
                                Triple(0, "اقتصادية\n($500)", "-5% أداء"),
                                Triple(1, "قياسية\n($2,500)", "أداء معتمد"),
                                Triple(2, "مختبر غيغا\n($8,000)", "+10% أداء ⚡")
                            )
                            rTiers.forEach { (tier, name, effect) ->
                                val selected = researchTier == tier
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) Color(0xFFFFB703).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selected) Color(0xFFFFB703) else Color.White.copy(alpha = 0.06f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onBudgetChanged(tier, null, null) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (selected) Color(0xFFFFB703) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = effect,
                                            color = if (selected) Color(0xFFFFB703).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                                            fontSize = 9.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Manufacturing Quality Standards
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تأثير على تكلفة وصلاحية القطع",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "جودة ومواصفات خامات التصنيع",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val qTiers = listOf(
                                Triple(0, "تجميع معتدل\n(-20% تكلفة)", "كفاءة -15%"),
                                Triple(1, "خامات معيارية\n(سعر المصنع)", "كفاءة 100%"),
                                Triple(2, "تجميع متفوق بريميوم\n(+25% تكلفة)", "كفاءة +15% ⭐")
                            )
                            qTiers.forEach { (tier, name, effect) ->
                                val selected = manufacturingQuality == tier
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) Color(0xFFFFB703).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selected) Color(0xFFFFB703) else Color.White.copy(alpha = 0.06f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onBudgetChanged(null, tier, null) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (selected) Color(0xFFFFB703) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = effect,
                                            color = if (selected) Color(0xFFFFB703).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                                            fontSize = 9.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Marketing Launch Campaigns
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تأثير على الحصّة السوقية والمعجبين",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "الحملات الترويجية والتدشين",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val mTiers = listOf(
                                Triple(0, "بلا ترويج\n($0)", "طلب محدود (-30%)"),
                                Triple(1, "منصات رقمية\n($1,500)", "جذب رقمي"),
                                Triple(2, "صنّاع ومراجعين\n($6,500)", "+35% مبيعات 📸"),
                                Triple(3, "رعاية عالمية وبث\n($22,000)", "+80% مبيعات ثورية 🚀")
                            )
                            mTiers.forEach { (tier, name, effect) ->
                                val selected = marketingCampaign == tier
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) Color(0xFFFFB703).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selected) Color(0xFFFFB703) else Color.White.copy(alpha = 0.06f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onBudgetChanged(null, null, tier) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (selected) Color(0xFFFFB703) else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = effect,
                                            color = if (selected) Color(0xFFFFB703).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                                            fontSize = 8.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.06f))

                    // Investment Sum Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "US$ ${usdFormatter.format(totalDirectInvestment)}",
                            color = Color(0xFFFFB703),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إجمالي الاستثمار الفوري (أبحاث + تسويق):",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    if (progress.funds < totalDirectInvestment) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF233C).copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, Color(0xFFEF233C).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ رصيدك النقدي الحالي غير كافٍ لتغطية هذه الاستثمارات! يرجى تخفيض ميزانية التدشين أو الأبحاث لتجنب عجز فوري.",
                                color = Color(0xFFEF233C),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(10.dp).fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Cost vs Price display
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "تكلفة التصنيع المباشرة", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text(text = usdFormatter.format(adjustedCostOfGoods), color = Color(0xFFEF233C), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "سعر البيع المقترح لعامة الجمهور", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text(text = usdFormatter.format(proposedPrice), color = Color(0xFFFFB703), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        
                        val ratio = proposedPrice / adjustedCostOfGoods
                        val ratingLabel = when {
                            ratio < 1.1 -> "قيمة استثنائية (تحدي الربح)"
                            ratio < 1.3 -> "قيمة ممتازة ⭐"
                            ratio < 1.6 -> "سعر عادل 👍"
                            ratio < 2.0 -> "هاتف مرتفع الثمن"
                            ratio < 3.0 -> "هاتف فاخر (فلاجشيب)"
                            else -> "سعر مبالغ فيه جداً 💸"
                        }
                        Text(
                            text = ratingLabel,
                            color = when {
                                ratio < 1.6 -> Color(0xFF4AD66D)
                                ratio < 2.5 -> Color(0xFFFFB703)
                                else -> Color(0xFFEF233C)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Dynamic Slider for Pricing selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "حدد السعر النهائي للجمهور",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = "اجتياز التحدي الحالي يلزمه كسب توازن مالي وتسعير يتوافق مع ميزانية الزبائن.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Slider(
                    value = proposedPrice.toFloat(),
                    onValueChange = { onPriceChanged(it.roundToInt().toDouble()) },
                    valueRange = minPriceLimit.toFloat()..maxPriceLimit.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFB703),
                        activeTrackColor = Color(0xFFFFB703),
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("pricing_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = usdFormatter.format(maxPriceLimit), color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    Text(text = usdFormatter.format(minPriceLimit), color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
            }

            // Profit estimation analysis card
            val difference = proposedPrice - adjustedCostOfGoods
            val isLoss = difference < 0
            val profitPercent = if (adjustedCostOfGoods > 0) ((difference / adjustedCostOfGoods) * 100).roundToInt() else 0

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLoss) Color(0xFFEF233C).copy(alpha = 0.1f) else Color(0xFF4AD66D).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLoss) "خسارة للمبيعات!" else "+$profitPercent%",
                        color = if (isLoss) Color(0xFFEF233C) else Color(0xFF4AD66D),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isLoss) {
                            "أنت تبيع بأقل من كلفة التصنيع بـ ${usdFormatter.format(-difference)}"
                        } else {
                            "ربح صافي مباشر قدره ${usdFormatter.format(difference)} لكل جوال مُباع"
                        },
                        color = if (isLoss) Color(0xFFEF233C) else Color(0xFF4AD66D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right
                    )
                }
            }

            // Advisory diagnostics messages depending on levels and price limits
            val demoAlert = getDemographicAlert(challenge, proposedPrice)
            if (demoAlert != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB703).copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB703).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = demoAlert,
                            color = Color(0xFFFFB703),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button trigger
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4AD66D),
                    contentColor = Color(0xFF0F172A)
                ),
                contentPadding = PaddingValues(16.dp),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("launch_simulation_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "طرح الجوال في السوق ومراقبة الأرباح 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SpecItemAr(label: String, value: String, priceText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = priceText, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "$label:", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, textAlign = TextAlign.Right)
        }
    }
}

// Advisory checker
private fun getDemographicAlert(challenge: LevelChallenge, price: Double): String? {
    return when (challenge.idealFocus) {
        "BUDGET" -> {
            if (price > 199.0) "انتبه! زبائن هذه الفئة الاقتصادية غير مستعدين لدفع أكثر من $199 لجوال. قد تنهار المبيعات بشكل عنيف!" else null
        }
        "BALANCED" -> {
            if (price > 399.0) "انتبه! هذه فئة طلاب وشباب بمتوسط ميزانية ضيقة. سعر فوق $399 سيبعدهم نحو عروض سام ميديوم وريد كول." else null
        }
        "CAMERA" -> {
            if (price < 550.0) "تنبيه: السعر رخيص جداً لجهاز فلاجشيب متكامل! ستحقق مبيعات كبرى ولكن بهامش ربح قد يعوق تحقيق هدف الميزانية الطامح." else null
        }
        else -> null
    }
}
