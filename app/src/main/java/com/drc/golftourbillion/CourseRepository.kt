package com.drc.golftourbillion

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Per-device user credential; never a shared/build-time API key. */
class CourseApiKeyStore(context: Context) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "course-api-key.json"))
    private val alias = "drc_golfcourseapi_key_v1"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    fun save(value: String) {
        val token = value.trim()
        require(token.isNotEmpty() && token.length <= 4096 && token.none { it.isWhitespace() }) {
            "Enter the API key alone, without a Bearer or Key prefix."
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key())
        }
        val record = JSONObject()
            .put("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .put("data", Base64.encodeToString(cipher.doFinal(token.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP))
        atomicWrite(file, record.toString())
    }

    fun read(): String? = runCatching {
        val record = JSONObject(file.openRead().bufferedReader().use { it.readText() })
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val storedKey = store.getKey(alias, null) as? SecretKey ?: return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, storedKey,
                GCMParameterSpec(128, Base64.decode(record.getString("iv"), Base64.NO_WRAP)))
        }
        String(cipher.doFinal(Base64.decode(record.getString("data"), Base64.NO_WRAP)), Charsets.UTF_8)
    }.getOrNull()

    fun clear() {
        file.delete()
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            if (containsAlias(alias)) deleteEntry(alias)
        }
    }
}

private fun atomicWrite(file: AtomicFile, text: String) {
    val output = file.startWrite()
    try {
        output.write(text.toByteArray(Charsets.UTF_8))
        file.finishWrite(output)
    } catch (error: Exception) {
        file.failWrite(output)
        throw error
    }
}

class CourseRepository(context: Context) {
    private val root = File(context.filesDir, "course-data").apply { mkdirs() }
    private val credentials = CourseApiKeyStore(context)

    fun hasKey(): Boolean = !credentials.read().isNullOrBlank()
    fun saveKey(value: String) = credentials.save(value)
    fun removeKey() = credentials.clear()

    private fun file(name: String) = AtomicFile(File(root, "$name.json"))
    private fun read(name: String): String? =
        runCatching { file(name).openRead().bufferedReader().use { it.readText() } }.getOrNull()

    fun selected(): GolfCourseData? = read("selected")?.let {
        runCatching { GolfCourseApiCodec.decode(JSONObject(it)) }.getOrNull()
    }

    fun savedCourses(): List<GolfCourseData> = runCatching {
        val rows = JSONArray(read("saved") ?: "[]")
        (0 until rows.length()).mapNotNull { index ->
            runCatching { GolfCourseApiCodec.decode(rows.getJSONObject(index)) }.getOrNull()
        }
    }.getOrDefault(emptyList())

    fun select(course: GolfCourseData) {
        val profiles = savedCourses().filterNot { it.id == course.id } + course
        atomicWrite(file("saved"), JSONArray().apply {
            profiles.forEach { put(GolfCourseApiCodec.encode(it)) }
        }.toString())
        atomicWrite(file("selected"), GolfCourseApiCodec.encode(course).toString())
    }

    private fun request(path: String): String {
        val token = credentials.read() ?: throw IOException("Add your free API key in Course API settings.")
        // Fixed HTTPS origin, read-only endpoints, no redirects that could leak Authorization.
        val connection = URL("https://api.golfcourseapi.com/v1/$path").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status != 200) throw IOException(courseApiError(status))
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun cached(name: String, refresh: Boolean, path: String): String {
        val existing = read(name)?.let { runCatching { JSONObject(it) }.getOrNull() }
        val age = System.currentTimeMillis() - (existing?.optLong("savedAt") ?: 0L)
        if (!refresh && existing != null && age in 0..86_400_000L) {
            return existing.getString("payload")
        }
        val raw = try { request(path) } catch (error: IOException) {
            // Explicitly saved scorecards remain usable; don't hide auth or quota failures.
            throw IOException(error.message ?: "Cannot connect. Use a saved course while offline.", error)
        }
        // Do not store credentials. Only successful JSON responses are cached.
        JSONObject(raw)
        atomicWrite(file(name), JSONObject().put("savedAt", System.currentTimeMillis())
            .put("payload", raw).toString())
        return raw
    }

    fun search(query: String, refresh: Boolean = false): List<GolfCourseSummary> {
        val term = query.trim()
        require(term.length in 2..120) { "Enter between 2 and 120 characters to search." }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(term.lowercase().toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        val path = "search?search_query=${URLEncoder.encode(term, "UTF-8")}&fuzzy_match=true"
        return GolfCourseApiCodec.search(cached("search-$digest", refresh, path))
    }

    fun tees(id: String, refresh: Boolean = false): List<GolfCourseData> {
        require(GolfCourseApiCodec.validId(id)) { "Invalid course ID." }
        return GolfCourseApiCodec.tees(cached("detail-${id.lowercase()}", refresh, "courses/$id"), id)
    }
}
