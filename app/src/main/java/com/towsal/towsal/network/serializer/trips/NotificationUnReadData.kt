package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Response model for getting new notifications count
 * */
class NotificationUnReadData {
    @SerializedName("unReadCount")
    @Expose
    var unReadCount: Int = null ?: -1
}