package com.example.game

data class PhoneStats(
    val performance: Double,      // 0 to 100
    val cameraPower: Double,     // 0 to 100
    val designSelection: Double,  // 0 to 100
    val technologyAppeal: Double, // 0 to 100
    val manufacturingCost: Double // Production cost in USD
)

data class PhoneColor(
    val id: String,
    val nameAr: String,
    val hexString: String,
    val colorCost: Double,
    val designBonus: Double
)

data class ProcessorOption(
    val id: String,
    val nameAr: String,
    val techDetailAr: String,
    val performanceScore: Double,
    val baseCost: Double
)

data class CameraOption(
    val id: String,
    val nameAr: String,
    val megapixelsTextAr: String,
    val cameraScore: Double,
    val baseCost: Double
)

data class DesignStyle(
    val id: String,
    val nameAr: String,
    val techDetailAr: String,
    val designScore: Double,
    val baseCost: Double
)

data class CustomTechnology(
    val id: String,
    val nameAr: String,
    val descAr: String,
    val addedCost: Double,
    val techScoreBonus: Double,
    val isFuturistic: Boolean = false
)

data class BatteryOption(
    val id: String,
    val nameAr: String,
    val descAr: String,
    val capacityMah: Int,
    val baseCost: Double,
    val scoreBonus: Double
)

data class LevelChallenge(
    val levelNumber: Int,
    val titleAr: String,
    val subtitleAr: String,
    val targetSegmentAr: String,
    val budgetGiven: Double,
    val descriptionAr: String,
    val requiredUnitsSold: Int,
    val requiredNetProfit: Double,
    val ratingRequirement: Float,
    val difficultyMultiplier: Double,
    val idealFocus: String, // "BUDGET", "BALANCED", "CAMERA", "FUTURISTIC"
    val competitor1: CompetitorModel,
    val competitor2: CompetitorModel
)

data class CompetitorModel(
    val brandName: String,
    val phoneName: String,
    val cameraRating: Double,
    val speedRating: Double,
    val marketPrice: Double
)

// Pre-seeded lists representing the choices available in-game
object GameDefaults {
    val Colors = listOf(
        PhoneColor("titanium_gold", "تيتانيوم ذهبي فاخر", "#FFD700", 80.0, 15.0),
        PhoneColor("stealth_black", "أسود كربوني غامق", "#1A1A1A", 20.0, 8.0),
        PhoneColor("aurora_blue", "أزرق الأورورا المتدرج", "#007FFF", 45.0, 12.0),
        PhoneColor("emerald_green", "أخضر زمردي برّاق", "#097969", 35.0, 10.0),
        PhoneColor("sakura_pink", "وردي الساكورا الناعم", "#FFB7C5", 30.0, 9.0),
        PhoneColor("pearl_white", "أبيض لؤلؤي ناصع", "#F0EAD6", 15.0, 7.0)
    )

    val Processors = listOf(
        ProcessorOption("dual_basic", "معالج ثنائي النواة (Lite Core)", "تردد 1.2 جيجاهرتز، اقتصادي ومناسب للمهام البسيطة.", 25.0, 15.0),
        ProcessorOption("hexa_balanced", "سداسي النواة (SnapBalance 600)", "أداء ممتاز للمهام اليومية واستهلاك متقن للطاقة.", 50.0, 45.0),
        ProcessorOption("octa_power", "ثماني النواة (ProSnap G900)", "أداء عالي للألعاب الثقيلة وتعدد مهام فائق السرعة.", 78.0, 95.0),
        ProcessorOption("quantum_ai", "شريحة الذكاء الفائق (Quantum AI Core)", "معالجة عصبية متطورة، طاقة غير محدودة، ودعم للجيل القادم.", 96.0, 220.0)
    )

    val Cameras = listOf(
        CameraOption("single_basic", "كاميرا أحادية 12 ميجا بكسل", "عدسة كلاسيكية بتركيز ميكرو وصور مقبولة للإضاءة العالية.", 20.0, 12.0),
        CameraOption("dual_advanced", "كاميرا ثنائية 48 ميجا بكسل بذاكرة بصرية", "عدسة واسعة بدقة عالية ودعم عزل ممتاز للخلفية.", 50.0, 38.0),
        CameraOption("triple_pro", "كاميرا ثلاثية فوتوغرافية 108 ميجا بكسل", "تقريب بصري 10x وتصوير ليلي مع فلاتر ذكاء اصطناعي سينمائي.", 82.0, 85.0),
        CameraOption("quantum_lens", "عدسة الفضاء 200 ميجا بكسل (Quantum Eye)", "تقريب فلكي 100x ومستشعر رؤية ليلية حراري فائق المدى كليًا.", 100.0, 190.0)
    )

