package com.drc.golftourbillion
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Gold=Color(0xFFD4AF37); private val Green=Color(0xFF071C14); private val Panel=Color(0xFF0B2A1D); private val Red=Color(0xFF9E1B1B)
private val clubs=mutableStateListOf("Driver 210m 10.5°","3 Wood 190m 15°","5 Wood 175m 18°","7 Iron 135m 32°","PW 100m 46°","SW 75m 56°","Putter")
@Composable fun TourbillionApp(){
 var page by remember{mutableStateOf("HOME")}; var hole by remember{mutableIntStateOf(1)}; val scores=remember{mutableStateListOf<Int>().apply{repeat(18){add(0)}}}
 Scaffold(containerColor=Green,bottomBar={Row(Modifier.fillMaxWidth().navigationBarsPadding(),Arrangement.SpaceEvenly){listOf("HOME","CADDIE","BAG","LAB","SCORE","MORE").forEach{TextButton({page=it}){Text(it,color=Gold,fontSize=9.sp)}}}}){p->
  Box(Modifier.padding(p).fillMaxSize()){when(page){
   "HOME"->Home{page="CADDIE"};"CADDIE"->Caddie(hole,{if(hole>1)hole--},{if(hole<18)hole++})
   "BAG"->ListScreen("MY BAG",clubs);"LAB"->Lab();"SCORE"->Score(hole,scores,{hole=it})
   else->ListScreen("MORE",listOf("Strokes Gained","Fusion","Round Overview","Round History","Pre-Round","Club Equipment","Records","Course Information","Settings","How To"))
  }}
 }
}
@Composable private fun Header(t:String){Column{Text("DRC",color=Gold,fontWeight=FontWeight.Bold);Text(t,color=Gold,fontSize=27.sp,fontWeight=FontWeight.Black);HorizontalDivider(color=Gold.copy(.5f));Spacer(Modifier.height(12.dp))}}
@Composable private fun Panel(t:String,b:String){Card(Modifier.fillMaxWidth().padding(vertical=5.dp),colors=CardDefaults.cardColors(containerColor=Panel),border=BorderStroke(1.dp,Gold),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(14.dp)){Text(t,color=Gold,fontWeight=FontWeight.Bold);Text(b,color=Color.White)}}}
@Composable private fun Home(start:()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)){Header("GOLF TOURBILLION");Panel("CADDIE ENGINE","Live GPS • Weather • Wind • Scoring");Button(start,Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Red)){Text("START CADDIE ENGINE")};Panel("QUICK START","18-hole round");Panel("GOLF LAB","Analytical Frameworks And Diagnostic Suite")}}
@Composable private fun Caddie(h:Int,prev:()->Unit,next:()->Unit){
 val ctx=LocalContext.current; val scope=rememberCoroutineScope(); var fix by remember{mutableStateOf<LiveFix?>(null)};var wx by remember{mutableStateOf<LiveWeather?>(null)}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)){Header("LIVE HOLE • $h")
  Button({if(ActivityCompat.checkSelfPermission(ctx,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(ctx as Activity,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),7)}else{scope.launch{fix=LiveServices.lastLocation(ctx);wx=withContext(Dispatchers.IO){fix?.let{LiveServices.weather(it)}}}}}){Text("REFRESH LIVE GPS + WEATHER")}
  Panel("GPS",fix?.let{"%.6f, %.6f • ±%.0fm".format(it.lat,it.lon,it.accuracy)}?:"Permission/position required")
  Panel("WEATHER",wx?.let{"${it.temp}°C • Wind ${it.wind} km/h • ${it.direction}°"}?:"Refresh after GPS fix")
  Panel("DISTANCES","Front —   Middle —   Back —");Panel("CADDIE","Advice only • select target and club");Panel("WIND","L→R / R→L")
  Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(8.dp)){OutlinedButton(prev,Modifier.weight(1f)){Text("PREV")};Button(next,Modifier.weight(1f)){Text("NEXT")}}
 }}
@Composable private fun ListScreen(title:String,items:List<String>){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)){Header(title);items.forEach{Panel(it,"Open")}}}
@Composable private fun Lab(){val ctx=LocalContext.current;Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)){Header("Analytical Frameworks And Diagnostic Suite")
 Button({LiveServices.recordSwing(ctx as Activity)},Modifier.fillMaxWidth()){Text("RECORD SWING VIDEO")}
 listOf("Choose Video","Shot Tracer","Shot Pattern / Dispersion","Putting Practice","Short Game","Wedge Distances","Greenside Chipping","Greenslope","Score Comparison","Biometrics","Round Overview","Pre-Round","Club Equipment","Round History","How To").forEach{Panel(it,if(it=="Biometrics")"Health provider required for HR/SpO₂ — no invented readings" else "Ready module")}
}}
@Composable private fun Score(h:Int,s:MutableList<Int>,setHole:(Int)->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)){Header("SCORECARD");(1..18).forEach{i->Row(Modifier.fillMaxWidth().padding(3.dp),verticalAlignment=Alignment.CenterVertically){Text("Hole $i",color=Color.White,modifier=Modifier.weight(1f));listOf(3,4,5,6,7).forEach{n->TextButton({s[i-1]=n;setHole(i)}){Text(if(s[i-1]==n)"[$n]" else "$n",color=Gold)}}}};Panel("TOTAL",s.filter{it>0}.sum().toString())}}
