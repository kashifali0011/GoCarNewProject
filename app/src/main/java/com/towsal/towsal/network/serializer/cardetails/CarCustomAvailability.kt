package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.utils.Constants
import java.io.Serializable

/**
 * Response model for car custom availability
 * */
class CarCustomAvailability : Serializable {

    @SerializedName("day")
    @Expose
    val day: String = null ?: ""

    @SerializedName("from")
    @Expose
    val from: String = null ?: ""

    @SerializedName("to")
    @Expose
    val to: String = null ?: ""

    @SerializedName("is_unavailable")
    @Expose
    val isUnavailable: Int = null ?: Constants.Availability.AVAILABLE

    var dayIndex: Int = -1

}