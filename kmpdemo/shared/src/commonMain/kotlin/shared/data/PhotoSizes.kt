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
 * 背景颜色选项
 */
enum class BackgroundColor(val displayName: String, val colorValue: Long) {
    WHITE("白色", 0xFFFFFFFF),
    BLUE("蓝色", 0xFF2196F3.toLong()),
    RED("红色", 0xFFE53935.toLong()),
    LIGHT_BLUE("浅蓝色", 0xFF03A9F4.toLong()),
    DARK_BLUE("深蓝色", 0xFF1565C0.toLong()),
    DARK_RED("深红色", 0xFFC62828.toLong()),
    GRAY("浅灰色", 0xFFF5F5F5.toLong()),
    GRADIENT_BLUE("渐变蓝", 0xFF1976D2.toLong())
}

/**
 * 服装模板
 */
data class ClothingTemplate(
    val id: String,
    val name: String,
    val description: String,
    val thumbnailRes: String = ""
)

object ClothingTemplates {
    val templates = listOf(
        ClothingTemplate("suit_man", "男士西装", "正式商务西装"),
        ClothingTemplate("suit_woman", "女士西装", "职业女性西装"),
        ClothingTemplate("shirt_white", "白色衬衫", "经典白衬衫"),
        ClothingTemplate("shirt_blue", "蓝色衬衫", "蓝色商务衬衫"),
        ClothingTemplate("sweater", "毛衣", "休闲毛衣"),
        ClothingTemplate("tang", "中山装", "传统中山装")
    )
}
