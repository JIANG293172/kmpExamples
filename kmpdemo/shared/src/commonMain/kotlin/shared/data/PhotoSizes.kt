package shared.data

/**
 * 证件照尺寸分类
 */
enum class SizeCategory {
    STANDARD,    // 标准尺寸（一寸、二寸）
    VISA,        // 签证照
    EXAM,        // 考试照
    WORK,        // 工牌照
    CUSTOM       // 自定义
}

/**
 * 证件照尺寸数据类
 */
data class PhotoSize(
    val id: String,
    val name: String,           // 显示名称 "一寸照"
    val widthMm: Int,           // 宽度 mm
    val heightMm: Int,          // 高度 mm
    val widthPx: Int,           // 宽度 px
    val heightPx: Int,          // 高度 px
    val dpi: Int = 300,
    val category: SizeCategory, // 分类
    val description: String = "" // 描述
)

/**
 * 常用证件照尺寸
 */
object PhotoSizes {

    val oneInch = PhotoSize(
        id = "one_inch",
        name = "一寸照",
        widthMm = 25,
        heightMm = 35,
        widthPx = 295,
        heightPx = 413,
        category = SizeCategory.STANDARD,
        description = "常用尺寸 25×35mm"
    )

    val twoInch = PhotoSize(
        id = "two_inch",
        name = "二寸照",
        widthMm = 35,
        heightMm = 49,
        widthPx = 413,
        heightPx = 579,
        category = SizeCategory.STANDARD,
        description = "常用尺寸 35×49mm"
    )

    val smallOneInch = PhotoSize(
        id = "small_one_inch",
        name = "小一寸照",
        widthMm = 22,
        heightMm = 32,
        widthPx = 260,
        heightPx = 378,
        category = SizeCategory.STANDARD,
        description = "22×32mm"
    )

    // 签证照
    val visaUS = PhotoSize(
        id = "visa_us",
        name = "美国签证照",
        widthMm = 50,
        heightMm = 50,
        widthPx = 600,
        heightPx = 600,
        category = SizeCategory.VISA,
        description = "美国签证专用 50×50mm"
    )

    val visaUK = PhotoSize(
        id = "visa_uk",
        name = "英国签证照",
        widthMm = 35,
        heightMm = 45,
        widthPx = 413,
        heightPx = 531,
        category = SizeCategory.VISA,
        description = "英国签证专用 35×45mm"
    )

    val visaJapan = PhotoSize(
        id = "visa_japan",
        name = "日本签证照",
        widthMm = 45,
        heightMm = 45,
        widthPx = 531,
        heightPx = 531,
        category = SizeCategory.VISA,
        description = "日本签证专用 45×45mm"
    )

    val visaSchengen = PhotoSize(
        id = "visa_schengen",
        name = "申根签证照",
        widthMm = 35,
        heightMm = 45,
        widthPx = 413,
        heightPx = 531,
        category = SizeCategory.VISA,
        description = "申根签证通用 35×45mm"
    )

    // 考试照
    val examGaokao = PhotoSize(
        id = "exam_gaokao",
        name = "高考报名照",
        widthMm = 30,
        heightMm = 40,
        widthPx = 354,
        heightPx = 472,
        category = SizeCategory.EXAM,
        description = "高考报名专用"
    )

    val examGraduate = PhotoSize(
        id = "exam_graduate",
        name = "考研报名照",
        widthMm = 26,
        heightMm = 32,
        widthPx = 307,
        heightPx = 378,
        category = SizeCategory.EXAM,
        description = "研究生报名专用"
    )

    val examCivilService = PhotoSize(
        id = "exam_civil",
        name = "公务员报名照",
        widthMm = 35,
        heightMm = 45,
        widthPx = 413,
        heightPx = 531,
        category = SizeCategory.EXAM,
        description = "公务员考试报名专用"
    )

    // 工牌照
    val workGeneral = PhotoSize(
        id = "work_general",
        name = "普通工牌照",
        widthMm = 26,
        heightMm = 32,
        widthPx = 307,
        heightPx = 378,
        category = SizeCategory.WORK,
        description = "一般工作证使用"
    )