    val DesignStyles = listOf(
        DesignStyle("notch_circular", "نوتش دائري (Circular Notch)", "ثقب كاميرا علوي صغير دائري يزيد من مساحة الرؤية والأناقة.", 45.0, 20.0),
        DesignStyle("notch_large", "نوتش كبير (Classic Notch)", "تصميم النوتش العريض الكلاسيكي الذي يضم مستشعرات الإضاءة والصوت.", 25.0, 10.0),
        DesignStyle("notch_iphone", "نوتش ايفون (iPhone Dynamic Island)", "شريط تفاعلي ذكي يتغير شكله ديناميكيًا لعرض الإشعارات والتحكم.", 90.0, 75.0),
        DesignStyle("notch_side", "نوتش جانبي (Side Notch/Punch)", "ثقب كاميرا جانبي أنيق في أعلى الزاوية اليسرى لتجربة ألعاب وحواص بلا حواشي.", 50.0, 25.0),
        DesignStyle("under_display", "شاشة الكاميرا الخفية (Under-Screen Invisible)", "واجهة أمامية كاملة خالية من الثقوب، غامرة ومستقبلية للغاية.", 85.0, 90.0),
        DesignStyle("futuristic_fold", "هيكل قابل للطي ثنائي الأجنحة (Hex Fold)", "شاشة مرنة تتحول من هاتف متكامل إلى جهاز لوحي رائع وببريق ترفيهي.", 100.0, 250.0)
    )

    val Technologies = listOf(
        CustomTechnology("tech_5g", "دعم شبكات الجيل الخامس (5G Speed)", "اتصال فائق السرعة واستجابة فورية للألعاب السحابية.", 30.0, 10.0),
        CustomTechnology("tech_cooling", "التبريد المائي السائل (Chilled Liquid)", "نظام تبريد متقدم يقلل درجة حرارة المعالج عند الألعاب 10 درجات.", 25.0, 8.0),
        CustomTechnology("tech_satellite", "اتصال الطوارئ بالأقمار الصناعية (SkyLink)", "القدرة على الاتصال وطلب النجدة عبر الفضاء حتى لو تلفت الشبكات.", 50.0, 15.0, isFuturistic = true),
        CustomTechnology("tech_ai_chip", "شريحة تسريع مهام الذكاء الاصطناعي (AI NPU)", "تحويل الصور لمقطوعات فنية وترجمة فورية ومساعد شخصي فوري.", 40.0, 12.0),
        CustomTechnology("tech_titanium", "جسم تيتانيوم من الدرجة الطبية (Grade 5)", "متانة فائقة مضادة للسقطات القاسية مع وزن أخف ومقاومة للحرائق وبصمات الأصابع.", 60.0, 18.0),
        CustomTechnology("tech_charge_120", "شحن فلاش بقوة 120 واط", "شحن بطارية الهاتف بالكامل من 0% إلى 100% في غضون 12 دقيقة فقط.", 35.0, 11.0)
    )

    val Batteries = listOf(
        BatteryOption("lithium_basic", "بطارية ليثيوم كلاسيكية", "سعة 3500 مللي أمبير، سريعة النفاد تحت الضغط واستخدام اقتصادي.", 3500, 10.0, 5.0),
        BatteryOption("silicon_balanced", "بطارية الكاربون المزدوجة", "سعة 4800 مللي أمبير، تيار منظم وعمر افتراضي طويل للاستخدام المتوسط.", 4800, 25.0, 15.0),
        BatteryOption("graphene_pro", "خلية الجرافين المتجانسة", "سعة 5800 مللي أمبير، فائقة الكثافة وتتحمل الألعاب الطويلة بدون تسريب طاقة.", 5800, 55.0, 30.0),
        BatteryOption("nuclear_quantum", "مكثف الطاقة الكمي المستقبلي", "سعة 8000 مللي أمبير، طاقة أبدية مدعومة بهندسة نووية كمية مصغرة.", 8000, 120.0, 50.0)
    )

