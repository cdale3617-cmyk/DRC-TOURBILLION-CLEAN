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
import org.json.JSONObject
import java.net.URL

data class LiveFix(
    val lat: Double,
    val lon: Double,
    val accuracy: Float
)

data class LiveWeather(
    val temp: Double,
    val wind: Double,
    val direction: Int
)

object LiveServices {

    fun lastLocation(context: Context): LiveFix? {
        val hasFineLocation =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) return null

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locations = locationManager
            .getProviders(true)
            .mapNotNull { provider ->
                runCatching {
                    locationManager.getLastKnownLocation(provider)
                }.getOrNull()
            }

        val bestLocation: Location =
            locations.maxByOrNull { it.time } ?: return null

        return LiveFix(
            lat = bestLocation.latitude,
            lon = bestLocation.longitude,
            accuracy = bestLocation.accuracy
        )
    }

    fun weather(fix: LiveFix): LiveWeather? = runCatching {
        val address =
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${fix.lat}" +
                "&longitude=${fix.lon}" +
                "&current=temperature_2m,wind_speed_10m,wind_direction_10m"

        val connection = URL(address).openConnection().apply {
            connectTimeout = 7000
            readTimeout = 7000
        }

        val response = connection
            .getInputStream()
            .bufferedReader()
            .use { it.readText() }

        val current = JSONObject(response).getJSONObject("current")

        LiveWeather(
            temp = current.getDouble("temperature_2m"),
            wind = current.getDouble("wind_speed_10m"),
            direction = current.getInt("wind_direction_10m")
        )
    }.getOrNull()

    fun recordSwing(activity: Activity) {
        runCatching {
            activity.startActivity(
                Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            )
        }
    }
}
