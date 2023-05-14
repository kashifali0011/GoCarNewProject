package com.towsal.towsal.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.GpsStatus.GPS_EVENT_STARTED
import android.location.GpsStatus.GPS_EVENT_STOPPED
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.helper.UIHelper

/**
 * Service class for checking for location updates
 * */
open class GPSTracker(private val mContext: Context) : Service(),
    LocationListener {

    // flag for GPS status
    var isGPSEnabled = false

    // flag for network status
    var isNetworkEnabled = false

    // flag for GPS status
    var canGetLocation = false
    private var location: Location? = null
    private var fusedLocation: Location? = null
    private var latitude = 0.0
    private var longitude = 0.0
    var mGnssStatusCallback: GnssStatus.Callback? = null

    // Declaring a Location Manager
    var locationManager: LocationManager? = null

    /**
     *  Functions for checking checking gps enabled
     * */
    fun checkGPsEnabled(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     *  Functions for setting location
     * */
    open fun setLocation(location: Location?) {
        fusedLocation = location

    }

    /**
     *  Functions for getting location
     * */
    @SuppressLint("MissingPermission")
    fun getLocation(): Location? {
        try {
            locationManager =
                mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // getting GPS status
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

//            if (!isGPSEnabled && !isNetworkEnabled) {
//                // no network provider is enabled
//            } else {
//                canGetLocation = true
//                // First get location from Network Provider
//                if (isNetworkEnabled) {
//                    locationManager!!.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER,
//                        MIN_TIME_BW_UPDATES,
//                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
//                    )
//                    Log.d("Network", "Network")
//                    if (locationManager != null) {
//                        location = locationManager!!
//                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//                        if (location != null) {
//                            latitude = location!!.latitude
//                            longitude = location!!.longitude
//                        }
//                    }
//                }
//
//                // if GPS Enabled get lat/long using GPS Services
//                if (isGPSEnabled) {
//                    if (location == null) {
//                        locationManager!!.requestLocationUpdates(
//                            LocationManager.GPS_PROVIDER,
//                            MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
//                        )
//                        Log.d("GPS Enabled", "GPS Enabled")
//                        if (locationManager != null) {
//                            location = locationManager!!
//                                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
//                            if (location != null) {
//                                latitude = location!!.latitude
//                                longitude = location!!.longitude
//                            }
//                        }
//                    }
//                    return location
//                }
//            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mGnssStatusCallback = object : GnssStatus.Callback() {
                    override fun onSatelliteStatusChanged(status: GnssStatus) {
                        if (MainApplication.lastKnownLocation == null) {
                            location = MainApplication.getLastDeviceLocation()
                        }
//                        Log.e("Gps", "Gnss On onSatelliteStatusChanged")
                    }

                    override fun onStarted() {
                        super.onStarted()
//                        Log.e("Gps", "Gnss On start")
                        if (MainApplication.lastKnownLocation == null) {
                            location = MainApplication.getLastDeviceLocation()
                        }
                    }

                    override fun onStopped() {
//                        Log.e("Gps", "Gnss On Stop")
                        mGnssStatusCallback?.let {
                            locationManager!!.unregisterGnssStatusCallback(
                                it
                            )
                        }
                        super.onStopped()
                    }
                }
                locationManager!!.registerGnssStatusCallback(mGnssStatusCallback!!)
            } else {
                locationManager!!.addGpsStatusListener { event ->
                    when (event) {
                        GPS_EVENT_STARTED -> {
//                            Log.e("Gps", "Gps Enabled")
                            if (MainApplication.lastKnownLocation == null) {
                                location = MainApplication.getLastDeviceLocation()
                            }
                        }
                        GPS_EVENT_STOPPED -> {
//                            Log.e("Gps", "Gps Stopped")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (location == null) {
            location = Location("")
            location!!.latitude = 0.0
            location!!.longitude = 0.0
        }
        return location
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    /**
     * Function to get latitude
     */
    fun getLatitude(): Double {
        getProviderLocation()
        if (location == null) {
            return 0.0
        }
        latitude = location!!.latitude
        MainApplication.lastKnownLocation = location
        // return latitude
        return latitude
    }

    /**
     * Function to get longitude
     */
    fun getLongitude(): Double {
        getProviderLocation()
        if (location == null) {
            return 0.0
        }
        longitude = location!!.longitude
        MainApplication.lastKnownLocation = location
        // return longitude
        return longitude
    }

    /**
     *  Functions for getting location
     * */
    private fun getProviderLocation() {
        if (ActivityCompat.checkSelfPermission(
                MainApplication.applicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainApplication.applicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                if (checkGPsEnabled()) {
                    location =
                        locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                        location =
                            locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null) {
                            location = MainApplication.getLastDeviceLocation()
                        }
                    }
                } else {
                    location = locationManager!!
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null && location!!.longitude == 0.0) {
                        location =
                            locationManager!!.getLastKnownLocation(LocationManager.EXTRA_PROVIDER_ENABLED)
                    }
                }
                if (fusedLocation != null && location == null) {
                    location = fusedLocation
                }
            } catch (e: Exception) {
                var message = e.localizedMessage
                if (message.isNullOrEmpty()) {
                    message = "Something Went Wrong Try Again"
                }
                UIHelper().showLongToastInCenter(
                    MainApplication.applicationContext(),
                    message
                )
            }
        }
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    override fun onProviderDisabled(provider: String) {
        Log.e("GPS Tracker", "Provide Disabled=$provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.e("GPS Tracker", "Provide Enabled=$provider")

    }

    override fun onLocationChanged(p0: Location) {
        location = p0
    }

    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
        Log.e("GPS Tracker", "Status Changed Provider=$provider")
        Log.e("GPS Tracker", "Status Changed Status=$status")
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 100 // 10 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 0 // 1 minute
            .toLong()
    }

    init {
        getLocation()
    }

    /**
     *  Functions for getting distance
     * */
    private fun distance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val earthRadius = 6371 // in miles, change to 6371 for kilometer output
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val sindLat = Math.sin(dLat / 2)
        val sindLng = Math.sin(dLng / 2)
        val a = Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(
            Math.toRadians(
                lat2
            )
        ))
        val c =
            2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c // output distance, in MILES
    }

}