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
 val old=scores[index-1];var shots by remember(index){mutableIntStateOf(old.shots.coerceAtLeast(1))};var putts by remember(index){mutableIntStateOf(old.putts)};var par by remember(index){mutableIntStateOf(old.par)};var length by remember(index){mutableIntStateOf(old.metres)};var fairway by remember(index){mutableStateOf(old.fairway)};var gir by remember(index){mutableStateOf(old.gir)};var edit by remember{mutableStateOf(false)}
 val d=distance(length,yards);val recommendation=bag.filter{it.carry>0}.minByOrNull{abs(it.carry-d)}?.name?:"Choose club"
 fun update(){onEdit(Hole(par,length,shots,putts,fairway,gir))}
 Page{
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("LIVE CADDIE",color=Gold,fontSize=21.sp,fontWeight=FontWeight.Bold);Text(course,color=Muted,fontSize=11.sp)}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){OutlinedButton(onClick={if(index>1)onIndex(index-1)},enabled=index>1,border=BorderStroke(1.dp,Gold)){Text("PREV",color=Gold)};Column(horizontalAlignment=Alignment.CenterHorizontally){Text("HOLE "+index,color=Gold,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("PAR "+par+" • "+d+(if(yards)" yd" else " m"),color=White)};OutlinedButton(onClick={if(index<18)onIndex(index+1)},enabled=index<18,border=BorderStroke(1.dp,Gold)){Text("NEXT",color=Gold)}}
  PanelCard("HOLE MAP","TEE  ↑  FAIRWAY  ↑  GREEN","Hole length is editable. GPS reports current device position.")
  Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){Tiny("GPS",gps,Modifier.weight(1f));Tiny("WIND",wind.ifBlank{"Waiting for GPS"},Modifier.weight(1f))}
  Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){Tiny("DISTANCE",d.toString()+(if(yards)" yd" else " m"),Modifier.weight(1f));Tiny("CLUB ADVICE",recommendation,Modifier.weight(1f))}
  Text("Advice only • confirm target and club",color=Muted,fontSize=12.sp)
  PanelCard("SCORE","Strokes: "+shots+" • Putts: "+putts,"Score saves on this device")
  Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){OutlineAction("− STROKE",{if(shots>1)shots--;update()},Modifier.weight(1f));OutlineAction("+ STROKE",{shots++;update()},Modifier.weight(1f))}
  Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){OutlineAction("− PUTT",{if(putts>0)putts--;update()},Modifier.weight(1f));OutlineAction("+ PUTT",{putts++;update()},Modifier.weight(1f))}
  Row(verticalAlignment=Alignment.CenterVertically){Checkbox(fairway,{fairway=it;update()},colors=CheckboxDefaults.colors(checkedColor=Gold));Text("Fairway",color=White);Checkbox(gir,{gir=it;update()},colors=CheckboxDefaults.colors(checkedColor=Gold));Text("GIR",color=White)}
  OutlinedButton(onClick={edit=true},modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.dp,Gold)){Text("EDIT HOLE PAR / DISTANCE",color=Gold)}
  Button(onClick={onSave(Hole(par,length,shots,putts,fairway,gir))},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Red,contentColor=Gold),border=BorderStroke(1.dp,Gold)){Text(if(index<18)"SAVE SCORE • NEXT HOLE" else "SAVE FINAL HOLE",fontWeight=FontWeight.Bold)}
 }
 if(edit)HoleDialog(par,length,{edit=false}){p,m->par=p;length=m;update()}
}

@Composable private fun Score(course:String,scores:List<Hole>,yards:Boolean,onHole:(Int)->Unit){
 val played=scores.filter{it.shots>0};val total=played.sumOf{it.shots};val par=played.sumOf{it.par}
 Page{
  Text("SCORECARD",color=Gold,fontSize=24.sp,fontWeight=FontWeight.Bold);Text(course,color=White)
  Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){Tiny("HOLES",played.size.toString()+"/18",Modifier.weight(1f));Tiny("STROKES",if(played.isEmpty())"—" else total.toString(),Modifier.weight(1f));Tiny("TO PAR",if(played.isEmpty())"—" else (if(total>par)"+" else "")+(total-par),Modifier.weight(1f))}
  Text("Tap ENTER to edit a hole. Keep the paper card for tournaments.",color=Muted,fontSize=12.sp)
  scores.forEachIndexed{i,s->Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Panel),border=BorderStroke(1.dp,Gold)){Row(Modifier.fillMaxWidth().padding(horizontal=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text("HOLE "+(i+1),color=Gold,fontWeight=FontWeight.Bold);Text("P"+s.par,color=White);Text(distance(s.metres,yards).toString()+(if(yards)"yd" else "m"),color=Muted);Text(if(s.shots==0)"—" else s.shots.toString(),color=Gold,fontSize=18.sp,fontWeight=FontWeight.Bold);TextButton(onClick={onHole(i)}){Text("ENTER",color=Gold,fontSize=10.sp)}}}}
  PanelCard("OFFICIAL CARD","Use and sign the paper card","Tournament reminder")
 }
}