    val Levels = listOf(
        LevelChallenge(
            levelNumber = 1,
            titleAr = "المستوى 1: الانطلاقة المتواضعة",
            subtitleAr = "استهداف الفئات الشعبية ذات الميزانية الاقتصادية البسيطة",
            targetSegmentAr = "المستهلك الاقتصادي (يبحث عن هاتف رخيص ومقبول)",
            budgetGiven = 150000.0,
            descriptionAr = "أنت الآن تبدأ شركتك الناشئة. الجمهور يبحث عن أرخص هاتف ممكن لأداء المهام اليومية. حاول ألا تبالغ في الترقيات حتى يظل السعر في متناول المستهلك. والمنافسون هنا يعرضون أسعارًا منخفضة للغاية وعليك كسر السعر للحصول على حصة سوقية جيدة.",
            requiredUnitsSold = 4500,
            requiredNetProfit = 30000.0,
            ratingRequirement = 2.8f,
            difficultyMultiplier = 0.9,
            idealFocus = "BUDGET",
            competitor1 = CompetitorModel("شركة رخيصة تك", "جوال إيزي 1", 15.0, 20.0, 99.0),
            competitor2 = CompetitorModel("Pear Mobile", "Pear Eco Lite", 25.0, 25.0, 149.0)
        ),
        LevelChallenge(
            levelNumber = 2,
            titleAr = "المستوى 2: التميز في الفئة المتوسطة",
            subtitleAr = "هواتف متوازنة الأداء مع ميزات ذات قيمة ممتازة",
            targetSegmentAr = "مستخدمو الفئة الاقتصادية-المتوسطة والشباب في المدارس",
            budgetGiven = 350000.0,
            descriptionAr = "في هذا المستوى، يحتاج الجمهور معالجاً سداسياً وكاميرا 48 ميجا على الأقل لتشغيل برامج التواصل وصناعة المحتوى بشكل سلس. ستحتاج لطرح جهاز بسعر متوازن لا يتجاوز 350 دولاراً ومقنعاً بالمواصفات الأساسية، والمنافسة هنا شرسة للغاية.",
            requiredUnitsSold = 10000,
            requiredNetProfit = 120000.0,
            ratingRequirement = 3.5f,
            difficultyMultiplier = 1.2,
            idealFocus = "BALANCED",
            competitor1 = CompetitorModel("جالاكسي تيك", "سام ميديوم أيه 3", 45.0, 48.0, 249.0),
            competitor2 = CompetitorModel("ريد زون", "ريد كول سيلفر", 52.0, 42.0, 299.0)
        ),
        LevelChallenge(
            levelNumber = 3,
            titleAr = "المستوى 3: منافسة الكبار (الفلاجشيب الكلاسيكي)",
            subtitleAr = "تقديم هواتف رائدة بمواد فخمة وكاميرات احترافية متكاملة",
            targetSegmentAr = "المحترفون، رجال الأعمال، ومحبو الفخامة العالية",
            budgetGiven = 850000.0,
            descriptionAr = "هنا تبدأ ساحة العمالقة. الزبائن لن يشتروا هاتفك إلا إذا كان مصنعاً من هيكل التيتانيوم والكاميرا لا تقل عن 108 ميجا للحصول على اللقطات الاحترافية الخارقة. قم بإدراج تقنية 5G بشكل أساسي ليكون جذاباً للمدى البعيد. تسعيره يجب أن يكون عادلاً ومنافساً لعمالقة الهواتف.",
            requiredUnitsSold = 25000,
            requiredNetProfit = 450000.0,
            ratingRequirement = 4.1f,
            difficultyMultiplier = 1.7,
            idealFocus = "CAMERA",
            competitor1 = CompetitorModel("Pear Corp", "Pear Phone 16", 80.0, 85.0, 899.0),
            competitor2 = CompetitorModel("Samsong", "Samsong Alpha S30", 85.0, 82.0, 949.0)
        ),
        LevelChallenge(
            levelNumber = 4,
            titleAr = "المستوى 4: سيادة عوالم المستقبل",
            subtitleAr = "إطلاق أقصى ما أوتيت التكنولوجيا من طاقة ومرونة وتصميم",
            targetSegmentAr = "عشاق الألعاب الثقيلة، المهندسون، والأثرياء المهتمين بالمستقبل",
            budgetGiven = 1800000.0,
            descriptionAr = "هذا هو الملعب النهائي للجنون الإيجابي والتقنية الجامحة. يبحث العملاء عن كل تقنية مستقبلية: شاشات ثنائية تلتف وتثنى، شريحة ذكاء اصطناعي كمومية، مستشعرات كاميرا الفضاء 200 ميجا بكسل، والشحن اللحظي. لا تخف من وضع أسعار ناطحة للسحاب طالما الميزات ترقى للخيال العالي!",
            requiredUnitsSold = 60000,
            requiredNetProfit = 1500000.0,
            ratingRequirement = 4.5f,
            difficultyMultiplier = 2.4,
            idealFocus = "FUTURISTIC",
            competitor1 = CompetitorModel("Pear Corp", "Pear Infinity Foldable", 95.0, 92.0, 1899.0),
            competitor2 = CompetitorModel("Samsong Space", "Samsong Omega Fold Z", 94.0, 98.0, 1999.0)
        )
    )
}