    val workRefined = PhotoSize(
        id = "work_refined",
        name = "精致工牌照",
        widthMm = 35,
        heightMm = 45,
        widthPx = 413,
        heightPx = 531,
        category = SizeCategory.WORK,
        description = "高端工作证使用"
    )

    // 获取所有标准尺寸
    val standardSizes = listOf(oneInch, smallOneInch, twoInch)

    // 获取所有签证尺寸
    val visaSizes = listOf(visaUS, visaUK, visaJapan, visaSchengen)

    // 获取所有考试尺寸
    val examSizes = listOf(examGaokao, examGraduate, examCivilService)

    // 获取所有工牌照尺寸
    val workSizes = listOf(workGeneral, workRefined)

    // 获取所有尺寸
    val allSizes = listOf(
        standardSizes,
        visaSizes,
        examSizes,
        workSizes
    ).flatten()

    // 按分类获取尺寸
    fun getByCategory(category: SizeCategory): List<PhotoSize> {
        return when (category) {
            SizeCategory.STANDARD -> standardSizes
            SizeCategory.VISA -> visaSizes
            SizeCategory.EXAM -> examSizes
            SizeCategory.WORK -> workSizes
            SizeCategory.CUSTOM -> emptyList()
        }
    }

    // 根据ID查找尺寸
    fun findById(id: String): PhotoSize? {
        return allSizes.find { it.id == id }
    }
}

/**
 * 背景类型
 */
enum class BackgroundType {
    SOLID,      // 纯色背景
    GRADIENT,   // 渐变背景
    TRANSPARENT // 透明背景
}

/**
 * 背景颜色选项 - 按需求文档标准色值
 */
enum class BackgroundColor(
    val displayName: String,
    val colorValue: Long,
    val rgb: String,
    val type: BackgroundType = BackgroundType.SOLID,
    val gradientColors: List<Long>? = null
) {
    // 纯色背景 - 官方标准色值
    WHITE("白色", 0xFFFFFFFF, "255,255,255", BackgroundType.SOLID),
    RED("红色", 0xFFFF0000, "255,0,0", BackgroundType.SOLID),
    BLUE("蓝色", 0xFF0099FF, "0,153,255", BackgroundType.SOLID),
    LIGHT_BLUE("浅蓝色", 0xFF66CCFF, "102,204,255", BackgroundType.SOLID),
    LIGHT_GRAY("浅灰色", 0xFFE6E6E6, "230,230,230", BackgroundType.SOLID),
    DARK_RED("深红色", 0xFFCC0000, "204,0,0", BackgroundType.SOLID),
    DARK_BLUE("深蓝色", 0xFF003399, "0,51,153", BackgroundType.SOLID),
    PALE_BLUE("苍蓝色", 0xFFBDD7EE, "189,215,238", BackgroundType.SOLID),

    // 渐变背景
    GRADIENT_BLUE("渐变蓝", 0xFF1976D2, "25,118,210", BackgroundType.GRADIENT, listOf(0xFF1976D2, 0xFF42A5F5)),
    GRADIENT_RED("渐变红", 0xFFD32F2F, "211,47,47", BackgroundType.GRADIENT, listOf(0xFFD32F2F, 0xFFEF5350)),
    GRADIENT_PURPLE("渐变紫", 0xFF7B1FA2, "123,31,162", BackgroundType.GRADIENT, listOf(0xFF7B1FA2, 0xFFBA68C8)),

    // 透明背景
    TRANSPARENT("透明", 0x00000000, "0,0,0,0", BackgroundType.TRANSPARENT);

    companion object {
        fun getSolidColors() = entries.filter { it.type == BackgroundType.SOLID }
        fun getGradientColors() = entries.filter { it.type == BackgroundType.GRADIENT }
        fun getAllColors() = entries
    }
}

/**
 * 裁剪模式
 */
enum class CropMode(val displayName: String, val aspectRatio: Float?) {
    AI_AUTO("AI智能裁剪", null),           // AI自动裁剪，保持原始比例
    RATIO_LOCKED("比例锁定", null),        // 根据证件照标准比例裁剪
    FREE("自由裁剪", null);                // 自由比例裁剪
    ;