@Composable private fun Bag(bag:SnapshotStateList<Club>,yards:Boolean,onChange:(Int?,Club)->Unit){
 var edit by remember{mutableStateOf<Pair<Int?,Club>?>(null)}
 Page{
  Text("MY BAG",color=Gold,fontSize=24.sp,fontWeight=FontWeight.Bold)
  PanelCard("BAG SUMMARY",bag.size.toString()+" clubs • Longest carry "+distance(bag.maxOfOrNull{it.carry}?:0,yards)+(if(yards)" yd" else " m"),"Carry distances are editable")
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("CLUB",color=Gold,fontSize=11.sp);Text(if(yards)"CARRY (YD)" else "CARRY (M)",color=Gold,fontSize=11.sp);Text("LOFT",color=Gold,fontSize=11.sp)}
  bag.forEachIndexed{i,club->Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Panel),border=BorderStroke(1.dp,Gold)){Row(Modifier.fillMaxWidth().padding(horizontal=5.dp),verticalAlignment=Alignment.CenterVertically){Text(club.name,color=White,modifier=Modifier.weight(1f),fontSize=12.sp);Text(distance(club.carry,yards).toString(),color=Gold,modifier=Modifier.width(45.dp),textAlign=TextAlign.Center);Text(club.loft+"°",color=Muted,modifier=Modifier.width(38.dp),textAlign=TextAlign.Center);TextButton(onClick={edit=i to club}){Text("EDIT",color=Gold,fontSize=10.sp)}}}}
  OutlinedButton(onClick={edit=null to Club("New Club",100,"—")},modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.dp,Gold)){Text("ADD CLUB",color=Gold)}
 }
 edit?.let{pair->ClubDialog(pair.second,yards,{edit=null}){onChange(pair.first,it)}}
}

private val tools=listOf("Swing Monitor","Shot Tracer","Shot Pattern / Dispersion","Putting Practice","Short Game","Wedge Distances","Greenside Chipping","Greenslope","Score Comparison","Biometrics","Round Overview","Pre-Round","Club Equipment","Round History","How To")
private fun toolInfo(s:String)=when(s){"Swing Monitor"->"Record a swing or select a saved video";"Shot Tracer"->"Review swing video and log a shot";"Shot Pattern / Dispersion"->"Record shot distance and miss direction";"Putting Practice"->"Log putts from repeatable distances";"Short Game"->"Track up-and-down practice";"Wedge Distances"->"Build a wedge carry chart";"Greenside Chipping"->"Log chip start and finish distances";"Greenslope"->"Open camera and save a green read";"Score Comparison"->"Compare this round with your target";"Biometrics"->"Record heart rate and oxygen saturation";"Round Overview"->"Review scoring and round stats";"Pre-Round"->"Complete your preparation checklist";"Club Equipment"->"Edit club lofts and carries";"Round History"->"Review saved scorecard";else->"Quick guide to Tourbillion"}
@Composable private fun Lab(onPick:(String)->Unit){Page{Text("Analytical Frameworks And Diagnostic Suite",color=Gold,fontSize=21.sp,fontWeight=FontWeight.Bold);tools.forEach{Action(it,toolInfo(it),Modifier.fillMaxWidth()){onPick(it)}}}}
@Composable private fun More(course:String,player:String,yards:Boolean,onCourse:(String)->Unit,onUnits:()->Unit,onTool:(String)->Unit){var choose by remember{mutableStateOf(false)};Page{Text("MORE",color=Gold,fontSize=24.sp,fontWeight=FontWeight.Bold);PanelCard("PLAYER",player,"Name is editable from Home");PanelCard("COURSE",course,"Select course"){choose=true};Action("UNITS",if(yards)"Switch to metres" else "Switch to yards",Modifier.fillMaxWidth(),onUnits);Action("HOW TO","App guide",Modifier.fillMaxWidth()){onTool("How To")};Action("PRE-ROUND","Preparation checklist",Modifier.fillMaxWidth()){onTool("Pre-Round")};Action("ROUND OVERVIEW","Scoring, fairways and greens",Modifier.fillMaxWidth()){onTool("Round Overview")};Text("Tournament play: keep and sign the official paper card.",color=Muted,fontSize=12.sp)};if(choose)SelectDialog("Select course",listOf("Mercure Capricorn Resort","Custom Course"),{choose=false}){onCourse(it)}}

