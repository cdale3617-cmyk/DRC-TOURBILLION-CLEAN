package com.drc.golftourbillion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class RemoteGolfCourse(val id:String,val name:String,val city:String,val region:String,val latitude:Double?,val longitude:Double?,val par:Int?,val yardage:Int?)
object GolfCourseApi {
 private const val BASE="https://api.opengolfapi.org/v1/courses"
 private fun getJson(url:String):JSONObject {
  val c=URL(url).openConnection() as HttpURLConnection;c.connectTimeout=10000;c.readTimeout=12000;c.setRequestProperty("Accept","application/json")
  return try {val code=c.responseCode;val stream=if(code in 200..299)c.inputStream else c.errorStream;val body=stream?.bufferedReader()?.use{it.readText()}.orEmpty();if(code !in 200..299)error("HTTP $code");JSONObject(body)}finally{c.disconnect()}
 }
 fun search(query:String,lat:Double?,lon:Double?):List<RemoteGolfCourse>{
  val params=if(query.isNotBlank())"q="+URLEncoder.encode(query.trim(),StandardCharsets.UTF_8.name()) else if(lat!=null&&lon!=null)"lat=$lat&lng=$lon&radius_mi=50" else error("Enter a course or location. Nearby search needs GPS.")
  val rows=getJson("$BASE/search?$params&limit=25").optJSONArray("courses")?:JSONArray()
  return (0 until rows.length()).mapNotNull{i->val o=rows.optJSONObject(i)?:return@mapNotNull null
   val id=o.optString("id").takeIf{it.isNotBlank()}?:return@mapNotNull null;val name=o.optString("course_name").ifBlank{o.optString("name")};if(name.isBlank())return@mapNotNull null
   RemoteGolfCourse(id,name,o.optString("city").takeUnless{it=="null"}.orEmpty(),o.optString("state").takeUnless{it=="null"}.orEmpty(),o.optDouble("latitude").takeUnless{it.isNaN()||it==0.0},o.optDouble("longitude").takeUnless{it.isNaN()||it==0.0},o.optInt("par_total").takeIf{it in 54..90},o.optInt("total_yardage").takeIf{it in 500..9000})
  }
 }
 fun holes(id:String):List<GolfHoleData>{
  val enc=URLEncoder.encode(id,StandardCharsets.UTF_8.name());val o=getJson("$BASE/$enc/holes");val rows=o.optJSONArray("holes")?:o.optJSONArray("data")?:o.optJSONArray("results")?:return emptyList()
  val list=(0 until rows.length()).mapNotNull{i->val h=rows.optJSONObject(i)?:return@mapNotNull null
   val n=firstInt(h,"number","hole","hole_number")?.takeIf{it in 1..18}?:i+1;val p=firstInt(h,"par")?.takeIf{it in 3..6}?:return@mapNotNull null
   val m=firstInt(h,"metres","meters","distance_m","length_m")?:firstInt(h,"yardage","yards","distance_yards","length_yards","total_yards")?.let{(it/1.09361).toInt()}?:0
   GolfHoleData(n,p,m.coerceIn(0,700))
  }.sortedBy{it.number}
  return if(list.size==18)list else emptyList()
 }
 private fun firstInt(o:JSONObject,vararg keys:String):Int?{keys.forEach{k->if(!o.has(k)||o.isNull(k))return@forEach;val v=o.opt(k);val n=if(v is Number)v.toInt() else v.toString().toIntOrNull();if(n!=null&&n>0)return n};return null}
}
@Composable fun GolfCourseSearchScreen(latitude:Double?,longitude:Double?,onBack:()->Unit,onUseCourse:(RemoteGolfCourse,List<GolfHoleData>)->Unit){
 val gold=Color(0xFFD4AF37);val panel=Color(0xFF0B2A1D);val white=Color(0xFFF5F5F5);val muted=Color(0xFFB9C3BE);val scope=rememberCoroutineScope()
 var query by remember{mutableStateOf("")};var results by remember{mutableStateOf(emptyList<RemoteGolfCourse>())};var message by remember{mutableStateOf("Search more than 30,000 courses worldwide. Internet is needed.")};var busy by remember{mutableStateOf(false)}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  Text("COURSE FINDER",color=gold,fontSize=24.sp,fontWeight=FontWeight.Bold)
  Text("Search by course, town or country. Leave blank to search near GPS.",color=white,fontSize=13.sp)
  OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),singleLine=true,label={Text("Course or location")},placeholder={Text("Pebble Beach, Australia, Capricorn")})
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Button(onClick={busy=true;message="Searching…";scope.launch{try{results=withContext(Dispatchers.IO){GolfCourseApi.search(query,latitude,longitude)};message=if(results.isEmpty())"No results. Try a town or broader place." else "${results.size} courses found. Choose one to load hole data."}catch(_:Exception){message="Course search failed. Check internet and try again."}finally{busy=false}}},enabled=!busy,colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF9E1B1B),contentColor=gold),border=BorderStroke(1.dp,gold)){Text(if(busy)"SEARCHING…" else "SEARCH")}
   TextButton(onClick=onBack){Text("BACK",color=gold)}
  }
  Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=panel),border=BorderStroke(1.dp,gold),shape=RoundedCornerShape(12.dp)){Text(message,Modifier.padding(12.dp),color=white,fontSize=13.sp)}
  results.forEach{r->Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=panel),border=BorderStroke(1.dp,gold),shape=RoundedCornerShape(12.dp)){
   Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text(r.name,color=gold,fontWeight=FontWeight.Bold,fontSize=16.sp)
    Text(listOf(r.city,r.region).filter{it.isNotBlank()}.joinToString(", ").ifBlank{"Location not listed"},color=muted,fontSize=12.sp)
    Text("Par ${r.par ?: "—"} • ${r.yardage ?: "—"} yards • Hole map coverage varies by course.",color=white,fontSize=12.sp)
    Button(onClick={busy=true;message="Loading ${r.name}…";scope.launch{try{val holes=withContext(Dispatchers.IO){GolfCourseApi.holes(r.id)};onUseCourse(r,holes)}catch(_:Exception){message="Course details failed. Check internet and try again."}finally{busy=false}}},enabled=!busy,colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF9E1B1B),contentColor=gold),border=BorderStroke(1.dp,gold)){Text("USE THIS COURSE")}
   }
  }}
  Text("Course data: OpenGolfAPI • © OpenStreetMap contributors • ODbL 1.0",color=muted,fontSize=10.sp)
 }
}
