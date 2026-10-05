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

private val Gold=Color(0xFFD4AF37)
private val Green=Color(0xFF071C14)
private val Panel=Color(0xFF0B2A1D)
private val Red=Color(0xFF9E1B1B)
private val White=Color(0xFFF5F5F5)
private val Muted=Color(0xFFB9C3BE)
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
 val raw=prefs(c).getString("scores",null)?:return pars.indices.map{Hole(pars[it],lengths[it],0,0,false,false)}
 val list=raw.split("|").mapNotNull{val p=it.split("~");if(p.size!=6)null else Hole(p[0].toIntOrNull()?:4,p[1].toIntOrNull()?:350,p[2].toIntOrNull()?:0,p[3].toIntOrNull()?:0,p[4]=="1",p[5]=="1")}
 return if(list.size==18)list else pars.indices.map{Hole(pars[it],lengths[it],0,0,false,false)}
}
private fun saveScores(c:Context,s:List<Hole>){prefs(c).edit().putString("scores",s.joinToString("|"){it.par.toString()+"~"+it.metres+"~"+it.shots+"~"+it.putts+"~"+(if(it.fairway)"1" else "0")+"~"+(if(it.gir)"1" else "0")}).apply()}

@Composable fun TourbillionApp(){
 val c=LocalContext.current
 var page by remember{mutableStateOf("HOME")};var tool by remember{mutableStateOf("")};var hole by remember{mutableIntStateOf(1)}
 var yards by remember{mutableStateOf(prefs(c).getBoolean("yards",false))}
 var player by remember{mutableStateOf(prefs(c).getString("player","Dale")?:"Dale")}
 var course by remember{mutableStateOf(prefs(c).getString("course","Mercure Capricorn Resort")?:"Mercure Capricorn Resort")}
 val bag=remember{mutableStateListOf<Club>().apply{addAll(loadBag(c))}}
 val scores=remember{mutableStateListOf<Hole>().apply{addAll(loadScores(c))}}
 var loc by remember{mutableStateOf<Location?>(null)};var gps by remember{mutableStateOf("GPS not connected")};var wx by remember{mutableStateOf("Weather waiting for GPS")};var wind by remember{mutableStateOf("")}
 var allowed by remember{mutableStateOf(ContextCompat.checkSelfPermission(c,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)}
 val worker=remember{Executors.newSingleThreadExecutor()}
 val ask=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){allowed=it;gps=if(it)"Finding GPS…" else "Location permission not granted"}
 DisposableEffect(allowed){
  if(!allowed) onDispose{} else {
   val mgr=c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
   val listener=object:LocationListener{
    override fun onLocationChanged(l:Location){loc=l;gps="GPS accuracy "+l.accuracy.roundToInt()+" m"}
    override fun onProviderEnabled(provider:String){gps="GPS active"}
    override fun onProviderDisabled(provider:String){gps="Turn on device location"}
    @Deprecated("Deprecated") override fun onStatusChanged(provider:String?,status:Int,extras:Bundle?){}
   }
   try{mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let{loc=it};mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000L,5f,listener,Looper.getMainLooper());gps=if(loc==null)"Finding GPS…" else "GPS connected"}catch(_:Exception){gps="GPS unavailable — allow location"}
   onDispose{try{mgr.removeUpdates(listener)}catch(_:Exception){}}
  }
 }
 LaunchedEffect(loc?.latitude,loc?.longitude){
  val l=loc?:return@LaunchedEffect
  worker.execute{try{
   val url=URL("https://api.open-meteo.com/v1/forecast?latitude="+l.latitude+"&longitude="+l.longitude+"&current=temperature_2m,wind_speed_10m,wind_direction_10m&wind_speed_unit=kmh&timezone=auto")
   val conn=url.openConnection() as HttpURLConnection;conn.connectTimeout=7000;conn.readTimeout=7000
   val data=JSONObject(conn.inputStream.bufferedReader().use{it.readText()}).getJSONObject("current")
   val temp=data.getDouble("temperature_2m").roundToInt();val speed=data.getDouble("wind_speed_10m").roundToInt();val deg=data.getInt("wind_direction_10m")
   val dirs=listOf("N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW")
   wx=temp.toString()+"°C • Wind "+speed+" km/h";wind=dirs[((deg+11)/22.5).toInt()%16]+" • "+speed+" km/h";conn.disconnect()
  }catch(_:Exception){wx="Weather unavailable — check internet"}}
 }
 Scaffold(containerColor=Green,bottomBar={Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(2.dp),horizontalArrangement=Arrangement.SpaceEvenly){listOf("HOME","CADDIE","LAB","SCORE","BAG","MORE").forEach{Nav(it,page==it){page=it}}}}){pad->
  Box(Modifier.fillMaxSize().padding(pad)){
   when(page){
    "HOME"->TourbillionHomeScreen(
     playerName=player,
     courseName=course,
     gpsStatus=gps,
     weatherSummary=wx,
     windSummary=wind,
     yards=yards,
     onPlayerNameChange={player=it;prefs(c).edit().putString("player",it).apply()},
     onCourseChange={page="MORE"},
     onUnitsToggle={yards=!yards;prefs(c).edit().putBoolean("yards",yards).apply()},
     onStartRound={if(!allowed)ask.launch(Manifest.permission.ACCESS_FINE_LOCATION);hole=1;page="CADDIE"},
     onOpenBag={page="BAG"},
     onOpenLab={page="LAB"},
     onOpenScore={page="SCORE"},
     onOpenHistory={tool="Round History";page="TOOL"}
    )
    "CADDIE"->Caddie(course,hole,scores,bag,yards,gps,wind,{hole=it},{v->scores[hole-1]=v;saveScores(c,scores);if(hole<18)hole++},{v->scores[hole-1]=v;saveScores(c,scores)})
    "SCORE"->Score(course,scores,yards){hole=it+1;page="CADDIE"}
    "BAG"->Bag(bag,yards){i,v->if(i==null)bag.add(v)else bag[i]=v;saveBag(c,bag)}
    "LAB"->Lab{tool=it;page="TOOL"}
    "MORE"->More(course,player,yards,{course=it;prefs(c).edit().putString("course",it).apply()},{yards=!yards;prefs(c).edit().putBoolean("yards",yards).apply()},{tool=it;page="TOOL"})
    else->Tool(tool,yards,{page="LAB"},{page="BAG"})
   }
  }
 }
}