@Composable private fun Tool(name:String,yards:Boolean,onBack:()->Unit,onBag:()->Unit){
 val c=LocalContext.current;val p=prefs(c);var message by remember(name){mutableStateOf("Ready")};var a by remember(name){mutableStateOf("")};var b by remember(name){mutableStateOf("")};var hr by remember{mutableStateOf(p.getString("hr","")?:"")};var spo by remember{mutableStateOf(p.getString("spo","")?:"")};var checked by remember{mutableStateOf(setOf<String>())}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri:Uri?->message=if(uri==null)"No video selected" else "Video selected. Review in your video player."}
 Page{
  Text(name.uppercase(),color=Gold,fontSize=22.sp,fontWeight=FontWeight.Bold);Text(toolInfo(name),color=White)
  when(name){
   "Swing Monitor","Shot Tracer"->{PanelCard("VIDEO REVIEW","Record with the phone camera or choose a saved swing.","Ask a helper to tap record for a full swing view.");Log("RECORD SWING VIDEO"){try{c.startActivity(Intent(MediaStore.ACTION_VIDEO_CAPTURE));message="Camera opened. Return after recording."}catch(_:Exception){message="No camera app available"}};OutlinedButton(onClick={picker.launch("video/*")},modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.dp,Gold)){Text("CHOOSE VIDEO",color=Gold)};Text("Tracer colour: green. Video playback opens in the phone player.",color=Muted,fontSize=12.sp)}
   "Shot Pattern / Dispersion"->{Field("Carry distance",a){a=it};Choice("Miss direction",listOf("L","Centre","R")){b=it};Log("LOG SHOT"){val n=p.getInt("disp",0)+1;p.edit().putInt("disp",n).apply();message="Shot "+n+" saved: "+a+" "+b};PanelCard("SHOTS LOGGED",p.getInt("disp",0).toString(),"Saved on this device")}
   "Putting Practice"->{Field("Starting distance",a){a=it};Field("Putts made",b){b=it};Log("SAVE PUTTING SET"){p.edit().putInt("putting",p.getInt("putting",0)+1).apply();message="Putting set saved"};PanelCard("SETS SAVED",p.getInt("putting",0).toString(),"Repeat a distance to compare")}
   "Short Game","Greenside Chipping"->{Field("Start distance",a){a=it};Field("Finish distance",b){b=it};Log("SAVE PRACTICE"){p.edit().putInt("short",p.getInt("short",0)+1).apply();message="Short-game result saved"};PanelCard("PRACTICE LOGS",p.getInt("short",0).toString(),"Short game begins inside 100 metres or yards")}
   "Wedge Distances"->{PanelCard("WEDGE MATRIX","Record full-swing carry, not total roll.","Edit wedge entries in My Bag.");Field("Club name",a){a=it};Field("Carry distance",b){b=it};OutlinedButton(onClick=onBag,modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.dp,Gold)){Text("EDIT MY BAG",color=Gold)}}
   "Greenslope"->{PanelCard("GREEN READ","Use camera, then record slope direction and confidence.","Camera image does not measure slope by itself.");Choice("Slope direction",listOf("Up","Down","Left","Right","Level")){a=it};Choice("Confidence",listOf("Low","Medium","High")){b=it};Log("OPEN CAMERA"){try{c.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE));message="Camera opened. Return to save the read."}catch(_:Exception){message="No camera app available"}};Log("SAVE GREEN READ"){p.edit().putInt("green",p.getInt("green",0)+1).apply();message=a+" • "+b};PanelCard("SAVED READS",p.getInt("green",0).toString(),"Manual reads saved")}
   "Biometrics"->{PanelCard("HEALTH DATA","Enter readings from Samsung Health or a watch.","Phone does not measure heart rate or SpO₂.");Field("Heart rate (bpm)",hr){hr=it};Field("SpO₂ (%)",spo){spo=it};Log("SAVE READING"){p.edit().putString("hr",hr).putString("spo",spo).apply();message="Reading saved"};PanelCard("LAST READING","HR "+hr.ifBlank{"—"}+" bpm • SpO₂ "+spo.ifBlank{"—"}+"%","Saved on this device")}
   "Pre-Round"->{listOf("Clubs checked","Balls and tees ready","Warm-up complete","Practice swings complete","Fees paid","Paper card ready").forEach{v->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(v in checked,{checked=if(it)checked+v else checked-v},colors=CheckboxDefaults.colors(checkedColor=Gold));Text(v,color=White)}};PanelCard("PREPARATION",checked.size.toString()+" / 6 complete","Putting-speed test not included")}
   "Club Equipment"->{PanelCard("CLUB SETUP","Update each club loft and carry in My Bag.","Changes save on this device.");OutlinedButton(onClick=onBag,modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.dp,Gold)){Text("EDIT MY BAG",color=Gold)}}
   "Round Overview","Score Comparison","Round History"->{val played=loadScores(c).filter{it.shots>0};val strokes=played.sumOf{it.shots};val par=played.sumOf{it.par};PanelCard("HOLES PLAYED",played.size.toString()+" / 18","Saved scorecard");PanelCard("STROKES",if(played.isEmpty())"—" else strokes.toString(),"Par "+if(played.isEmpty())"—" else par);PanelCard("FAIRWAYS",played.count{it.fairway}.toString(),"Greens in regulation: "+played.count{it.gir});PanelCard("PUTTS",played.sumOf{it.putts}.toString(),"Recorded on scorecard");if(name=="Score Comparison")Field("Target score",a){a=it}}
   else->{PanelCard("QUICK GUIDE","Home starts the round. Caddie shows each hole. Score stores strokes. My Bag keeps club carries.","GPS and weather need location and internet.");PanelCard("ADVICE ONLY","Choose a target, consider wind, and select a club for carry.","Confirm recommendations.");PanelCard("TOURNAMENTS","Use your official paper scorecard and have it signed.","For practice and personal records.")}
  }
  Text(message,color=Gold,fontSize=14.sp);OutlinedButton(onClick=onBack,modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.dp,Gold)){Text("BACK TO GOLF LAB",color=Gold)}
 }
}