    companion object {
        fun getById(id: String) = entries.find { it.name == id }
    }
}

/**
 * 标准证件照裁剪比例
 */
object CropRatios {
    // 证件照标准比例 (宽度/高度)
    val ONE_INCH = 25f / 35f       // 一寸照 25x35mm
    val TWO_INCH = 35f / 49f       // 二寸照 35x49mm
    val SMALL_ONE_INCH = 22f / 32f // 小一寸 22x32mm
    val VISA_SQUARE = 1f           // 签证方型 1:1
    val PASSPORT = 33f / 48f       // 护照 33x48mm
    val ID_CARD = 26f / 32f        // 身份证 26x32mm
    val DRIVING_LICENSE = 21f / 26f // 驾驶证 21x26mm

    // 根据尺寸ID获取比例
    fun getRatioForSize(sizeId: String): Float {
        return when (sizeId) {
            "one_inch" -> ONE_INCH
            "two_inch" -> TWO_INCH
            "small_one_inch" -> SMALL_ONE_INCH
            "visa_us", "visa_japan" -> VISA_SQUARE
            "visa_uk", "visa_schengen" -> 35f / 45f
            "passport" -> PASSPORT
            "id_card" -> ID_CARD
            "driving_license" -> DRIVING_LICENSE
            "exam_gaokao" -> 30f / 40f
            "exam_graduate" -> 26f / 32f
            "exam_civil" -> 35f / 45f
            "work_general" -> 26f / 32f
            "work_refined" -> 35f / 45f
            else -> ONE_INCH
        }
    }
}

/**
 * 服装类型
 */
enum class ClothingType {
    MEN,    // 男装
    WOMEN,  // 女装
    STUDENT // 学生装
}

/**
 * 服装模板 - 按需求文档分类
 */
data class ClothingTemplate(
    val id: String,
    val name: String,
    val description: String,
    val type: ClothingType,
    val isFree: Boolean = true,  // 是否免费
    val thumbnailRes: String = ""
)

object ClothingTemplates {
    // 免费模板
    private val freeTemplates = listOf(
        // 男装免费
        ClothingTemplate("shirt_white_man", "白色衬衫", "经典白衬衫", ClothingType.MEN, true),
        ClothingTemplate("shirt_blue_man", "浅蓝衬衫", "蓝色商务衬衫", ClothingType.MEN, true),
        // 女装免费
        ClothingTemplate("shirt_white_woman", "白色衬衫", "经典白衬衫", ClothingType.WOMEN, true),
        ClothingTemplate("shirt_blue_woman", "浅蓝衬衫", "蓝色商务衬衫", ClothingType.WOMEN, true),
        // 学生装免费
        ClothingTemplate("student_white", "学生白衬衫", "学院风白衬衫", ClothingType.STUDENT, true),
        ClothingTemplate("student_collar", "娃娃领衬衫", "甜美娃娃领", ClothingType.STUDENT, true)
    )

    // 付费/会员模板
    private val premiumTemplates = listOf(
        // 男装付费
        ClothingTemplate("suit_man", "男士西装", "正式商务西装", ClothingType.MEN, false),
        ClothingTemplate("suit_tie_man", "西装+领带", "领带正装", ClothingType.MEN, false),
        ClothingTemplate("zhongshan", "中山装", "传统中山装", ClothingType.MEN, false),
        // 女装付费
        ClothingTemplate("suit_woman", "女士西装", "职业女性西装", ClothingType.WOMEN, false),
        ClothingTemplate("suit_bow_woman", "西装+领结", "精致领结装", ClothingType.WOMEN, false),
        ClothingTemplate("turtleneck", "高领毛衣", "优雅高领", ClothingType.WOMEN, false),
        ClothingTemplate("blazer", "轻便西装", "休闲西装外套", ClothingType.WOMEN, false)
    )

    val allTemplates = freeTemplates + premiumTemplates

    val templates = allTemplates

    fun getByType(type: ClothingType) = allTemplates.filter { it.type == type }

    fun getFreeTemplates() = freeTemplates

    fun getPremiumTemplates() = premiumTemplates

    fun getById(id: String) = allTemplates.find { it.id == id }
}
