package com.drc.golftourbillion

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.roundToInt

private val Gold get() = AppPalette.accent
private val Green get() = AppPalette.background
private val Panel get() = AppPalette.panel
private val Red get() = AppPalette.action
private val White get() = AppPalette.text
private val Muted get() = AppPalette.muted
private data class Club(val name:String,val carry:Int,val loft:String)
private data class Hole(val par:Int,val metres:Int,val shots:Int,val putts:Int,val fairway:Boolean,val gir:Boolean)
private val pars=listOf(5,4,3,4,4,5,3,4,4,4,5,3,4,4,5,3,4,4)
private val lengths=listOf(485,365,155,345,360,475,145,330,350,370,490,150,355,340,470,140,365,355)
private val defaultBag=listOf(Club("Driver",210,"10.5"),Club("3 Wood",195,"15"),Club("5 Wood",180,"18"),Club("4 Hybrid",165,"22"),Club("5 Iron",150,"25"),Club("6 Iron",140,"28"),Club("7 Iron",130,"32"),Club("8 Iron",120,"36"),Club("9 Iron",108,"41"),Club("Pitching Wedge",95,"46"),Club("Gap Wedge",82,"50"),Club("Sand Wedge",68,"56"),Club("Lob Wedge",52,"60"),Club("Putter",0,"3"))
private fun prefs(c:Context)=c.getSharedPreferences("tourbillion",Context.MODE_PRIVATE)
private fun distance(m:Int,y:Boolean)=if(y)(m*1.09361).roundToInt() else m
private fun yardsToMetres(y:Int)=(y/1.09361).roundToInt()
private fun loadBag(c:Context):List<Club>{
 val raw=prefs(c).getString("bag",null)?:return defaultBag
 return raw.split("|").mapNotNull{val p=it.split("~");if(p.size==3)Club(p[0],p[1].toIntOrNull()?:0,p[2])else null}.ifEmpty{defaultBag}
}
private fun saveBag(c:Context,b:List<Club>){prefs(c).edit().putString("bag",b.joinToString("|"){it.name+"~"+it.carry+"~"+it.loft}).apply()}
private fun loadScores(c:Context):List<Hole>{
 val raw=prefs(c).getString("scores",null)?:
