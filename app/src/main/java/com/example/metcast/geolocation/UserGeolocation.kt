package com.example.metcast.geolocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class LocationHelper(
    private val context: Context,
    private val callback: LocationCallback
) : LocationListener {

    interface LocationCallback {
        fun onLocationReceived(cityName: String)
    }

    private val locationPermissionCode = 1
    private var locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as ComponentActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionCode
            )
            return
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                this
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val cityName = getCityNameFromCoordinates(context, latitude, longitude)
        callback.onLocationReceived(cityName)
    }

    private fun getCityNameFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                return address.locality ?: ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            }
        }
    }
}
