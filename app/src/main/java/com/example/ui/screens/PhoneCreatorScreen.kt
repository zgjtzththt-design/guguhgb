package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.PlayerProgress
import com.example.game.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhoneCreatorScreen(
    progress: PlayerProgress,
    challenge: LevelChallenge,
    selectedColor: PhoneColor,
    selectedProcessor: ProcessorOption,
    selectedCamera: CameraOption,
    selectedLens: LensOption,
    selectedDesign: DesignStyle,
    selectedBattery: BatteryOption,
    selectedDisplay: DisplayOption,
    selectedOs: OsOption,
    selectedTechs: Set<CustomTechnology>,
    selectedCameraPosition: CameraPosition,
    phoneName: String,
    activeNews: com.example.game.NewsItem?,
    onSpecsChangedRaw: (
        color: PhoneColor?,
        processor: ProcessorOption?,
        camera: CameraOption?,
        lens: LensOption?,
        design: DesignStyle?,
        battery: BatteryOption?,
        display: DisplayOption?,
        os: OsOption?,
        techs: Set<CustomTechnology>?,
        cameraPosition: CameraPosition?,
        name: String?
    ) -> Unit,
    onSubmitSpecs: () -> Unit,
    onCancel: () -> Unit
) {
    val onSpecsChanged = { color: PhoneColor?, processor: ProcessorOption?, camera: CameraOption?, lens: LensOption?, design: DesignStyle?, battery: BatteryOption?, display: DisplayOption?, os: OsOption?, techs: Set<CustomTechnology>?, name: String? ->
        if (techs == null) {
            GameSoundPlayer.playInteractSound()
        }
        onSpecsChangedRaw(color, processor, camera, lens, design, battery, display, os, techs, null, name)
    }

    val usdFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    // Wizard step state: 1: Design, 2: Screen, 3: Processor, 4: Battery, 5: Camera, 6: Extra Techs
    var currentStep by rememberSaveable { mutableStateOf(1) }
    var selectedBodyFrame by rememberSaveable { mutableStateOf("samsung_s25_ultra") }
    val context = LocalContext.current

    // Calculate real-time manufacturing cost
    val lensMult = if (activeNews?.affectedPartId == "lens") activeNews.priceMultiplier else 1.0
    val cpuMult = if (activeNews?.affectedPartId == "cpu") activeNews.priceMultiplier else 1.0
    val batteryMult = if (activeNews?.affectedPartId == "battery") activeNews.priceMultiplier else 1.0
    val caseMult = if (activeNews?.affectedPartId == "case") activeNews.priceMultiplier else 1.0

    val currentCost = (selectedProcessor.baseCost * cpuMult) + selectedCamera.baseCost + (selectedLens.baseCost * lensMult) + 
                      selectedDesign.baseCost + selectedColor.colorCost + (selectedBattery.baseCost * batteryMult) + 
                      selectedDisplay.baseCost + selectedTechs.sumOf { it.addedCost } + (30.0 * caseMult)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
            // ==========================================
            // LEFT PANEL: STEP-BY-STEP CUSTOMIZER WIZARD
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
            ) {
                // Production Line Title with Return to HQ button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCancel,
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
                        text = "خط الإنتاج والتطوير الذكي",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }

                // Balance and Level Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المستوى: ${progress.currentLevel}",
                        color = Color(0xFFFFB703),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الميزانية: ${usdFormatter.format(progress.funds)}",
                        color = Color(0xFF4AD66D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (activeNews != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeNews.isGoodNews) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF451A03).copy(alpha = 0.5f))
                            .border(1.dp, if (activeNews.isGoodNews) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFF59E0B).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeNews.titleAr,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Podcasts, contentDescription = null, tint = if (activeNews.isGoodNews) Color(0xFF34D399) else Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                            }
                            Text(
                                text = "تنبيه: ${activeNews.impactDescAr}",
                                color = if (activeNews.isGoodNews) Color(0xFF34D399) else Color(0xFFF87171),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Top Tab row indicating the progress stages
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepsList = listOf(
                        1 to "التصميم",
                        2 to "الشاشة",
                        3 to "المعالج",
                        4 to "البطارية",
                        5 to "الكاميرا",
                        6 to "العدسات",
                        7 to "النظام",
                        8 to "الميزات"
                    )

                    stepsList.forEach { (stepNum, stepTitle) ->
                        val isCurrent = stepNum == currentStep
                        val isPassed = stepNum < currentStep

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isCurrent -> Color(0xFFFFB703)
                                        isPassed -> Color(0xFF4AD66D).copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { currentStep = stepNum }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isPassed) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4AD66D),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text(
                                    text = stepTitle,
                                    color = when {
                                        isCurrent -> Color(0xFF0F172A)
                                        isPassed -> Color(0xFF4AD66D)
                                        else -> Color.White.copy(alpha = 0.5f)
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable container for current step options
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Step Content
                    when (currentStep) {
                        1 -> {
                            // DESIGN STEP
                            DesignSparePart(selectedColor)

                            Spacer(modifier = Modifier.height(2.dp))

                            // Name field
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "اسم الهاتف المبتكر",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = phoneName,
                                    onValueChange = { onSpecsChanged(null, null, null, null, null, null, null, null, null, it) },
                                    modifier = Modifier.fillMaxWidth().testTag("phone_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFB703),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, fontSize = 13.sp),
                                    placeholder = {
                                        Text(
                                            text = "أدخل اسم الهاتف هنا...",
                                            color = Color.White.copy(alpha = 0.3f),
                                            fontSize = 13.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Colors Selection
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "اختر لون وهيكل الغلاف الخارجي",
                                    color = Color(0xFFFFB703),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    GameDefaults.Colors.forEach { color ->
                                        val isSelected = color.id == selectedColor.id
                                        val boxColor = Color(android.graphics.Color.parseColor(color.hexString))

                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(boxColor)
                                                .border(
                                                    BorderStroke(
                                                        if (isSelected) 2.5.dp else 1.dp,
                                                        if (isSelected) Color(0xFFFFB703) else Color.White.copy(alpha = 0.2f)
                                                    ),
                                                    CircleShape
                                                )
                                                .clickable {
                                                    GameSoundPlayer.playInteractSound()
                                                    onSpecsChanged(color, null, null, null, null, null, null, null, null, null)
                                                }
                                                .testTag("color_option_${color.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (color.hexString == "#F0EAD6" || color.hexString == "#FFB7C5" || color.hexString == "#FFD700") Color.Black else Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "اللون المحدد: ${selectedColor.nameAr} (+${usdFormatter.format(selectedColor.colorCost)})",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Body Frame Struct Design Decisions
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "اختر هيكل وهندسة جسم الهاتف الخارجي",
                                    color = Color(0xFFFFB703),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val bodyFrames = listOf(
                                    Triple("samsung_s25_ultra", "Samsung S25 Ultra", "إطار تيتانيوم مصقول، زوايا حادة قوية للأعمال والإنتاجية."),
                                    Triple("samsung_s25_standard", "Samsung S25 Standard", "تصميم متوازن بزوايا مستديرة مريحة لليد مع إطار ألمنيوم خفيف."),
                                    Triple("iphone_17_pro", "iPhone 17 Pro", "زوايا انسيابية مستديرة للغاية، ملمس ناعم كالحرير بحواف متناسقة."),
                                    Triple("iphone_17_standard", "iPhone 17 Standard", "الأناقة الزجاجية المعتادة مع زوايا مريحة وهيكل نحيف للغاية."),
                                    Triple("nothing_phone_3", "Nothing Phone (3)", "تصميم شفاف أيقوني مع واجهة Glyph الضوئية وهيكل صناعي أنيق."),
                                    Triple("rog_phone_9", "ROG Phone 9 Ultimate", "تصميم هجومي للألعاب مع زوايا حادة مائلة وشاشة AniMe Matrix خلفية."),
                                    Triple("google_pixel", "هاتِف Google Pixel Pro", "النمط العضوي الدافئ مع شريط مستشعر خلفي أفقي ممتد ومميز جداً."),
                                    Triple("xiaomi_15_ultra", "Xiaomi 15 Ultra", "هيكل سيراميكي متين مزود بحلقة كاميرا دائرية أيقونية عملاقة بالأعلى.")
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    bodyFrames.forEach { (id, name, desc) ->
                                        val isSelectedBody = selectedBodyFrame == id
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelectedBody) Color(0xFFFFB703).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                                            ),
                                            border = BorderStroke(
                                                if (isSelectedBody) 1.2.dp else 1.dp,
                                                if (isSelectedBody) Color(0xFFFFB703) else Color.White.copy(alpha = 0.05f)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedBodyFrame = id }
                                                .testTag("body_frame_$id")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                                ) {
                                                    Text(
                                                        text = name,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Right
                                                    )
                                                    Text(
                                                        text = desc,
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        fontSize = 10.sp,
                                                        textAlign = TextAlign.Right
                                                    )
                                                }
                                                RadioButton(
                                                    selected = isSelectedBody,
                                                    onClick = { selectedBodyFrame = id },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = Color(0xFFFFB703),
                                                        unselectedColor = Color.White.copy(alpha = 0.3f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // DISPLAY/SCREEN STEP
                            ScreenSparePart(selectedDesign)

                            Text(
                                text = "تحديد مهارات الشاشة والدقة",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            GameDefaults.Displays.forEach { disp ->
                                val isSelected = disp.id == selectedDisplay.id
                                val isLocked = disp.requiredLevel > progress.currentLevel
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isLocked -> Color.White.copy(alpha = 0.01f)
                                            isSelected -> Color(0xFFFFB703).copy(alpha = 0.12f)
                                            else -> Color.White.copy(alpha = 0.02f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 1.2.dp else 1.dp,
                                        when {
                                            isLocked -> Color.White.copy(alpha = 0.05f)
                                            isSelected -> Color(0xFFFFB703)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isLocked) {
                                            onSpecsChanged(null, null, null, null, null, null, disp, null, null, null)
                                        }
                                        .testTag("display_option_${disp.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "+${usdFormatter.format(disp.baseCost)}",
                                                color = Color(0xFF4AD66D),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                            Text(
                                                text = if (isLocked) "؟؟؟ (مستوى ${disp.requiredLevel})" else "${disp.typeAr} (${disp.resolutionAr})",
                                                color = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Right
                                            )
                                            
                                            if (!isLocked) {
                                                // Display Quality Bar
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "الجودة: ${disp.displayScore.toInt()}/100",
                                                        color = Color(0xFF4AD66D),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .width(60.dp)
                                                            .height(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White.copy(alpha = 0.1f))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth( (disp.displayScore / 100f).toFloat() )
                                                                .fillMaxHeight()
                                                                .background(
                                                                    Brush.horizontalGradient(
                                                                        listOf(Color(0xFF4AD66D), Color(0xFF06D6A0))
                                                                    )
                                                                )
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = if (isLocked) "ارتقِ بمستوى شركتك لفتح هذا المكون المتطور." else disp.techDetailAr,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        if (!isLocked) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSpecsChanged(null, null, null, null, null, null, disp, null, null, null) },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB703), unselectedColor = Color.White.copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "تحديد تكنولوجيا الحواف والوجهة العلوية",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            GameDefaults.DesignStyles.forEach { style ->
                                val isSelected = style.id == selectedDesign.id
                                val isLocked = style.requiredLevel > progress.currentLevel
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isLocked -> Color.White.copy(alpha = 0.01f)
                                            isSelected -> Color(0xFFFFB703).copy(alpha = 0.12f)
                                            else -> Color.White.copy(alpha = 0.02f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 1.2.dp else 1.dp,
                                        when {
                                            isLocked -> Color.White.copy(alpha = 0.05f)
                                            isSelected -> Color(0xFFFFB703)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isLocked) {
                                            onSpecsChanged(null, null, null, null, style, null, null, null, null, null)
                                        }
                                        .testTag("design_option_${style.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "+${usdFormatter.format(style.baseCost)}",
                                                color = Color(0xFF4AD66D),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                            Text(
                                                text = if (isLocked) "؟؟؟ (مستوى ${style.requiredLevel})" else style.nameAr,
                                                color = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Right
                                            )
                                            Text(
                                                text = if (isLocked) "يفتح هذا التصميم المستقبلي عند الوصول للمستوى المطلوب." else style.techDetailAr,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        if (!isLocked) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSpecsChanged(null, null, null, null, style, null, null, null, null, null) },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB703), unselectedColor = Color.White.copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // PROCESSOR STEP
                            ProcessorSparePart(selectedProcessor)

                            Text(
                                text = "تحديد فئة المعالج والسرعة الحسابية",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            GameDefaults.Processors.forEach { cpu ->
                                val isSelected = cpu.id == selectedProcessor.id
                                val isLocked = cpu.requiredLevel > progress.currentLevel
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isLocked -> Color.White.copy(alpha = 0.01f)
                                            isSelected -> Color(0xFFFFB703).copy(alpha = 0.12f)
                                            else -> Color.White.copy(alpha = 0.02f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 1.2.dp else 1.dp,
                                        when {
                                            isLocked -> Color.White.copy(alpha = 0.05f)
                                            isSelected -> Color(0xFFFFB703)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isLocked) {
                                            onSpecsChanged(null, cpu, null, null, null, null, null, null, null, null)
                                        }
                                        .testTag("processor_option_${cpu.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "+${usdFormatter.format(cpu.baseCost)}",
                                                color = Color(0xFF4AD66D),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                            Text(
                                                text = if (isLocked) "؟؟؟ (مستوى ${cpu.requiredLevel})" else cpu.nameAr,
                                                color = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Right
                                            )
                                            
                                            if (!isLocked) {
                                                // Performance Power Bar
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "القوة: ${cpu.performanceScore.toInt()}/100",
                                                        color = Color(0xFFFFB703),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .width(60.dp)
                                                            .height(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White.copy(alpha = 0.1f))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth( (cpu.performanceScore / 100f).toFloat() )
                                                                .fillMaxHeight()
                                                                .background(
                                                                    Brush.horizontalGradient(
                                                                        listOf(Color(0xFFFFB703), Color(0xFFFB8500))
                                                                    )
                                                                )
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = if (isLocked) "هذا المعالج الجبار متاح لشركات المستوى المتقدم فقط." else cpu.techDetailAr,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        if (!isLocked) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSpecsChanged(null, cpu, null, null, null, null, null, null, null, null) },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB703), unselectedColor = Color.White.copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // BATTERY STEP (NEW DYNAMIC SPECS!)
                            BatterySparePart(selectedBattery)

                            Text(
                                text = "تحديد خلية تخرين الطاقة والبطارية",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            GameDefaults.Batteries.forEach { rawBattery ->
                                val isSelected = rawBattery.id == selectedBattery.id
                                val isLocked = rawBattery.requiredLevel > progress.currentLevel
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isLocked -> Color.White.copy(alpha = 0.01f)
                                            isSelected -> Color(0xFFFFB703).copy(alpha = 0.12f)
                                            else -> Color.White.copy(alpha = 0.02f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 1.2.dp else 1.dp,
                                        when {
                                            isLocked -> Color.White.copy(alpha = 0.05f)
                                            isSelected -> Color(0xFFFFB703)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isLocked) {
                                            onSpecsChanged(null, null, null, null, null, rawBattery, null, null, null, null)
                                        }
                                        .testTag("battery_option_${rawBattery.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "+${usdFormatter.format(rawBattery.baseCost)}",
                                                color = Color(0xFF4AD66D),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                            Text(
                                                text = if (isLocked) "؟؟؟ (مستوى ${rawBattery.requiredLevel})" else "${rawBattery.nameAr} (${rawBattery.capacityMah}mAh)",
                                                color = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Right
                                            )

                                            if (!isLocked) {
                                                // Battery Efficiency Bar
                                                val batteryScore = when(rawBattery.id) {
                                                    "lithium_basic" -> 25f
                                                    "silicon_balanced" -> 50f
                                                    "graphene_pro" -> 75f
                                                    "nuclear_quantum" -> 98f
                                                    else -> 30f
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "الكفاءة: ${batteryScore.toInt()}%",
                                                        color = Color(0xFF4AD66D),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .width(60.dp)
                                                            .height(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White.copy(alpha = 0.1f))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth(batteryScore / 100f)
                                                                .fillMaxHeight()
                                                                .background(
                                                                    Brush.horizontalGradient(
                                                                        listOf(Color(0xFF4AD66D), Color(0xFF06D6A0))
                                                                    )
                                                                )
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = if (isLocked) "خوارزميات إدارة الطاقة المتقدمة لم تكتشف بعد." else rawBattery.descAr,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        if (!isLocked) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSpecsChanged(null, null, null, null, null, rawBattery, null, null, null, null) },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB703), unselectedColor = Color.White.copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        5 -> {
                            // CAMERA STEP
                            CameraSparePart(selectedCamera)

                            Text(
                                text = "تثبيت مستشعرات الكاميرا والعدسة البصرية",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            GameDefaults.Cameras.forEach { cam ->
                                val isSelected = cam.id == selectedCamera.id
                                val isLocked = cam.requiredLevel > progress.currentLevel
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isLocked -> Color.White.copy(alpha = 0.01f)
                                            isSelected -> Color(0xFFFFB703).copy(alpha = 0.12f)
                                            else -> Color.White.copy(alpha = 0.02f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 1.2.dp else 1.dp,
                                        when {
                                            isLocked -> Color.White.copy(alpha = 0.05f)
                                            isSelected -> Color(0xFFFFB703)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isLocked) {
                                            onSpecsChangedRaw(null, null, cam, null, null, null, null, null, null, null, null)
                                        }
                                        .testTag("camera_option_${cam.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "+${usdFormatter.format(cam.baseCost)}",
                                                color = Color(0xFF4AD66D),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                            Text(
                                                text = if (isLocked) "؟؟؟ (مستوى ${cam.requiredLevel})" else cam.nameAr,
                                                color = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Right
                                            )

                                            if (!isLocked) {
                                                // Camera Power Bar
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "القوة: ${cam.cameraScore.toInt()}/100",
                                                        color = Color(0xFFE11D48),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .width(60.dp)
                                                            .height(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White.copy(alpha = 0.1f))
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth((cam.cameraScore / 100f).toFloat())
                                                                .fillMaxHeight()
                                                                .background(
                                                                    Brush.horizontalGradient(
                                                                        listOf(Color(0xFFE11D48), Color(0xFFFB7185))
                                                                    )
                                                                )
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = if (isLocked) "هذه التكنولوجيا البصرية متاحة فقط للمصورين المحترفين." else cam.megapixelsTextAr,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        if (!isLocked) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSpecsChangedRaw(null, null, cam, null, null, null, null, null, null, null, null) },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB703), unselectedColor = Color.White.copy(alpha = 0.3f))
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "موضع الكاميرا الخلفية",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CameraPosition.entries.forEach { pos ->
                                    val isSelected = pos == selectedCameraPosition
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFFFB703).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                        ),
                                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFFFFB703) else Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).clickable {
                                            onSpecsChangedRaw(null, null, null, null, null, null, null, null, null, pos, null)
                                        }
                                    ) {
                                        Box(modifier = Modifier.width(100.dp).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                            Text(text = pos.nameAr, color = if (isSelected) Color(0xFFFFB703) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        6 -> {
                            // OPTICAL LENS STEP (NEW!)
                            Text(
                                text = "اختيار تكنولوجيا العدسات الاحترافية",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            GameDefaults.LensOptions.forEach { lensOpt ->
                                val isSelected = lensOpt.id == selectedLens.id
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFFFFB703).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f)
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 1.2.dp else 1.dp,
                                        if (isSelected) Color(0xFFFFB703) else Color.White.copy(alpha = 0.05f)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSpecsChangedRaw(null, null, null, lensOpt, null, null, null, null, null, null, null)
                                        }
                                        .testTag("lens_option_${lensOpt.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "+${usdFormatter.format(lensOpt.baseCost)}",
                                            color = Color(0xFF4AD66D),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = lensOpt.nameAr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = lensOpt.iconEmoji, fontSize = 14.sp)
                                            }

                                            // Lens Quality Bar
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                                            ) {
                                                Text(
                                                    text = "الجودة: ${lensOpt.qualityScore.toInt()}/100",
                                                    color = Color(0xFF5CC4FF),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(50.dp)
                                                        .height(3.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.1f))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth((lensOpt.qualityScore / 100f).toFloat())
                                                            .fillMaxHeight()
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(Color(0xFF5CC4FF), Color(0xFF00B4D8))
                                                                )
                                                            )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = lensOpt.descAr,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onSpecsChangedRaw(null, null, null, lensOpt, null, null, null, null, null, null, null) },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB703), unselectedColor = Color.White.copy(alpha = 0.3f))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        7 -> {
                            // OPERATING SYSTEM STEP
                            Text(
                                text = "تثبيت وتخصيص نظام التشغيل",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            GameDefaults.OperatingSystems.forEach { os ->
                                val isSelected = os.id == selectedOs.id
                                val isLocked = os.requiredLevel > progress.currentLevel
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isLocked -> Color.White.copy(alpha = 0.01f)
                                            isSelected -> Color(0xFFFFB703).copy(alpha = 0.12f)
                                            else -> Color.White.copy(alpha = 0.02f)
                                        }
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 1.2.dp else 1.dp,
                                        when {
                                            isLocked -> Color.White.copy(alpha = 0.05f)
                                            isSelected -> Color(0xFFFFB703)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isLocked) {
                                            onSpecsChanged(null, null, null, null, null, null, null, os, null, null)
                                        }
                                        .testTag("os_option_${os.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // LOGO SECTION
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isLocked) Color.White.copy(alpha = 0.05f) else Color(android.graphics.Color.parseColor(os.themeColor)).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isLocked) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
                                            } else {
                                                val logoResId = context.resources.getIdentifier(os.logoName, "drawable", context.packageName)
                                                if (logoResId != 0) {
                                                    Image(
                                                        painter = painterResource(id = logoResId),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                } else {
                                                    // Fallback icon based on OS type
                                                    Icon(
                                                        imageVector = when(os.id) {
                                                            "os_basic" -> Icons.Default.Android
                                                            "os_fruit" -> Icons.Default.PhoneIphone
                                                            "os_custom" -> Icons.Default.AutoFixHigh
                                                            else -> Icons.Default.Settings
                                                        },
                                                        contentDescription = null,
                                                        tint = Color(android.graphics.Color.parseColor(os.themeColor)),
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isLocked) "؟؟؟ (مستوى ${os.requiredLevel})" else os.nameAr,
                                                color = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Text(
                                                text = if (isLocked) "رخص استخدام هذا النظام متاحة فقط للشركات الكبرى." else os.shortDescAr,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (!isLocked) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "+${usdFormatter.format(os.baseCost)}",
                                                    color = Color(0xFF4AD66D),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { onSpecsChanged(null, null, null, null, null, null, null, os, null, null) },
                                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFB703), unselectedColor = Color.White.copy(alpha = 0.3f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        8 -> {
                            // ADDITIONAL TECHS STEP
                            ExtraTechSparePart()

                            Text(
                                text = "دعم التقنيات المبتكرة لمضاعفة الجلب",
                                color = Color(0xFFFFB703),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GameDefaults.Technologies.forEach { tech ->
                                    val isChecked = selectedTechs.any { it.id == tech.id }
                                    val isLocked = tech.requiredLevel > progress.currentLevel
                                    
                                    FilterChip(
                                        selected = isChecked,
                                        onClick = {
                                            if (isLocked) return@FilterChip
                                            val nextTechs = if (isChecked) {
                                                GameSoundPlayer.playInteractSound()
                                                selectedTechs.filterNot { it.id == tech.id }.toSet()
                                            } else {
                                                GameSoundPlayer.playUpgradeSuccessSound()
                                                selectedTechs + tech
                                            }
                                            onSpecsChanged(null, null, null, null, null, null, null, null, nextTechs, null)
                                        },
                                        label = { 
                                            Text(
                                                text = if (isLocked) "🔒 مستوى ${tech.requiredLevel}" else tech.nameAr, 
                                                fontSize = 10.sp, 
                                                fontWeight = FontWeight.Bold
                                            ) 
                                        },
                                        leadingIcon = if (isChecked && !isLocked) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(10.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFFB703).copy(alpha = 0.25f),
                                            selectedLabelColor = Color(0xFFFFB703),
                                            selectedLeadingIconColor = Color(0xFFFFB703),
                                            containerColor = if (isLocked) Color.White.copy(alpha = 0.01f) else Color.White.copy(alpha = 0.03f),
                                            labelColor = if (isLocked) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.7f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("tech_badge_${tech.id}"),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (isLocked) Color.Transparent else Color.White.copy(alpha = 0.1f),
                                            selectedBorderColor = Color(0xFFFFB703),
                                            enabled = !isLocked,
                                            selected = isChecked
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action button Row for left customizer pane
                val totalCost = selectedProcessor.baseCost + selectedCamera.baseCost + selectedDesign.baseCost + 
                                selectedColor.colorCost + selectedBattery.baseCost + selectedDisplay.baseCost + 
                                selectedTechs.sumOf { it.addedCost }
                val devCost = totalCost * 500.0
                val canAfford = progress.funds >= devCost

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "تكلفة النموذج", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                        Text(
                            text = usdFormatter.format(devCost), 
                            color = if (canAfford) Color(0xFF4AD66D) else Color(0xFFEF4444), 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Previous Step Button
                        if (currentStep > 1) {
                            Button(
                                onClick = { currentStep -= 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "السابق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Next Step Button
                        if (currentStep < 8) {
                            Button(
                                onClick = { currentStep += 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFB703),
                                    contentColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "الخطوة التالية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            // Finish & Submit button!
                            Button(
                                onClick = {
                                    if (phoneName.isNotBlank() && canAfford) {
                                        onSubmitSpecs()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4AD66D),
                                    contentColor = Color(0xFF020617),
                                    disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                                ),
                                enabled = phoneName.isNotBlank() && canAfford,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("pricing_stage_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "طرح وسعر البيع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // VERTICAL SEPARATOR CHASSIS GLOW LINE
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )

            // ==========================================
            // RIGHT PANEL: THE STATIC/DYNAMIC REALTIME PHONE PREVIEW & BOTTOM DETAILS
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand name & Capital funds header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "رأس المال الحالي", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        Text(text = usdFormatter.format(progress.funds), color = Color(0xFF4AD66D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "المستوى ${challenge.levelNumber}", color = Color(0xFFFFB703), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = progress.brandName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // The Phone Physical Mockup Space
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val availableWidth = maxWidth
                        val availableHeight = maxHeight
                        
                        val isFoldable = selectedDesign.id == "futuristic_fold"
                        val showFrontScreen = currentStep == 2 || currentStep == 7
                        
                        // Dynamically scale mockup size to ensure it never overflows parents in landscape
                        val maxPhoneHeight = minOf(230f, (availableHeight * 0.92f).value).dp
                        val maxPhoneWidth = if (isFoldable) maxPhoneHeight * 0.72f else maxPhoneHeight * 0.52f
                        
                        // Hologram base matching style color
                        val brushColor = Color(android.graphics.Color.parseColor(selectedColor.hexString))
                        Box(
                            modifier = Modifier
                                .size(maxPhoneHeight * 0.88f)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(brushColor.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )
                        )

                        // Determine Phone Shape and Corner Radius based on Body Frame selected
                        val phoneShape = remember(selectedBodyFrame, isFoldable) {
                            if (isFoldable) {
                                RoundedCornerShape(8.dp)
                            } else {
                                when (selectedBodyFrame) {
                                    "samsung_s25_ultra" -> RoundedCornerShape(4.dp) // Very sharp!
                                    "samsung_s25_standard" -> RoundedCornerShape(16.dp) // Balanced
                                    "iphone_17_pro", "iphone_17_standard" -> RoundedCornerShape(26.dp) // Highly rounded!
                                    "nothing_phone_3" -> RoundedCornerShape(24.dp) // Industrial roundness
                                    "rog_phone_9" -> RoundedCornerShape(8.dp) // Aggressive cut
                                    "google_pixel" -> RoundedCornerShape(18.dp)    // Organic roundness!
                                    "xiaomi_15_ultra" -> RoundedCornerShape(14.dp)  // Premium curves!
                                    else -> RoundedCornerShape(16.dp)
                                }
                            }
                        }

                        // Outer Physical Frame Container
                        Box(
                            modifier = Modifier
                                .width(maxPhoneWidth)
                                .height(maxPhoneHeight)
                                .clip(phoneShape)
                                .background(brushColor)
                                .border(2.dp, Color.White.copy(alpha = 0.35f), phoneShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showFrontScreen) {
                                // ==========================================
                                // FRONT SCREEN DISPLAY VIEW (STEP 2: SCREEN)
                                // ==========================================
                                val bezelThickness = 3.dp
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bezelThickness)
                                        .clip(phoneShape)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = when (selectedColor.id) {
                                                    "titanium_gold" -> listOf(Color(0xFF2E1C05), Color(0xFFD4AF37).copy(alpha = 0.8f), Color(0xFF110B02))
                                                    "stealth_black" -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF020617))
                                                    "aurora_blue" -> listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6), Color(0xFF0F172A))
                                                    "emerald_green" -> listOf(Color(0xFF064E3B), Color(0xFF10B981), Color(0xFF022C22))
                                                    "sakura_pink" -> listOf(Color(0xFF831843), Color(0xFFF472B6), Color(0xFF500724))
                                                    "pearl_white" -> listOf(Color(0xFF475569), Color(0xFFCBD5E1), Color(0xFF1E293B))
                                                    else -> listOf(Color(0xFF1E1B4B), brushColor.copy(alpha = 0.7f), Color(0xFF020617))
                                                }
                                            )
                                        ),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    // SHOW OS LOGO INSTEAD OF WALLPAPER IMAGE
                                    val logoResId = context.resources.getIdentifier(selectedOs.logoName, "drawable", context.packageName)
                                    if (logoResId != 0) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = logoResId),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .alpha(0.8f),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }

                                    // Elegant abstract grid or shine overlay inside screen
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                                                )
                                            )
                                    )

                                    // Display Details (Time, Signal, Battery, etc)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 18.dp, bottom = 12.dp, start = 10.dp, end = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Header Row: Status & Info Indicators
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Left: WiFi & Signal Symbols
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("5G", color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            // Right: Battery Icon
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("100%", color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp)
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Icon(Icons.Default.BatteryFull, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(11.dp))
                                            }
                                        }

                                        // Lock Screen Widget (Time and Date)
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 12.dp)) {
                                            Text(
                                                text = "١٢:٠٠", // 12:00 in Arabic
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Light,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "الأحد، ٧ يونيو", // Sunday, 7 June
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        // Virtual Fingerprint scanner icon if under displays technology
                                        if (selectedDesign.id == "under_display") {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "بصمة",
                                                tint = Color.Cyan.copy(alpha = 0.6f),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "مستشعر مدمج خفي",
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 8.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        } else {
                                            // Standard greeting
                                            Text(
                                                text = phoneName.ifBlank { "هاتفك المبتكر" },
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // Render selected Notch at the top of the display screen
                                    when (selectedDesign.id) {
                                        "notch_circular" -> {
                                            // Punch Hole upper center
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 6.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black)
                                            )
                                        }
                                        "notch_large" -> {
                                            // Trapezoid classic notch
                                            Box(
                                                modifier = Modifier
                                                    .width(34.dp)
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                                                    .background(Color.Black),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(modifier = Modifier.width(12.dp).height(1.dp).background(Color.DarkGray))
                                            }
                                        }
                                        "notch_iphone" -> {
                                            // Pill-shape "Dynamic Island"
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 6.dp)
                                                    .width(28.dp)
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.Black)
                                                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            )
                                        }
                                        "notch_side" -> {
                                            // Punch hole top-right (starts top on right side)
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(top = 6.dp, end = 12.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black)
                                            )
                                        }
                                        "under_display" -> {
                                            // Invisible: very faint optical aura
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 6.dp)
                                                    .size(5.dp)
                                                    .border(0.4.dp, Color.Cyan.copy(alpha = 0.3f), CircleShape)
                                            )
                                        }
                                        "futuristic_fold" -> {
                                            // Hinge Fold crease line down center
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(1.dp)
                                                    .background(Color.Black.copy(alpha = 0.4f))
                                            )
                                        }
                                    }
                                }
                            } else {
                                // ==========================================
                                // REAR PANEL BACKPLATE VIEW (ALL OTHER STEPS)
                                // ==========================================
                                // Glass shine reflection overlay effect
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.22f),
                                                    Color.White.copy(alpha = 0.06f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                // Custom camera styles on back based on selected Body Frame
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    val lensColor = Color(0xFF000000)
                                    val reflectionColor = Color(0xFF5CC4FF)
                                    
                                    val alignment = when (selectedCameraPosition) {
                                        CameraPosition.LEFT -> Alignment.TopStart
                                        CameraPosition.CENTER -> Alignment.TopCenter
                                        CameraPosition.RIGHT -> Alignment.TopEnd
                                    }

                                    when (selectedBodyFrame) {
                                        "samsung_s25_ultra", "samsung_s25_standard" -> {
                                            // Vertical Stack of individual lens protrusions
                                            Column(
                                                modifier = Modifier
                                                    .align(alignment)
                                                    .padding(top = 6.dp, start = if (selectedCameraPosition == CameraPosition.LEFT) 6.dp else 0.dp, end = if (selectedCameraPosition == CameraPosition.RIGHT) 6.dp else 0.dp),
                                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val count = when (selectedCamera.id) {
                                                    "single_basic" -> 1
                                                    "dual_advanced" -> 2
                                                    "triple_pro" -> 3
                                                    "quantum_lens" -> if (selectedBodyFrame == "samsung_s25_ultra") 4 else 3
                                                    else -> 3
                                                }
                                                repeat(count) { i ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(13.dp)
                                                            .clip(CircleShape)
                                                            .background(lensColor)
                                                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(reflectionColor))
                                                    }
                                                }
                                                // Tiny flash
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                )
                                            }
                                        }

                                        "iphone_17_pro" -> {
                                            // Pro Stove-pot Rounded Triangular Camera Plate
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(top = 2.dp, start = 2.dp)
                                                    .size(if (selectedCamera.id == "single_basic") 24.dp else 42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.White.copy(alpha = 0.12f))
                                                    .border(0.8.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val lenses = if (selectedCamera.id == "single_basic") 1 else if (selectedCamera.id == "dual_advanced") 2 else 3
                                                if (lenses == 1) {
                                                    Box(
                                                        modifier = Modifier.size(13.dp).clip(CircleShape).background(lensColor).border(0.8.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(reflectionColor))
                                                    }
                                                } else {
                                                    // Layout multi-lenses
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        Box(
                                                            modifier = Modifier.padding(3.dp).size(13.dp).clip(CircleShape).background(lensColor).align(Alignment.TopStart),
                                                            contentAlignment = Alignment.Center
                                                        ) { Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(reflectionColor)) }
                                                        
                                                        Box(
                                                            modifier = Modifier.padding(3.dp).size(13.dp).clip(CircleShape).background(lensColor).align(Alignment.BottomCenter),
                                                            contentAlignment = Alignment.Center
                                                        ) { Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(reflectionColor)) }
                                                        
                                                        if (lenses == 3) {
                                                            Box(
                                                                 modifier = Modifier.padding(3.dp).size(13.dp).clip(CircleShape).background(lensColor).align(Alignment.TopEnd),
                                                                contentAlignment = Alignment.Center
                                                            ) { Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(reflectionColor)) }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        "iphone_17_standard" -> {
                                            // Classic Dual Diagonal Camera Layout
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(top = 4.dp, start = 4.dp)
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.White.copy(alpha = 0.1f))
                                                    .border(0.8.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    // Lens 1 (Top Right)
                                                    Box(
                                                        modifier = Modifier.padding(4.dp).size(12.dp).clip(CircleShape).background(lensColor).align(Alignment.TopEnd),
                                                        contentAlignment = Alignment.Center
                                                    ) { Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(reflectionColor)) }

                                                    // Lens 2 (Bottom Left)
                                                    Box(
                                                        modifier = Modifier.padding(4.dp).size(12.dp).clip(CircleShape).background(lensColor).align(Alignment.BottomStart),
                                                        contentAlignment = Alignment.Center
                                                    ) { Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(reflectionColor)) }

                                                    // Flash (Top Left)
                                                    Box(
                                                        modifier = Modifier.padding(4.dp).size(4.dp).clip(CircleShape).background(Color.White).align(Alignment.TopStart)
                                                    )
                                                }
                                            }
                                        }

                                        "nothing_phone_3" -> {
                                            // Nothing Style: Dual Vertical Lenses + Glyph Accents
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(top = 8.dp, start = 8.dp)
                                                    .width(22.dp)
                                                    .fillMaxHeight(0.35f)
                                                    .clip(RoundedCornerShape(11.dp))
                                                    .background(Color.White.copy(alpha = 0.05f))
                                                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(11.dp))
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.SpaceEvenly,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    repeat(2) {
                                                        Box(
                                                            modifier = Modifier.size(14.dp).clip(CircleShape).background(lensColor),
                                                            contentAlignment = Alignment.Center
                                                        ) { Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(reflectionColor)) }
                                                    }
                                                }
                                            }
                                        }

                                        "rog_phone_9" -> {
                                            // ROG Gaming: Hexagonal/Aggressive Camera Island
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(top = 6.dp, start = 2.dp)
                                                    .size(width = 44.dp, height = 30.dp)
                                                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 8.dp, bottomEnd = 0.dp, bottomStart = 8.dp))
                                                    .background(Color.Black.copy(alpha = 0.4f))
                                                    .border(1.dp, Color(0xFFFF2B51).copy(alpha = 0.3f), RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    repeat(3) {
                                                        Box(
                                                            modifier = Modifier.size(10.dp).clip(CircleShape).background(lensColor),
                                                            contentAlignment = Alignment.Center
                                                        ) { Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(reflectionColor)) }
                                                    }
                                                }
                                            }
                                        }

                                        "google_pixel" -> {
                                            // Iconic horizontal Visor shield across top
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 10.dp)
                                                    .height(20.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.Black.copy(alpha = 0.82f))
                                                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val lenses = if (selectedCamera.id == "single_basic") 1 else if (selectedCamera.id == "dual_advanced") 2 else 3
                                                    repeat(lenses) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(11.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFF090D16)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(reflectionColor))
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        "xiaomi_15_ultra" -> {
                                            // Giant centered circular studio dome plate
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .padding(top = 6.dp)
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF111827))
                                                    .border(2.dp, Color(0xFFFFB703).copy(alpha = 0.4f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Camera sensors layout inside circle
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    // Main giant lens
                                                    Box(
                                                        modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.Black).border(0.6.dp, Color.White.copy(alpha = 0.3f), CircleShape).align(Alignment.Center),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(reflectionColor))
                                                    }
                                                    
                                                    // Extra small lenses orbiting
                                                    val lensesCount = if (selectedCamera.id == "single_basic") 0 else if (selectedCamera.id == "dual_advanced") 1 else 3
                                                    if (lensesCount >= 1) {
                                                        Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(Color.Black).align(Alignment.TopCenter))
                                                    }
                                                    if (lensesCount >= 2) {
                                                        Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(Color.Black).align(Alignment.BottomStart))
                                                        Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(Color.Black).align(Alignment.BottomEnd))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Folding physical hinge line
                                if (isFoldable) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.5.dp)
                                            .background(Color.White.copy(alpha = 0.35f))
                                    )
                                }

                                // Brand logo text overlay at the bottom
                                Text(
                                    text = phoneName.ifBlank { progress.brandName }.take(10),
                                    color = if (selectedColor.hexString == "#F0EAD6" || selectedColor.hexString == "#FFB7C5" || selectedColor.hexString == "#FFD700") Color.Black else Color.White,
                                    fontSize = minOf(13f, maxPhoneHeight.value * 0.07f).sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Solid Bottom cost box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تكلفة إنتاج النسخة الواحدة",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = usdFormatter.format(currentCost),
                            color = Color(0xFFEF233C),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
}

