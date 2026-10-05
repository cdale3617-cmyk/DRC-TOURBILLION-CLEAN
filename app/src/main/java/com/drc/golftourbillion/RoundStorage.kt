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
            decodeRounds(raw)
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, round: SavedRound) {
        val rounds = load(context)
            .filterNot { it.id == round.id }
            .plus(round)
            .sortedByDescending { it.playedAtMillis }

        writeRounds(context, rounds)
    }

    fun delete(context: Context, roundId: String) {
        val rounds = load(context).filterNot { it.id == roundId }
        writeRounds(context, rounds)
    }

    private fun writeRounds(context: Context, rounds: List<SavedRound>) {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        preferences.edit()
            .putString(ROUNDS_KEY, encodeRounds(rounds))
            .apply()
    }

    private fun encodeRounds(rounds: List<SavedRound>): String {
        val roundsArray = JSONArray()

        rounds.forEach { round ->
            val holesArray = JSONArray()

            round.holes.forEach { hole ->
                holesArray.put(
                    JSONObject()
                        .put("holeNumber", hole.holeNumber)
                        .put("par", hole.par)
                        .put("strokes", hole.strokes)
                        .put("putts", hole.putts)
                        .put("fairwayHit", hole.fairwayHit)
                        .put("greenInRegulation", hole.greenInRegulation)
                )
            }

            roundsArray.put(
                JSONObject()
                    .put("id", round.id)
                    .put("playedAtMillis", round.playedAtMillis)
                    .put("playerName", round.playerName)
                    .put("courseName", round.courseName)
                    .put("holes", holesArray)
            )
        }

        return roundsArray.toString()
    }

    private fun decodeRounds(raw: String): List<SavedRound> {
        val roundsArray = JSONArray(raw)
        val rounds = mutableListOf<SavedRound>()

        for (roundIndex in 0 until roundsArray.length()) {
            val roundObject =
                roundsArray.optJSONObject(roundIndex) ?: continue
            val holesArray =
                roundObject.optJSONArray("holes") ?: JSONArray()
            val holes = mutableListOf<SavedHoleResult>()

            for (holeIndex in 0 until holesArray.length()) {
                val holeObject =
                    holesArray.optJSONObject(holeIndex) ?: continue

                holes.add(
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
                        greenInRegulation = holeObject.optBoolean(
                            "greenInRegulation",
                            false
                        )
                    )
                )
            }

            rounds.add(
                SavedRound(
                    id = roundObject.optString("id", ""),
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

        return rounds.sortedByDescending { it.playedAtMillis }
    }
}
