package com.towsal.towsal.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.towsal.towsal.R
import com.towsal.towsal.helper.preferenceModule
import com.towsal.towsal.helper.uiHelperModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.*

/**
 * Main application class where third party sdks initialized
 */
class MainApplication : LocalizationApplication() {

    companion object {
        var isLoggedIn = false
        var appOpen = false
        private var instance: MainApplication? = MainApplication()
        private lateinit var fusedLocationClient: FusedLocationProviderClient

        private val job = Job()
        val coroutineScope = CoroutineScope(job)

        @Synchronized
        fun applicationContext(): Context {
            if (instance == null) {
                instance = MainApplication()
            }
            return instance!!.applicationContext
        }

        var lastKnownLocation: Location? = null



        /**
         * Function for getting last known location of the device
         * */
        fun getLastDeviceLocation(): Location? {

            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(applicationContext())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    Log.e("GPS", "Main application last known location called==" + location)
                    lastKnownLocation = location
                    Log.d("llllloooccc", "lastKnown  = " + location)
                }

            checkForLocationUpdates()
            return lastKnownLocation
        }

        /**
         * Function for requesting location updates
         * */
        private fun checkForLocationUpdates() {
            val mLocationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        lastKnownLocation = location
                        Log.i(
                            "Main Application",
                            "Location: " + location.latitude + " " + location.longitude
                        )
                    }
                }
            }
            val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000L).build()
            if (ContextCompat.checkSelfPermission(
                    applicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    override fun getDefaultLanguage(): Locale {
        return Locale.getDefault()
    }

    private val localizationDelegate = LocalizationApplicationDelegate()

    override fun attachBaseContext(base: Context) {
        localizationDelegate.setDefaultLanguage(base, Locale.ENGLISH)
        super.attachBaseContext(localizationDelegate.attachBaseContext(base))
    }

    override fun onTerminate() {
        super.onTerminate()
        appOpen = false
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(
                listOf(mainModule, uiHelperModule, preferenceModule)
            )
        }
        Places.initialize(this, this.getString(R.string.custom_google_api_key))
        getLastDeviceLocation()
        AppCenter.start(
            this, this.getString(R.string.app_center_key),
            Analytics::class.java, Crashes::class.java
        )
    }

}