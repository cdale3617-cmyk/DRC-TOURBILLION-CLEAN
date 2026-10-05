package com.drc.golftourbillion

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedHoleResult(
    val holeNumber: Int,
    val par: Int,
    val strokes: Int,
    val putts: Int,
    val fairwayHit: Boolean,
    val greenInRegulation: Boolean
)

data class SavedRound(
    val id: String,
    val playedAtMillis: Long,
    val playerName: String,
    val courseName: String,
    val holes: List<SavedHoleResult>
) {
    val totalStrokes: Int
        get() = holes.sumOf { it.strokes }

    val totalPar: Int
        get() = holes.sumOf { it.par }

    val totalPutts: Int
        get() = holes.sumOf { it.putts }
}

object RoundStorage {

    private const val PREFERENCES_NAME = "drc_tourbillion_round_history"
    private const val ROUNDS_KEY = "saved_rounds"

    fun load(context: Context): List<SavedRound> {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        val raw = preferences.getString(ROUNDS_KEY, "[]") ?: "[]"

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val roundObject = array.optJSONObject(index) ?: continue
                    val holesArray =
                        roundObject.optJSONArray("holes") ?: JSONArray()

                    val holes = buildList {
                        for (holeIndex in 0 until holesArray.length()) {
                            val holeObject =
                                holesArray.optJSONObject(holeIndex) ?: continue

                            add(
                                SavedHoleResult(
                                    holeNumber = holeObject.optInt(
                                        "holeNumber",
                                        holeIndex + 1
                                    ),
                                    par = holeObject.optInt("par", 4),
                                    strokes = holeObject.optInt("strokes", 0),
                                    putts = holeObject.optInt("putts", 0),
                                    fairwayHit = holeObject.optBoolean(
                                        "fairwayHit",
                                        false
                                    ),
                                    greenInRegulation =
                                        holeObject.optBoolean(
                                            "greenInRegulation",
                                            false
                                        )
                                )
                            )
                        }
                    }

                    add(
                        SavedRound(
                            id = roundObject.optString("id"),
                            playedAtMillis =
                                roundObject.optLong("playedAtMillis", 0L),
                            playerName =
                                roundObject.optString("playerName", "Player"),
                            courseName =
                                roundObject.optString("courseName", "Course"),
                            holes = holes
                        )
                    )
                }
            }.sortedByDescending { it.playedAtMillis }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, round: SavedRound) {
        val updatedRounds = load(context)
            .filterNot { it.id == round.id }
            .plus(round)
            .sortedByDescending { it.playedAtMillis }

        val roundsArray = JSONArray()

        updatedRounds.forEach { savedRound ->
            val holesArray = JSONArray()

            savedRound.holes.forEach { hole ->
                holesArray.put(
                    JSONObject()
                        .put("holeNumber", hole.holeNumber)
                        .put("par", hole.par)
                        .put("strokes", hole.strokes)
                        .put("putts", hole.putts)
                        .put("fairwayHit", hole.fairwayHit)
                        .put(
                            "greenInRegulation",
                            hole.greenInRegulation
                        )
                )
            }

            roundsArray.put(
                JSONObject()
                    .put("id", savedRound.id)
                    .put("playedAtMillis", savedRound.playedAtMillis)
                    .put("playerName", savedRound.playerName)
                    .put("courseName", savedRound.courseName)
                    .put("holes", holesArray)
            )
        }

        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putString(ROUNDS_KEY, roundsArray.toString())
            .apply()
    }

    fun delete(context: Context, roundId: String) {
        val remainingRounds = load(context).filterNot { it.id == roundId }
        val roundsArray = JSONArray()

        remainingRounds.forEach { savedRound ->
            val holesArray = JSONArray()

            savedRound.holes.forEach { hole ->
                holesArray.put(
                    JSONObject()
                        .put("holeNumber", hole.holeNumber)
                        .put("par", hole.par)
                        .put("strokes", hole.strokes)
                        .put("putts", hole.putts)
                        .put("fairwayHit", hole.fairwayHit)
                        .put(
                            "greenInReg
