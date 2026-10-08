package com.drc.golftourbillion

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CourseGold = Color(0xFFD4AF37)
private val CourseWhite = Color(0xFFF5F5F5)
private val CourseMuted = Color(0xFFB9C3BE)
private val CoursePanel = Color(0xFF0B2A1D)

@Composable
fun CourseBrowser(
    repository: CourseRepository,
    onChoose: (GolfCourseData) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val saved = remember { repository.savedCourses() }
    var keyReady by remember { mutableStateOf(repository.hasKey()) }
    var settings by remember { mutableStateOf(false) }
    // Not rememberSaveable: never put the plaintext key in a saved instance-state bundle.
    var keyInput by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GolfCourseSummary>>(emptyList()) }
    var tees by remember { mutableStateOf<List<GolfCourseData>>(emptyList()) }

    fun runRequest(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = ""
        scope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                message = error.message ?: "Cannot load courses. Try a saved course."
            } finally {
                busy = false
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("COURSES", color = CourseGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("BACK", color = CourseGold) }
        }
        Text("GolfCourseAPI • free plan", color = CourseWhite)
        Text("Up to 35 API requests/day per account. Search and course downloads each use a request. " +
            "Cached data avoids repeat requests for 24 hours; saved tee scorecards work offline.",
            color = CourseMuted, fontSize = 12.sp)
        Text("Scorecard lengths only — not live distances to the green. This service has no hole GPS.",
            color = CourseGold, fontSize = 12.sp)
        OutlinedButton(onClick = { settings = true }, enabled = !busy) {
            Text(if (keyReady) "COURSE API SETTINGS" else "ADD FREE API KEY", color = CourseGold)
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it.take(120) },
            label = { Text("Course or golf club name") },
            singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CourseWhite, unfocusedTextColor = CourseWhite,
                focusedBorderColor = CourseGold, unfocusedLabelColor = CourseMuted)
        )
        Row {
            Checkbox(checked = refresh, onCheckedChange = { refresh = it }, enabled = !busy,
                colors = CheckboxDefaults.colors(checkedColor = CourseGold))
            Text("Refresh cached data (uses requests)", color = CourseMuted,
                modifier = Modifier.padding(top = 14.dp), fontSize = 12.sp)
        }
        Button(
            enabled = !busy && keyReady && query.trim().length >= 2,
            onClick = {
                val term = query
                val force = refresh
                runRequest {
                    val found = withContext(Dispatchers.IO) { repository.search(term, force) }
                    results = found
                    tees = emptyList()
                    if (found.isEmpty()) message = "No courses found. Try a different club name."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CourseGold, contentColor = CoursePanel)
        ) { Text(if (busy) "LOADING…" else "SEARCH") }
        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CourseGold)
        if (message.isNotEmpty()) Text(message, color = CourseGold)
        if (tees.isNotEmpty()) {
            Text("CHOOSE A TEE", color = CourseGold, fontWeight = FontWeight.Bold)
            tees.forEach { profile ->
                CourseChoice(profile.name, "${profile.teeLabel} • ${profile.holes.size} holes • " +
                    "Par ${profile.totalPar} • ${profile.totalMetres} m",
                    enabled = !busy) { onChoose(profile) }
            }
        }
        if (results.isNotEmpty()) {
            Text("SEARCH RESULTS", color = CourseGold, fontWeight = FontWeight.Bold)
            results.forEach { result ->
                CourseChoice(result.name, result.location.ifBlank { "Location not supplied" },
                    enabled = !busy) {
                    val force = refresh
                    runRequest {
                        tees = withContext(Dispatchers.IO) { repository.tees(result.id, force) }
                        message = "Tee scorecards loaded. Choose a tee to save it offline."
                    }
                }
            }
        }
        Text("SAVED ON THIS DEVICE", color = CourseGold, fontWeight = FontWeight.Bold)
        if (saved.isEmpty()) Text("Choose a course and tee to save its scorecard here.", color = CourseMuted)
        saved.forEach { profile ->
            CourseChoice(profile.name, "${profile.teeLabel} • ${profile.holes.size} holes • Offline",
                enabled = !busy) { onChoose(profile) }
        }
    }

    if (settings) AlertDialog(
        onDismissRequest = { if (!busy) { settings = false; keyInput = "" } },
        title = { Text("Free course API key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Create and activate a free account at GolfCourseAPI.com, then enter your personal " +
                    "API key here. No paid subscription or hosting is required. Never post it in GitHub or chat.")
                TextButton(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://golfcourseapi.com/"))) }
                        .onFailure { message = "Open golfcourseapi.com in your browser." }
                }) { Text("OPEN GOLFCOURSEAPI.COM") }
                OutlinedTextField(value = keyInput, onValueChange = { keyInput = it },
                    label = { Text(if (keyReady) "Replacement API key" else "API key") },
                    enabled = !busy, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false))
                Text("Encrypted with Android Keystore; excluded from backups. The key is only sent to " +
                    "api.golfcourseapi.com over HTTPS. Removing it does not remove saved courses.",
                    fontSize = 12.sp)
                if (message.isNotEmpty()) Text(message, color = MaterialTheme.colorScheme.error)
                if (keyReady) TextButton(enabled = !busy, onClick = {
                    runRequest {
                        withContext(Dispatchers.IO) { repository.removeKey() }
                        keyReady = false
                        keyInput = ""
                        settings = false
                        message = "API key removed. Saved courses are still available offline."
                    }
                }) { Text("REMOVE API KEY") }
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && keyInput.isNotBlank(), onClick = {
                val input = keyInput
                runRequest {
                    withContext(Dispatchers.IO) { repository.saveKey(input) }
                    keyReady = true
                    keyInput = ""
                    settings = false
                    message = "Key saved securely. Tap Search to check your connection."
                }
            }) { Text("SAVE KEY") }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = { settings = false; keyInput = "" }) { Text("CANCEL") }
        }
    )
}

@Composable
private fun CourseChoice(title: String, detail: String, enabled: Boolean, onChoose: () -> Unit) {
    OutlinedButton(onClick = onChoose, enabled = enabled, modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, CourseGold)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = CourseWhite, fontWeight = FontWeight.Bold)
            Text(detail, color = CourseMuted, fontSize = 12.sp)
        }
    }
}
