package com.drc.golftourbillion

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class GolfCourseSummary(
    val id: String,
    val name: String,
    val location: String
)

/** GolfCourseAPI v1: scorecards only. Course location is NOT a pin position. */
object GolfCourseApiCodec {
    private val courseId = Regex("^[0-9abcdefghjkmnpqrstvwxyz]{8}$", RegexOption.IGNORE_CASE)

    fun validId(id: String): Boolean = courseId.matches(id)

    private fun title(course: JSONObject): String {
        val club = course.optString("club_name").trim()
        val name = course.optString("course_name").trim()
        require(club.isNotEmpty() || name.isNotEmpty()) { "Course name is missing." }
        return when {
            name.isEmpty() || club.equals(name, ignoreCase = true) -> club
            club.isEmpty() -> name
            else -> "$club — $name"
        }
    }

    fun search(raw: String): List<GolfCourseSummary> {
        val rows = JSONObject(raw).getJSONArray("courses")
        return (0 until rows.length()).mapNotNull { index ->
            val row = rows.getJSONObject(index)
            val id = row.getString("id").lowercase()
            if (!validId(id)) return@mapNotNull null
            val location = row.optJSONObject("location")
            GolfCourseSummary(
                id, title(row),
                listOf("city", "state", "country").mapNotNull { field ->
                    location?.optString(field)?.trim()?.takeIf { it.isNotEmpty() }
                }.joinToString(", ")
            )
        }
    }

    fun tees(raw: String, requestedId: String): List<GolfCourseData> {
        require(validId(requestedId)) { "Invalid course ID." }
        val envelope = JSONObject(raw)
        val course = envelope.optJSONObject("course") ?: envelope
        require(course.getString("id").equals(requestedId, ignoreCase = true)) {
            "The service returned a different course."
        }
        val groups = course.getJSONObject("tees")
        val profiles = mutableListOf<GolfCourseData>()
        for (group in groups.keys().asSequence().sorted()) {
            val tees = groups.optJSONArray(group) ?: continue
            for (index in 0 until tees.length()) {
                val tee = tees.getJSONObject(index)
                val rows = tee.optJSONArray("holes") ?: continue
                if (rows.length() !in 1..36) continue
                val holes = mutableListOf<GolfHoleData>()
                for (holeIndex in 0 until rows.length()) {
                    val row = rows.getJSONObject(holeIndex)
                    val par = row.optInt("par", -1)
                    val yards = row.optInt("yardage", -1)
                    val metres = if (row.has("meters") && !row.isNull("meters")) {
                        row.optInt("meters", -1)
                    } else if (yards >= 0) (yards * 0.9144).roundToInt() else -1
                    if (par !in 1..7 || metres !in 0..2000 || yards > 2500) break
                    holes.add(GolfHoleData(
                        holeIndex + 1, par, metres, yards.takeIf { it >= 0 },
                        row.optInt("handicap", -1).takeIf { it > 0 }
                    ))
                }
                // Never invent a distance or par to fill an incomplete scorecard.
                if (holes.size != rows.length()) continue
                val declaredCount = tee.optInt("number_of_holes", holes.size)
                if (declaredCount != holes.size) continue
                profiles.add(GolfCourseData(
                    id = "golfcourseapi:${requestedId.lowercase()}:$group:$index",
                    name = title(course),
                    holes = holes,
                    teeName = tee.optString("tee_name", "Tee ${index + 1}"),
                    teeGroup = group,
                    courseRating = tee.optDouble("course_rating", Double.NaN).takeIf { it.isFinite() },
                    slopeRating = tee.optInt("slope_rating", -1).takeIf { it > 0 }
                ))
            }
        }
        require(profiles.isNotEmpty()) { "This course has no complete tee scorecard available." }
        return profiles
    }

    fun encode(course: GolfCourseData): JSONObject = JSONObject()
        .put("id", course.id).put("name", course.name)
        .put("teeName", course.teeName).put("teeGroup", course.teeGroup)
        .put("courseRating", course.courseRating ?: JSONObject.NULL)
        .put("slopeRating", course.slopeRating ?: JSONObject.NULL)
        .put("holes", JSONArray().apply {
            course.holes.forEach { hole ->
                put(JSONObject().put("number", hole.number).put("par", hole.par)
                    .put("metres", hole.metres).put("yardage", hole.yardage ?: JSONObject.NULL)
                    .put("handicap", hole.handicap ?: JSONObject.NULL))
            }
        })

    fun decode(raw: JSONObject): GolfCourseData {
        val rows = raw.getJSONArray("holes")
        require(rows.length() in 1..36)
        val holes = (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            val number = row.getInt("number")
            val par = row.getInt("par")
            val metres = row.getInt("metres")
            require(number == index + 1 && par in 1..7 && metres in 0..2000)
            GolfHoleData(number, par, metres,
                row.optInt("yardage", -1).takeIf { it >= 0 },
                row.optInt("handicap", -1).takeIf { it > 0 })
        }
        return GolfCourseData(
            raw.getString("id"), raw.getString("name"), holes,
            raw.optString("teeName"), raw.optString("teeGroup"),
            raw.optDouble("courseRating", Double.NaN).takeIf { it.isFinite() },
            raw.optInt("slopeRating", -1).takeIf { it > 0 }
        )
    }
}

fun courseApiError(status: Int): String = when (status) {
    401 -> "The API key is invalid. Update it in Course API settings."
    403 -> "Activate your free GolfCourseAPI account, then try again."
    404 -> "This course is no longer available from the service."
    429 -> "The API request limit has been reached. Use a saved course and try again later."
    else -> "The course service is unavailable (HTTP $status). Your saved courses still work."
}
