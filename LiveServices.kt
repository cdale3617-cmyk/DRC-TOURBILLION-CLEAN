package com.drc.golftourbillion
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import java.net.URL
import org.json.JSONObject

data class LiveFix(val lat:Double,val lon:Double,val accuracy:Float)
data class LiveWeather(val temp:Double,val wind:Double,val direction:Int)

object LiveServices {
 fun lastLocation(context:Context):LiveFix?{
  if(ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
     ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) return null
  val lm=context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  val locations=lm.getProviders(true).mapNotNull{ runCatching{lm.getLastKnownLocation(it)}.getOrNull() }
  val best:Location=locations.maxByOrNull{it.time}?:return null
  return LiveFix(best.latitude,best.longitude,best.accuracy)
 }
 fun weather(fix:LiveFix):LiveWeather?=runCatching{
  val u="https://api.open-meteo.com/v1/forecast?latitude=${fix.lat}&longitude=${fix.lon}&current=temperature_2m,wind_speed_10m,wind_direction_10m"
  val c=URL(u).openConnection().apply{connectTimeout=7000;readTimeout=7000}
  val s=c.getInputStream().bufferedReader().use{it.readText()}
  val j=JSONObject(s).getJSONObject("current")
  LiveWeather(j.getDouble("temperature_2m"),j.getDouble("wind_speed_10m"),j.getInt("wind_direction_10m"))
 }.getOrNull()
 fun recordSwing(activity:Activity){ runCatching{activity.startActivity(Intent(MediaStore.ACTION_VIDEO_CAPTURE))} }
}
