package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CreatedPhone
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.PlayerProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

sealed interface GameUiState {
    object Welcome : GameUiState
    data class Dashboard(val progress: PlayerProgress, val history: List<CreatedPhone>) : GameUiState
    data class LevelBrief(val progress: PlayerProgress, val challenge: LevelChallenge) : GameUiState
    data class Creator(
        val progress: PlayerProgress,
        val challenge: LevelChallenge,
        val selectedColor: PhoneColor,
        val selectedProcessor: ProcessorOption,
        val selectedCamera: CameraOption,
        val selectedDesign: DesignStyle,
        val selectedBattery: BatteryOption,
        val selectedDisplay: DisplayOption,
        val selectedOs: OsOption,
        val selectedTechs: Set<CustomTechnology>,
        val selectedCameraPosition: CameraPosition = CameraPosition.LEFT,
        val currentPhoneName: String
    ) : GameUiState

    data class Valuation(
        val progress: PlayerProgress,
        val challenge: LevelChallenge,
        val stats: PhoneStats,
        val phoneName: String,
        val selectedColor: PhoneColor,
        val selectedProcessor: ProcessorOption,
        val selectedCamera: CameraOption,
        val selectedDesign: DesignStyle,
        val selectedBattery: BatteryOption,
        val selectedDisplay: DisplayOption,
        val selectedOs: OsOption,
        val selectedTechs: Set<CustomTechnology>,
        val selectedCameraPosition: CameraPosition = CameraPosition.LEFT,
        val proposedPrice: Double,
        val researchTier: Int = 1, // 0 = Basic, 1 = Standard, 2 = Advanced
        val manufacturingQuality: Int = 1, // 0 = Economy, 1 = Standard, 2 = Premium
        val marketingCampaign: Int = 1  // 0 = None, 1 = Social, 2 = Influencer, 3 = Global
    ) : GameUiState

    data class MarketSimulationResult(
        val progress: PlayerProgress,
        val challenge: LevelChallenge,
        val phoneName: String,
        val specs: PhoneStats,
        val retailPrice: Double,
        val unitsSold: Int,
        val grossRevenue: Double,
        val netProfit: Double,
        val finalRating: Float,
        val customerReviews: List<String>,
        val isLevelBeaten: Boolean,
        val competitorPhones: List<CompetitorPhoneResult>
    ) : GameUiState
}

