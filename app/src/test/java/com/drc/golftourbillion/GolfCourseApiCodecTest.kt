package com.drc.golftourbillion

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Synthetic fixtures, not real-world course data. No credentials or network calls. */
class GolfCourseApiCodecTest {
    private val id = "7k2m9qb4"

    private fun fixture(count: Int = 9, hole: JSONObject = JSONObject()
        .put("par", 4).put("yardage", 484).put("handicap", 2)): JSONObject {
        val tee = JSONObject().put("tee_name", "Test blue").put("number_of_holes", count)
            .put("course_rating", 72.5).put("slope_rating", 120)
            .put("holes", JSONArray().apply { repeat(count) { put(JSONObject(hole.toString())) } })
        return JSONObject().put("id", id).put("club_name", "Test club")
            .put("course_name", "Test course").put("tees", JSONObject().put("male", JSONArray().put(tee)))
    }

    @Test fun searchAcceptsCoursesWithoutCoordinates() {
        val raw = JSONObject().put("courses", JSONArray().put(JSONObject()
            .put("id", id.uppercase()).put("club_name", "Test club").put("course_name", "Test course")
            .put("location", JSONObject().put("city", "Test city").put("country", "Australia"))))
        val result = GolfCourseApiCodec.search(raw.toString()).single()
        assertEquals(id, result.id)
        assertEquals("Test club — Test course", result.name)
        assertEquals("Test city, Australia", result.location)
    }

    @Test fun usesOpaqueIdsNotLegacyNumericIds() {
        assertTrue(GolfCourseApiCodec.validId(id))
        assertTrue(GolfCourseApiCodec.validId(id.uppercase()))
        assertFalse(GolfCourseApiCodec.validId("123"))
        assertFalse(GolfCourseApiCodec.validId("../evil"))
        assertFalse(GolfCourseApiCodec.validId("7i2m9qb4"))
    }

    @Test fun convertsPublishedYardsToMetresAndKeepsOriginalYards() {
        val profile = GolfCourseApiCodec.tees(fixture().toString(), id).single()
        assertEquals(9, profile.holes.size)
        assertEquals(443, profile.holes.first().metres)
        assertEquals(484, profile.holes.first().yardage)
        assertEquals(2, profile.holes.first().handicap)
        assertEquals(36, profile.totalPar)
        assertEquals(72.5, profile.courseRating!!, 0.001)
        assertEquals(120, profile.slopeRating)
    }

    @Test fun publishedMetresOverrideYardConversion() {
        val raw = fixture(hole = JSONObject().put("par", 4).put("yardage", 484).put("meters", 440))
        assertEquals(440, GolfCourseApiCodec.tees(raw.toString(), id).single().holes.first().metres)
    }

    @Test fun supportsEighteenHolesAndAllTeeGroups() {
        val raw = fixture(18)
        val groups = raw.getJSONObject("tees")
        groups.put("female", JSONArray().put(groups.getJSONArray("male").getJSONObject(0)))
        val result = GolfCourseApiCodec.tees(raw.toString(), id)
        assertEquals(setOf("male", "female"), result.map { it.teeGroup }.toSet())
        assertTrue(result.all { it.holes.size == 18 })
        assertEquals(2, result.map { it.id }.toSet().size)
    }

    @Test fun incompleteTeeDoesNotInventDistances() {
        val raw = fixture(hole = JSONObject().put("par", 4))
        assertThrows(IllegalArgumentException::class.java) { GolfCourseApiCodec.tees(raw.toString(), id) }
    }

    @Test fun invalidParAndDeclaredHoleCountAreRejected() {
        val badPar = fixture(hole = JSONObject().put("par", 0).put("yardage", 400))
        assertThrows(IllegalArgumentException::class.java) { GolfCourseApiCodec.tees(badPar.toString(), id) }
        val badCount = fixture()
        badCount.getJSONObject("tees").getJSONArray("male").getJSONObject(0).put("number_of_holes", 18)
        assertThrows(IllegalArgumentException::class.java) { GolfCourseApiCodec.tees(badCount.toString(), id) }
    }

    @Test fun differentCourseCannotReplaceRequestedCourse() {
        val raw = fixture().put("id", "7k2m9qb5")
        assertThrows(IllegalArgumentException::class.java) { GolfCourseApiCodec.tees(raw.toString(), id) }
    }

    @Test fun offlineScorecardRoundTripsWithoutLosingTeeOrHoles() {
        val original = GolfCourseApiCodec.tees(fixture().toString(), id).single()
        assertEquals(original, GolfCourseApiCodec.decode(GolfCourseApiCodec.encode(original)))
    }

    @Test fun corruptOfflineHoleNumbersAreRejected() {
        val original = GolfCourseApiCodec.tees(fixture().toString(), id).single()
        val stored = GolfCourseApiCodec.encode(original)
        stored.getJSONArray("holes").getJSONObject(0).put("number", 9)
        assertThrows(IllegalArgumentException::class.java) { GolfCourseApiCodec.decode(stored) }
    }

    @Test fun handlesAuthAndFreePlanLimitWithoutOfferingPaidUpgrade() {
        assertTrue(courseApiError(401).contains("key"))
        assertTrue(courseApiError(403).contains("Activate"))
        assertTrue(courseApiError(429).contains("saved course"))
        assertFalse(courseApiError(429).contains("upgrade", ignoreCase = true))
        assertTrue(courseApiError(500).contains("saved courses"))
    }
}