// =========================================================
// SPARE PARTS GRAPHIC ILLUSTRATION DESIGN COMPOSABLES (Arabic)
// =========================================================

@Composable
fun DesignSparePart(color: PhoneColor) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exploded Backplate graphic representing design spare parts
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                // Colored Backplate Layer
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(android.graphics.Color.parseColor(color.hexString)))
                )
                // Frame highlight
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 40.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(5.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "قطع غيار الهيكل وغلاف الحماية",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مصقولة بأشعة الليزر المضادة للخدوش ومهيأة للجمع الفيزيائي المتكامل بدقة.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun ScreenSparePart(design: DesignStyle) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Screen assembly component design drawing
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glass glass reflection
                Box(
                    modifier = Modifier
                        .size(width = 22.dp, height = 34.dp)
                        .border(1.5.dp, Color(0xFF5CC4FF), RoundedCornerShape(3.dp))
                        .background(Color(0xFF5CC4FF).copy(alpha = 0.1f))
                ) {
                    // Draw a bezel notch line / foldable hinge pattern based on style
                    if (design.id == "classic_bezel") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                                .align(Alignment.TopCenter)
                        )
                    } else if (design.id == "modern_punch") {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .align(Alignment.TopCenter)
                        )
                    } else if (design.id == "futuristic_fold") {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.5f))
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "لوح زجاج الشاشة ومصفوفة البكسلات",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "شاشات حماية فائقة الاستقطاب بمعدل تحديث سلس ومقاومة للصدمات من الجيل الخامس.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun ProcessorSparePart(processor: ProcessorOption) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microprocessor die layout
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                // Chip body
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFF334155), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFFFB703), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CPU",
                        color = Color(0xFFFFB703),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                // Micro-pins around the chip
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(5.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "خريطة اللوحة المطبوعة ورقاقة السليكون",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "محفور بتقنية النانومتر لتسريع تدفق النبضات الكهربائية وتخفيف حرارة المهام المتزامنة.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun BatterySparePart(battery: BatteryOption) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery block drawing
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                // Battery cell block
                Box(
                    modifier = Modifier
                        .size(width = 18.dp, height = 32.dp)
                        .background(Color(0xFF475569), RoundedCornerShape(2.dp))
                        .border(1.dp, Color(0xFF4AD66D), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Filled power indicator
                    val fillHeight = when (battery.id) {
                        "lithium_basic" -> 10.dp
                        "silicon_balanced" -> 16.dp
                        "graphene_pro" -> 22.dp
                        "nuclear_quantum" -> 30.dp
                        else -> 12.dp
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(fillHeight)
                            .background(Color(0xFF4AD66D))
                    )
                }

                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "خلية مركب الليثيوم والجرافين الطاقية",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "توفر تدفقًا آمنًا للقدرة وترفع ساعات الألعاب والبث المتواصل دون خطر السخونة الزائدة.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun CameraSparePart(camera: CameraOption) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera optical core drawing
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                // outer rim
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // inner lens iris
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF5CC4FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // laser center
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE11D48))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "عدسة الاستقطاب ومستشعر الصور الفوتوغرافي",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "عدسات زجاجية متعددة الانحناء تدعم تقريب وموازنة الاهتزاز مع حساسية دقيقة للقطات الليلية.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun ExtraTechSparePart() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // RF Antenna / Auxiliary technologies board
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SettingsSuggest,
                    contentDescription = null,
                    tint = Color(0xFFFFB703),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text(
                    text = "التقنيات الثانوية ورقاقات الدعم",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مجموعة من الشرائح المتطورة والأنظمة اللاسلكية التي ترتقي بالهاتف إلى رتبة الفئة الأولى.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}