@Composable private fun Page(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=13.dp,vertical=9.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)}
@Composable private fun Nav(s:String,active:Boolean,onClick:()->Unit){TextButton(onClick=onClick,contentPadding=PaddingValues(horizontal=2.dp,vertical=3.dp)){Text(s,color=if(active)Gold else White,fontSize=10.sp,fontWeight=if(active)FontWeight.Bold else FontWeight.Normal)}}
@Composable private fun PanelCard(title:String,body:String,detail:String="",onClick:(()->Unit)?=null){Card(onClick=onClick?:{},enabled=onClick!=null,modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Panel,disabledContainerColor=Panel),border=BorderStroke(1.dp,Gold),shape=RoundedCornerShape(12.dp)){Column(Modifier.padding(12.dp)){Text(title,color=Gold,fontSize=13.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(3.dp));Text(body,color=White,fontSize=15.sp,fontWeight=FontWeight.SemiBold);if(detail.isNotBlank())Text(detail,color=Muted,fontSize=12.sp)}}}
@Composable private fun Tiny(title:String,body:String,modifier:Modifier=Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=Panel),border=BorderStroke(1.dp,Gold),shape=RoundedCornerShape(10.dp)){Column(Modifier.fillMaxWidth().padding(8.dp)){Text(title,color=Gold,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(body,color=White,fontSize=12.sp)}}}
@Composable private fun Action(title:String,detail:String,modifier:Modifier=Modifier,onClick:()->Unit){Card(onClick=onClick,modifier=modifier,colors=CardDefaults.cardColors(containerColor=Panel),border=BorderStroke(1.dp,Gold),shape=RoundedCornerShape(12.dp)){Column(Modifier.fillMaxWidth().padding(11.dp)){Text(title,color=Gold,fontWeight=FontWeight.Bold);Text(detail,color=White,fontSize=12.sp)}}}
@Composable private fun OutlineAction(title:String,onClick:()->Unit,modifier:Modifier=Modifier){OutlinedButton(onClick=onClick,modifier=modifier,border=BorderStroke(1.dp,Gold)){Text(title,color=Gold,fontSize=11.sp)}}
@Composable private fun Log(title:String,onClick:()->Unit){Button(onClick=onClick,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Panel,contentColor=Gold),border=BorderStroke(1.dp,Gold)){Text(title,fontWeight=FontWeight.Bold)}}
@Composable private fun Field(label:String,value:String,onValue:(String)->Unit){OutlinedTextField(value,onValue,modifier=Modifier.fillMaxWidth(),label={Text(label,color=Muted)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),colors=OutlinedTextFieldDefaults.colors(focusedTextColor=White,unfocusedTextColor=White,focusedBorderColor=Gold,unfocusedBorderColor=Muted))}
@Composable private fun Choice(title:String,options:List<String>,onChoice:(String)->Unit){var selected by remember{mutableStateOf(options.first())};Text(title,color=White,fontSize=13.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){options.forEach{v->OutlinedButton(onClick={selected=v;onChoice(v)},modifier=Modifier.weight(1f),contentPadding=PaddingValues(1.dp),border=BorderStroke(1.dp,if(selected==v)Gold else Muted)){Text(v,color=if(selected==v)Gold else White,fontSize=10.sp)}}}}
@Composable private fun TextDialog(title:String,initial:String,onDismiss:()->Unit,onSave:(String)->Unit){var value by remember{mutableStateOf(initial)};AlertDialog(onDismissRequest=onDismiss,containerColor=Panel,title={Text(title,color=Gold)},text={OutlinedTextField(value,{value=it},singleLine=true)},confirmButton={TextButton(onClick={onSave(value.trim());onDismiss()}){Text("SAVE",color=Gold)}},dismissButton={TextButton(onClick=onDismiss){Text("CANCEL",color=White)}})}
@Composable private fun SelectDialog(title:String,items:List<String>,onDismiss:()->Unit,onPick:(String)->Unit){AlertDialog(onDismissRequest=onDismiss,containerColor=Panel,title={Text(title,color=Gold)},text={Column{items.forEach{TextButton(onClick={onPick(it);onDismiss()}){Text(it,color=White)}}}},confirmButton={TextButton(onClick=onDismiss){Text("CLOSE",color=Gold)}})}
@Composable private fun HoleDialog(par:Int,len:Int,onDismiss:()->Unit,onSave:(Int,Int)->Unit){var p by remember{mutableStateOf(par.toString())};var m by remember{mutableStateOf(len.toString())};AlertDialog(onDismissRequest=onDismiss,containerColor=Panel,title={Text("Hole setup",color=Gold)},text={Column{Field("Par",p){p=it};Field("Distance metres",m){m=it}}},confirmButton={TextButton(onClick={onSave(p.toIntOrNull()?.coerceIn(3,6)?:par,m.toIntOrNull()?.coerceIn(30,700)?:len);onDismiss()}){Text("SAVE",color=Gold)}},dismissButton={TextButton(onClick=onDismiss){Text("CANCEL",color=White)}})}
@Composable private fun ClubDialog(club:Club,yards:Boolean,onDismiss:()->Unit,onSave:(Club)->Unit){var name by remember{mutableStateOf(club.name)};var carry by remember{mutableStateOf(distance(club.carry,yards).toString())};var loft by remember{mutableStateOf(club.loft)};AlertDialog(onDismissRequest=onDismiss,containerColor=Panel,title={Text("Edit club",color=Gold)},text={Column{OutlinedTextField(name,{name=it},label={Text("Club")},singleLine=true);OutlinedTextField(carry,{carry=it},label={Text(if(yards)"Carry yards" else "Carry metres")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));OutlinedTextField(loft,{loft=it},label={Text("Loft degrees")},singleLine=true)}},confirmButton={TextButton(onClick={val n=carry.toIntOrNull()?:0;onSave(Club(name.ifBlank{"Club"},if(yards)yardsToMetres(n) else n,loft.ifBlank{"—"}));onDismiss()}){Text("SAVE",color=Gold)}},dismissButton={TextButton(onClick=onDismiss){Text("CANCEL",color=White)}})}
