package com.towsal.towsal.network.serializer.trips

import android.net.Uri
import java.io.Serializable

/**
 * Custom model for rejection of photos
 * */
class CounterImageModel : Serializable {
    var position = -1
    var uri = Uri.EMPTY
    var reason = ""
    var odometerReading = ""
}