data class CompetitorPhoneResult(
    val companyName: String,
    val phoneName: String,
    val cost: Double,
    val rating: Float,
    val unitsSold: Int
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Welcome)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Transient cached records for flows
    private var currentPlayerProgress: PlayerProgress? = null
    private var runningHistory: List<CreatedPhone> = emptyList()

    init {
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())

        // Listen for database changes to keep active states synced
        viewModelScope.launch {
            repository.playerProgress.collectLatest { progress ->
                currentPlayerProgress = progress
                val hist = runningHistory
                if (progress != null) {
                    // If we were on the welcome screen but player has setup, move them to dashboard automatically
                    if (_uiState.value is GameUiState.Welcome && progress.brandName.isNotBlank()) {
                        _uiState.value = GameUiState.Dashboard(progress, hist)
                    } else {
                        updateActiveProgress(progress)
                    }
                } else {
                    _uiState.value = GameUiState.Welcome
                }
            }
        }

        viewModelScope.launch {
            repository.allCreatedPhones.collectLatest { history ->
                runningHistory = history
                val progress = currentPlayerProgress
                if (progress != null && progress.brandName.isNotBlank()) {
                    val current = _uiState.value
                    if (current is GameUiState.Dashboard) {
                        _uiState.value = GameUiState.Dashboard(progress, history)
                    }
                }
            }
        }
    }

    private fun updateActiveProgress(newProgress: PlayerProgress) {
        when (val current = _uiState.value) {
            is GameUiState.Dashboard -> _uiState.value = current.copy(progress = newProgress, history = runningHistory)
            is GameUiState.LevelBrief -> _uiState.value = current.copy(progress = newProgress)
            is GameUiState.Creator -> _uiState.value = current.copy(progress = newProgress)
            is GameUiState.Valuation -> _uiState.value = current.copy(progress = newProgress)
            is GameUiState.MarketSimulationResult -> _uiState.value = current.copy(progress = newProgress)
            else -> {}
        }
    }

    fun startNewGame(brandName: String, playerName: String) {
        viewModelScope.launch {
            val progress = PlayerProgress(
                brandName = brandName,
                playerName = playerName,
                funds = 180000.0, // generous starting budget for Level 1
                currentLevel = 1,
                fans = 150,
                phonesCreatedCount = 0
            )
            repository.savePlayerProgress(progress)
            _uiState.value = GameUiState.Dashboard(progress, emptyList())
        }
    }

    fun enterLevelBrief(levelNum: Int) {
        val progress = currentPlayerProgress ?: return
        val challenge = GameDefaults.Levels.firstOrNull { it.levelNumber == levelNum } ?: GameDefaults.Levels.first()
        _uiState.value = GameUiState.LevelBrief(progress, challenge)
    }

    fun startPhoneCreator(challenge: LevelChallenge) {
        val progress = currentPlayerProgress ?: return
        _uiState.value = GameUiState.Creator(
            progress = progress,
            challenge = challenge,
            selectedColor = GameDefaults.Colors.first(),
            selectedProcessor = GameDefaults.Processors.first(),
            selectedCamera = GameDefaults.Cameras.first(),
            selectedDesign = GameDefaults.DesignStyles.first(),
            selectedBattery = GameDefaults.Batteries.first(),
            selectedDisplay = GameDefaults.Displays.first(),
            selectedOs = GameDefaults.OperatingSystems.first(),
            selectedTechs = emptySet(),
            currentPhoneName = "نوفا ${progress.phonesCreatedCount + 1}"
        )
    }

    fun updateCreatorState(
        color: PhoneColor? = null,
        processor: ProcessorOption? = null,
        camera: CameraOption? = null,
        design: DesignStyle? = null,
        battery: BatteryOption? = null,
        display: DisplayOption? = null,
        os: OsOption? = null,
        techs: Set<CustomTechnology>? = null,
        cameraPosition: CameraPosition? = null,
        name: String? = null
    ) {
        val current = _uiState.value
        if (current is GameUiState.Creator) {
            _uiState.value = current.copy(
                selectedColor = color ?: current.selectedColor,
                selectedProcessor = processor ?: current.selectedProcessor,
                selectedCamera = camera ?: current.selectedCamera,
                selectedDesign = design ?: current.selectedDesign,
                selectedBattery = battery ?: current.selectedBattery,
                selectedDisplay = display ?: current.selectedDisplay,
                selectedOs = os ?: current.selectedOs,
                selectedTechs = techs ?: current.selectedTechs,
                selectedCameraPosition = cameraPosition ?: current.selectedCameraPosition,
                currentPhoneName = name ?: current.currentPhoneName
            )
        }
    }

    fun advanceToValuation() {
        val current = _uiState.value
        if (current is GameUiState.Creator) {
            // Compute specifications stats
            val stats = calculateStats(
                selectedColor = current.selectedColor,
                selectedProcessor = current.selectedProcessor,
                selectedCamera = current.selectedCamera,
                selectedDesign = current.selectedDesign,
                selectedBattery = current.selectedBattery,
                selectedDisplay = current.selectedDisplay,
                selectedOs = current.selectedOs,
                selectedTechs = current.selectedTechs,
                cameraPosition = current.selectedCameraPosition
            )

            // Deduct Development Cost (e.g., 500 units manufacturing worth)
            // RD level reduces this cost: 1 = full, 2 = 95%, 3 = 90%, etc.
            val rdReduction = 1.0 - (current.progress.rdLevel - 1) * 0.05
            val devCost = (stats.manufacturingCost * 500.0 * rdReduction).coerceAtLeast(100.0)
            
            if (current.progress.funds < devCost) return 

            viewModelScope.launch {
                val updatedProgress = current.progress.copy(funds = current.progress.funds - devCost)
                repository.savePlayerProgress(updatedProgress)

                _uiState.value = GameUiState.Valuation(
                    progress = updatedProgress,
                    challenge = current.challenge,
                    stats = stats,
                    phoneName = current.currentPhoneName,
                    selectedColor = current.selectedColor,
                    selectedProcessor = current.selectedProcessor,
                    selectedCamera = current.selectedCamera,
                    selectedDesign = current.selectedDesign,
                    selectedBattery = current.selectedBattery,
                    selectedDisplay = current.selectedDisplay,
                    selectedOs = current.selectedOs,
                    selectedTechs = current.selectedTechs,
                    selectedCameraPosition = current.selectedCameraPosition,
                    proposedPrice = (stats.manufacturingCost * 1.5).roundToInt().toDouble() // default 50% premium
                )
            }
        }
    }

    fun updateProposedPrice(price: Double) {
        val current = _uiState.value
        if (current is GameUiState.Valuation) {
            _uiState.value = current.copy(proposedPrice = price)
        }
    }

    fun updateValuationBudgets(research: Int?, quality: Int?, marketing: Int?) {
        val current = _uiState.value
        if (current is GameUiState.Valuation) {
            _uiState.value = current.copy(
                researchTier = research ?: current.researchTier,
                manufacturingQuality = quality ?: current.manufacturingQuality,
                marketingCampaign = marketing ?: current.marketingCampaign
            )
        }
    }

    fun backToCreator() {
        val current = _uiState.value
        if (current is GameUiState.Valuation) {
            _uiState.value = GameUiState.Creator(
                progress = current.progress,
                challenge = current.challenge,
                selectedColor = current.selectedColor,
                selectedProcessor = current.selectedProcessor,
                selectedCamera = current.selectedCamera,
                selectedDesign = current.selectedDesign,
                selectedBattery = current.selectedBattery,
                selectedDisplay = current.selectedDisplay,
                selectedOs = current.selectedOs,
                selectedTechs = current.selectedTechs,
                currentPhoneName = current.phoneName
            )
        }
    }

    fun backToDashboard() {
        val progress = currentPlayerProgress
        if (progress != null) {
            _uiState.value = GameUiState.Dashboard(progress, runningHistory)
        } else {
            _uiState.value = GameUiState.Welcome
        }
    }

    fun simulateMarketLaunch() {
        val current = _uiState.value as? GameUiState.Valuation ?: return
        GameSoundPlayer.playPhoneLaunchSound()
        val stats = current.stats
        val price = current.proposedPrice
        val challenge = current.challenge
        val progress = current.progress

        // 1. Extract competitors & specs
        val compStats1 = challenge.competitor1
        val compStats2 = challenge.competitor2

        // Player's phone specifications
        val pPerf = stats.performance
        val pCam = stats.cameraPower
        val pDsn = stats.designSelection
        val pTech = stats.technologyAppeal
        val pPrice = price

        // Competitor 1 Specifications (Estimated design & tech from speed/camera)
        val c1Perf = compStats1.speedRating
        val c1Cam = compStats1.cameraRating
        val c1Dsn = compStats1.speedRating * 0.95
        val c1Tech = compStats1.speedRating * 0.85
        val c1Price = compStats1.marketPrice

        // Competitor 2 Specifications
        val c2Perf = compStats2.speedRating
        val c2Cam = compStats2.cameraRating
        val c2Dsn = compStats2.speedRating * 0.98
        val c2Tech = compStats2.speedRating * 0.90
        val c2Price = compStats2.marketPrice

        // --- BUDS AND COST MULTIPLIERS ---
        val rDMultiplier = when (current.researchTier) {
            0 -> 0.95
            2 -> 1.10
            else -> 1.0
        }
        val buildQualityMultiplier = when (current.manufacturingQuality) {
            0 -> 0.85
            2 -> 1.15
            else -> 1.0
        }
        val overallUtilityMultiplier = rDMultiplier * buildQualityMultiplier

        val qMultiplier = when (current.manufacturingQuality) {
            0 -> 0.80
            2 -> 1.25
            else -> 1.0
        }
        val adjustedManufacturingCost = stats.manufacturingCost * qMultiplier

        val researchCost = when (current.researchTier) {
            0 -> 500.0
            1 -> 2500.0
            2 -> 8000.0
            else -> 2500.0
        }
        val marketingCost = when (current.marketingCampaign) {
            0 -> 0.0
            1 -> 1500.0
            2 -> 6500.0
            3 -> 22000.0
            else -> 1500.0
        }
        val budgetInvestments = researchCost + marketingCost

        // 2. Compute Segment-specific consumer utility for each phone
        val basePlayerUtility = calculatePhoneUtility(pPerf, pCam, pDsn, pTech, pPrice, challenge.idealFocus)
        
        // Cohesion and Optimization checks
        var cohesionMultiplier = 1.0
        val cpuScore = current.selectedProcessor.performanceScore
        val osId = current.selectedOs.id
        
        if (osId == "os_fruit" && cpuScore < 40.0) {
            cohesionMultiplier = 0.82 // Heavy OS on weak CPU penalty
        } else if (osId == "os_basic" && cpuScore > 75.0) {
            cohesionMultiplier = 1.08 // Light OS on power CPU bonus
        } else if (osId == "os_custom" && cpuScore > 50.0) {
            cohesionMultiplier = 1.04 // Balanced performance
        }

        val playerUtility = basePlayerUtility * overallUtilityMultiplier * cohesionMultiplier
        val comp1Utility = calculatePhoneUtility(c1Perf, c1Cam, c1Dsn, c1Tech, c1Price, challenge.idealFocus)
        val comp2Utility = calculatePhoneUtility(c2Perf, c2Cam, c2Dsn, c2Tech, c2Price, challenge.idealFocus)

        // 3. Apply Brand reputation / fan base factor to utility to derive attraction
        // Player's brand power goes up with fan density
        val playerBrandMultiplier = 0.92 + (progress.fans.toDouble() / 40000.0).coerceAtMost(0.68)
        // Competitors are established giants in their respectively targeted levels
        val comp1BrandMultiplier = 1.15
        val comp2BrandMultiplier = 1.30
        
        val mktAttractionMultiplier = when (current.marketingCampaign) {
            0 -> 0.72
            1 -> 1.0
            2 -> 1.38
            3 -> 1.85
            else -> 1.0
        }
        
        val playerAttraction = max(1.0, playerUtility * playerBrandMultiplier * mktAttractionMultiplier)
        val comp1Attraction = max(1.0, comp1Utility * comp1BrandMultiplier)
        val comp2Attraction = max(1.0, comp2Utility * comp2BrandMultiplier)

        // 4. Calculate Market Share based on relative attractions (Comparative Draft Model)
        val totalAttraction = playerAttraction + comp1Attraction + comp2Attraction
        val playerShare = playerAttraction / totalAttraction
        val comp1Share = comp1Attraction / totalAttraction
        val comp2Share = comp2Attraction / totalAttraction

        // Total consumer volumes per level challenge requirements
        val factoryLevel = progress.factoryLevel
        val factoryConfig = GameDefaults.FactoryConfigs.firstOrNull { it.level == factoryLevel } ?: GameDefaults.FactoryConfigs.first()
        val productionSpeedBonus = factoryConfig.productionSpeedMultiplier

        val marketVolume = when (challenge.levelNumber) {
            1 -> 16000
            2 -> 34000
            3 -> 80000
            4 -> 180000
            else -> 100000
        }

        // Apply a small organic variance (+-10%) to units sold to feel natural
        val randomFactor = 0.9 + (Math.random() * 0.2)
        val unitsSoldRaw = (marketVolume * playerShare * randomFactor * productionSpeedBonus).roundToInt()
        val unitsSold = max(120, unitsSoldRaw) // guaranteed baseline for effort

        val comp1Units = max(100, (marketVolume * comp1Share * (0.9 + Math.random() * 0.2)).roundToInt())
        val comp2Units = max(100, (marketVolume * comp2Share * (0.9 + Math.random() * 0.2)).roundToInt())

        // 5. Financial calculations for the player
        val grossRevenue = unitsSold * price
        val totalCostOfGoods = unitsSold * adjustedManufacturingCost
        val netProfit = grossRevenue - totalCostOfGoods - budgetInvestments

        // 6. Rating Calculation out of 5 stars
        // Highly rated phones match specifications well and present great pricing value!
        val baseStars = (playerUtility / 20.0).coerceIn(1.0, 5.0)
        val randomAura = (Math.random() * 0.3) - 0.15
        val finalRating = ((baseStars + randomAura).coerceIn(1.0, 5.5) * 10).roundToInt().toFloat() / 10f
        val formattedRating = min(5.0f, finalRating)

        // Competitor Ratings formatted cleanly too
        val c1Stars = (comp1Utility / 20.0).coerceIn(1.0, 5.0)
        val c2Stars = (comp2Utility / 20.0).coerceIn(1.0, 5.0)
        val formattedC1Rating = ((c1Stars * 10).roundToInt().toFloat() / 10f).coerceIn(1f, 5f)
        val formattedC2Rating = ((c2Stars * 10).roundToInt().toFloat() / 10f).coerceIn(1f, 5f)

        // 7. Dynamic comparative Arabic customer reviews
        val reviews = mutableListOf<String>()

        val costRatio = price / stats.manufacturingCost
        if (costRatio > 2.2) {
            reviews.add("أوه! سعر الهاتف مبالغ فيه للغاية مقارنة بتكلفة تصنيعه، هذا جشع تجاري! 💸")
        } else if (costRatio < 1.22) {
            reviews.add("قيمة تسعيرية ثورية! يكاد يكون بسعر التكلفة، شكراً لشركة ${progress.brandName}! ❤️")
        }

        // Compare price directly to competitors
        if (price < c1Price && price < c2Price) {
            reviews.add("أعجبني السعر كثيراً، إنه اقتصادي وأرخص بكثير من الأجهزة المنافسة في السوق مثل ${compStats1.phoneName}. 🤩")
        } else if (price > c1Price && price > c2Price && playerUtility < comp1Utility) {
            reviews.add("لماذا نشتري هذا الهاتف بسعره المرتفع بينما ${compStats1.phoneName} أرخص ويقدم تجربة مقاربة؟ 🧐")
        }

        // Compare Performance
        if (pPerf > c1Perf && pPerf > c2Perf) {
            reviews.add("سرعة المعالجة مذهلة! يتخطى أداء هذا الهاتف كبار أجهزة السوق مثل ${compStats1.phoneName} بوضوح ساطع. ⚡🔥")
        } else if (pPerf < c1Perf && pPerf < c2Perf) {
            reviews.add("الهاتف بطيء نوعاً ما، مقارنة بـ ${compStats1.phoneName} الذي يتمتع بسلاسة فائقة وسرعة فتح مذهلة. 🐢")
        }

        // Compare Camera
        if (pCam > c1Cam && pCam > c2Cam) {
            reviews.add("جودة الكاميرات والتصوير الليلي تسحق أقوى المنافسين مثل ${compStats2.phoneName} بلا رحمة! صور مبهرة. 📸💎")
        } else if (pCam < c1Cam && pCam < c2Cam) {
            reviews.add("جودة الصور مخيبة للآمال، تمنيت لو كانت الكاميرا بقوة عدسة ${compStats2.phoneName}. 🌑")
        }

        // Compare OS
        when (current.selectedOs.id) {
            "os_fruit" -> reviews.add("نظام التشغيل الفاخر أعطى الهاتف طابعاً حصرياً وجاذبية كبيرة لمحبي الأناقة والسهولة. 🍎✨")
            "os_custom" -> reviews.add("واجهة نوفا مرنة جداً، أعشق إمكانيات التخصيص التي لا تنتهي في هذا الهاتف. 🎨📱")
            "os_basic" -> reviews.add("النظام الخام سريع جداً وبسيط، يذكرني بالسرعة التي فقدتها في الهواتف المليئة بالتطبيقات الزائدة. ⚡🤖")
        }

        // Compare Design
        if (pDsn > 85.0) {
            reviews.add("تصميم الهاتف فني لدرجة مبهرة، أشعر أنني أحمل قطعة من المستقبل بين يديني. 🌌💎")
        } else if (pDsn < 35.0) {
            reviews.add("تصميم الهاتف يبدو قديماً ومملّاً، كان بالإمكان أفضل مما كان بكثير. 🏚️")
        }

        // Additional specifications commentary
        if (current.selectedTechs.contains(GameDefaults.Technologies.firstOrNull { it.id == "tech_titanium" })) {
            reviews.add("الهيكل التيتانيومي رائع وقوي، يعطي إحساساً بالأمان والصلابة عند الإمساك به. 💎")
        }
        if (current.selectedTechs.contains(GameDefaults.Technologies.firstOrNull { it.id == "tech_charge_120" })) {
            reviews.add("تقنية الشحن الخارق بقوة 120 واط مدهشة! يشحن الهاتف بسرعة خرافية. ⚡")
        }

        when (current.selectedBattery.id) {
            "nuclear_quantum" -> reviews.add("بطارية الطاقة الكمية كالسحر! يدوم شحن الهاتف لعدة أيام دون الحاجة لمنفذ جداري. 🔋🪐")
            "graphene_pro" -> reviews.add("خلية الجرافين تطور تقني بارز، الهاتف لا يسخن كثيراً والبطارية صامدة وممتازة. 🎮🔋")
            "lithium_basic" -> reviews.add("البطارية تنفد بسرعة مع الاستخدام المكثف، وتعتبر نقطة ضعف واضحة للجهاز. 📉")
            else -> reviews.add("سعة البطارية جيدة وتكفي لتغطية كامل المهام اليومية المعتادة بشكل مرضٍ. 👍")
        }

        if (reviews.size < 3) {
            reviews.add("التصميم الخارجي أنيق ووزن الهاتف متناسق ومريح جداً للاستخدام بيد واحدة. 🙌")
            reviews.add("الجمهور فخور برؤية منتج وطني من شركة ${progress.brandName} ينافس العلامات التجارية الكبرى. 🇸🇦")
        }

        val finalReviews = reviews.distinct().shuffled().take(4)

        // Competitors result package
        val competitors = listOf(
            CompetitorPhoneResult(
                companyName = compStats1.brandName,
                phoneName = compStats1.phoneName,
                cost = compStats1.marketPrice,
                rating = formattedC1Rating,
                unitsSold = comp1Units
            ),
            CompetitorPhoneResult(
                companyName = compStats2.brandName,
                phoneName = compStats2.phoneName,
                cost = compStats2.marketPrice,
                rating = formattedC2Rating,
                unitsSold = comp2Units
            )
        )

        // 8. Challenge parameters evaluation
        val unitsGoalMet = unitsSold >= challenge.requiredUnitsSold
        // While we track these for the UI, the level-up trigger will now follow the user's focus on sales
        val profitGoalMet = netProfit >= challenge.requiredNetProfit
        val ratingMet = formattedRating >= challenge.ratingRequirement

        val isLevelBeaten = unitsGoalMet // User specifically asked for sales trigger

        // 9. Persist update records asynchronously
        viewModelScope.launch {
            var updatedFunds = progress.funds + netProfit
            // Guard bankrupt state with starter support grants
            if (updatedFunds < 15000) {
                updatedFunds = 40000.0
            }

            val nextLevel = if (isLevelBeaten && progress.currentLevel == challenge.levelNumber) {
                min(4, progress.currentLevel + 1)
            } else {
                progress.currentLevel
            }

            // Calculate reputational gains or loss on fans
            val mktFansMultiplier = when (current.marketingCampaign) {
                0 -> 0.5
                1 -> 1.0
                2 -> 1.5
                3 -> 2.5
                else -> 1.0
            }
            val addedFansFactor = formattedRating / challenge.ratingRequirement
            // Base growth from rating
            var addedFansBase = (unitsSold * addedFansFactor * 0.05 * mktFansMultiplier)
            
            // Bonus growth if exceeded quality expectations
            if (formattedRating > 4.5) addedFansBase *= 1.3
            
            val addedFans = addedFansBase.roundToInt()
            val newFans = progress.fans + max(5, addedFans)

            val updatedProgress = progress.copy(
                funds = updatedFunds,
                currentLevel = nextLevel,
                fans = newFans,
                phonesCreatedCount = progress.phonesCreatedCount + 1
            )

            val phoneEntity = CreatedPhone(
                name = current.phoneName,
                colorHex = current.selectedColor.hexString,
                colorNameName = current.selectedColor.nameAr,
                processorName = current.selectedProcessor.nameAr,
                cameraName = current.selectedCamera.nameAr,
                designTypeName = current.selectedDesign.nameAr,
                manufacturingCost = adjustedManufacturingCost,
                retailPrice = price,
                unitsSold = unitsSold,
                revenue = grossRevenue,
                netProfit = netProfit,
                starRating = formattedRating,
                feedbackMessage = finalReviews.joinToString(". "),
                levelAssoc = challenge.levelNumber
            )

            repository.saveCreatedPhone(phoneEntity)
            repository.savePlayerProgress(updatedProgress)

            _uiState.value = GameUiState.MarketSimulationResult(
                progress = updatedProgress,
                challenge = challenge,
                phoneName = current.phoneName,
                specs = stats,
                retailPrice = price,
                unitsSold = unitsSold,
                grossRevenue = grossRevenue,
                netProfit = netProfit,
                finalRating = formattedRating,
                customerReviews = finalReviews,
                isLevelBeaten = isLevelBeaten,
                competitorPhones = competitors
            )
        }
    }

    private fun calculatePhoneUtility(
        performance: Double,
        camera: Double,
        design: Double,
        technology: Double,
        price: Double,
        idealFocus: String
    ): Double {
        return when (idealFocus) {
            "BUDGET" -> {
                val priceUtility = if (price <= 70.0) 100.0
                else if (price >= 250.0) 5.0
                else ((250.0 - price) / 180.0) * 100.0

                val specsScore = (performance * 0.3 + camera * 0.2 + design * 0.3 + technology * 0.2)
                (priceUtility * 0.70) + (specsScore * 0.30)
            }
            "BALANCED" -> {
                val priceUtility = if (price <= 150.0) 100.0
                else if (price >= 500.0) 5.0
                else ((500.0 - price) / 350.0) * 100.0

                val specPart = performance * 0.2 + camera * 0.2 + design * 0.3 + technology * 0.3
                (priceUtility * 0.35) + (specPart * 0.65)
            }
            "CAMERA" -> {
                val priceUtility = if (price <= 500.0) 100.0
                else if (price >= 1400.0) 5.0
                else ((1400.0 - price) / 900.0) * 100.0

                val specPart = camera * 0.45 + performance * 0.10 + design * 0.20 + technology * 0.25
                (priceUtility * 0.15) + (specPart * 0.85)
            }
            "FUTURISTIC" -> {
                val priceUtility = if (price <= 900.0) 100.0
                else if (price >= 3500.0) 5.0
                else ((3500.0 - price) / 2600.0) * 100.0

                val specPart = technology * 0.40 + performance * 0.20 + design * 0.30 + camera * 0.10
                (priceUtility * 0.05) + (specPart * 0.95)
            }
            else -> {
                val priceUtility = ((1500.0 - price.coerceIn(100.0, 1500.0)) / 1400.0) * 100.0
                priceUtility * 0.25 + performance * 0.25 + camera * 0.25 + (design + technology) * 0.125
            }
        }
    }

    private fun getWorkerLevel(key: String): Int {
        val prefs = getApplication<Application>().getSharedPreferences("HqWorkers", android.content.Context.MODE_PRIVATE)
        return prefs.getInt(key, 1)
    }

    fun upgradeWorker(workerKey: String, cost: Double): Boolean {
        val progress = currentPlayerProgress ?: return false
        if (progress.funds >= cost) {
            GameSoundPlayer.playUpgradeSuccessSound()
            viewModelScope.launch {
                val updatedFunds = progress.funds - cost
                val updatedProgress = progress.copy(funds = updatedFunds)
                repository.savePlayerProgress(updatedProgress)

                val prefs = getApplication<Application>().getSharedPreferences("HqWorkers", android.content.Context.MODE_PRIVATE)
                val currentLvl = prefs.getInt(workerKey, 1)
                prefs.edit().putInt(workerKey, currentLvl + 1).apply()
            }
            return true
        }
        return false
    }

    fun upgradeFactory() {
        val progress = currentPlayerProgress ?: return
        val currentLevel = progress.factoryLevel
        val nextConfig = GameDefaults.FactoryConfigs.firstOrNull { it.level == currentLevel + 1 } ?: return
        
        if (progress.funds >= nextConfig.upgradeCost) {
            GameSoundPlayer.playUpgradeSuccessSound()
            viewModelScope.launch {
                val updatedProgress = progress.copy(
                    funds = progress.funds - nextConfig.upgradeCost,
                    factoryLevel = nextConfig.level
                )
                repository.savePlayerProgress(updatedProgress)
            }
        }
    }

    fun upgradeRD() {
        val progress = currentPlayerProgress ?: return
        val nextLevel = progress.rdLevel + 1
        val upgradeCost = 25000.0 * nextLevel.toDouble().pow(1.5)
        
        if (progress.funds >= upgradeCost) {
            GameSoundPlayer.playUpgradeSuccessSound()
            viewModelScope.launch {
                val updatedProgress = progress.copy(
                    funds = progress.funds - upgradeCost,
                    rdLevel = nextLevel
                )
                repository.savePlayerProgress(updatedProgress)
            }
        }
    }

    private fun calculateStats(
        selectedColor: PhoneColor,
        selectedProcessor: ProcessorOption,
        selectedCamera: CameraOption,
        selectedDesign: DesignStyle,
        selectedBattery: BatteryOption,
        selectedDisplay: DisplayOption,
        selectedOs: OsOption,
        selectedTechs: Set<CustomTechnology>,
        cameraPosition: CameraPosition
    ): PhoneStats {
        val progress = currentPlayerProgress
        val factoryLevel = progress?.factoryLevel ?: 1
        val factoryConfig = GameDefaults.FactoryConfigs.firstOrNull { it.level == factoryLevel } ?: GameDefaults.FactoryConfigs.first()

        // Load worker levels
        val perfLevel = getWorkerLevel("worker_performance_lvl")
        val designLevel = getWorkerLevel("worker_design_lvl")
        val cameraLevel = getWorkerLevel("worker_camera_lvl")
        val techLevel = getWorkerLevel("worker_tech_lvl")

        // Base specs + worker level boosters (+6.0 per level above 1)
        var performance = selectedProcessor.performanceScore + (perfLevel - 1) * 6.0
        var cameraPower = selectedCamera.cameraScore + (cameraLevel - 1) * 6.0
        var designScore = selectedDesign.designScore + selectedColor.designBonus + (designLevel - 1) * 6.0
        var techAppeal = selectedBattery.scoreBonus + selectedDisplay.displayScore + selectedOs.uiBonus + (techLevel - 1) * 6.0

        // Accumulate feature additions
        var totalCost = selectedProcessor.baseCost + selectedCamera.baseCost + selectedDesign.baseCost + 
                        selectedColor.colorCost + selectedBattery.baseCost + selectedDisplay.baseCost + selectedOs.baseCost
 
        selectedTechs.forEach { tech ->
            totalCost += tech.addedCost
            techAppeal += tech.techScoreBonus
            if (tech.isFuturistic) {
                designScore += 5.0
            }
        }

        // Apply Factory Cost Reduction
        totalCost = max(5.0, totalCost - factoryConfig.unitCostReduction)

        // Clip maximums
        performance = min(100.0, performance)
        cameraPower = min(100.0, cameraPower)
        designScore = min(100.0, designScore)
        techAppeal = min(100.0, techAppeal)

        return PhoneStats(
            performance = performance,
            cameraPower = cameraPower,
            designSelection = designScore,
            technologyAppeal = techAppeal,
            manufacturingCost = totalCost,
            cameraPosition = cameraPosition
        )
    }

    fun restartGameProgress() {
        viewModelScope.launch {
            repository.resetGame()
            _uiState.value = GameUiState.Welcome
        }
    }
}
