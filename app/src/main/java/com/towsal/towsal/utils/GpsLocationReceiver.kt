package com.towsal.towsal.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.towsal.towsal.app.BaseActivity

/**
* Broadcast Receiver class for location handling
* */
class GpsLocationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val sss = "android.location.PROVIDERS_CHANGED"
        if (intent.action!!.matches(sss.toRegex())) {
            if (BaseActivity.gpsTracker != null) {
                BaseActivity.gpsTracker = GPSTracker(context)
            } else {
                Log.e("GPS", "Base Activity Gps is Null")
            }
        }
    }
}