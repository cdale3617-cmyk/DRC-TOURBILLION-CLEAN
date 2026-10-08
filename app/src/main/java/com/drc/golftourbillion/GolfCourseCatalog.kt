package com.drc.golftourbillion

data class GolfHoleData(
    val number: Int,
    val par: Int,
    val metres: Int,
    val yardage: Int? = null,
    val handicap: Int? = null
)

data class GolfCourseData(
    val id: String,
    val name: String,
    val holes: List<GolfHoleData>,
    val teeName: String = "",
    val teeGroup: String = "",
    val courseRating: Double? = null,
    val slopeRating: Int? = null
) {
    val teeLabel: String
        get() = listOf(teeName, teeGroup).filter { it.isNotBlank() }.joinToString(" • ")

    val totalPar: Int
        get() = holes.sumOf { it.par }

    val totalMetres: Int
        get() = holes.sumOf { it.metres }

    fun hole(number: Int): GolfHoleData? =
        holes.firstOrNull { it.number == number }
}

object GolfCourseCatalog {

    // Legacy manually entered data, retained for existing rounds.
    // This entry is not a verified API scorecard.
    val courses: List<GolfCourseData> = listOf(
        GolfCourseData(
            id = "mercure-capricorn-resort",
            name = "Mercure Capricorn Resort",
            holes = listOf(
                GolfHoleData(1, 5, 485),
                GolfHoleData(2, 4, 365),
                GolfHoleData(3, 3, 155),
                GolfHoleData(4, 4, 345),
                GolfHoleData(5, 4, 360),
                GolfHoleData(6, 5, 475),
                GolfHoleData(7, 3, 145),
                GolfHoleData(8, 4, 330),
                GolfHoleData(9, 4, 350),
                GolfHoleData(10, 4, 370),
                GolfHoleData(11, 5, 490),
                GolfHoleData(12, 3, 150),
                GolfHoleData(13, 4, 355),
                GolfHoleData(14, 4, 340),
                GolfHoleData(15, 5, 470),
                GolfHoleData(16, 3, 140),
                GolfHoleData(17, 4, 365),
                GolfHoleData(18, 4, 355)
            )
        )
    )

    fun findById(id: String): GolfCourseData? =
        courses.firstOrNull { it.id == id }

    fun findByName(name: String): GolfCourseData? =
        courses.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