@Composable private fun Home(player:String,course:String,gps:String,wx:String,wind:String,yards:Boolean,onPlayer:(String)->Unit,onCourse:(String)->Unit,onUnits:()->Unit,onGps:()->Unit,onStart:()->Unit,onBag:()->Unit,onLab:()->Unit){
 var nameDialog by remember{mutableStateOf(false)};var courseDialog by remember{mutableStateOf(false)}
 Page{
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("DRC",color=Gold,fontSize=20.sp,fontWeight=FontWeight.Bold);TextButton(onClick=onUnits){Text(if(yards)"YARDS" else "METRES",color=Gold)}}
  Text("GOLF TOURBILLION",color=Gold,fontSize=27.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())
  TextButton(onClick={nameDialog=true},modifier=Modifier.align(Alignment.CenterHorizontally)){Text("Welcome, "+player+" • Edit",color=White)}
  PanelCard("COURSE",course,"Change course"){courseDialog=true}
  PanelCard("LIVE CONDITIONS",gps,wx+(if(wind.isBlank())"" else " • "+wind)){onGps()}
  Button(onClick=onStart,modifier=Modifier.fillMaxWidth().height(58.dp),colors=ButtonDefaults.buttonColors(containerColor=Red,contentColor=Gold),border=BorderStroke(1.dp,Gold),shape=RoundedCornerShape(12.dp)){Text("START CADDIE ENGINE",fontSize=17.sp,fontWeight=FontWeight.Bold)}
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Action("MY BAG","Clubs, lofts and carries",Modifier.weight(1f),onBag);Action("GOLF LAB","Practice and analysis",Modifier.weight(1f),onLab)}
  PanelCard("ROUND STATUS","18-hole round ready","Scores save on this device")
  Text("Practice like you’re playing.",color=Muted,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
 }
 if(nameDialog)TextDialog("Player name",player,{nameDialog=false}){onPlayer(it)}
 if(courseDialog)SelectDialog("Select course",listOf("Mercure Capricorn Resort","Custom Course"),{courseDialog=false}){onCourse(it)}
}

@Composable private fun Caddie(course:String,index:Int,scores:SnapshotStateList<Hole>,bag:List<Club>,yards:Boolean,gps:String,wind:String,onIndex:(Int)->Unit,onSave:(Hole)->Unit,onEdit:(Hole)->Unit){
 val old=scores[index-1];
