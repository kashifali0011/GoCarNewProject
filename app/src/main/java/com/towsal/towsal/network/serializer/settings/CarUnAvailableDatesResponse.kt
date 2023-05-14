package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Response model for car unavailable dates
 * */
class CarUnAvailableDatesResponse : Serializable {
    @SerializedName("is_always_available")
    @Expose
    var isAlwaysAvailable: Int = null ?: 0

    @SerializedName("availability_data")
    @Expose
    var availabilityData: ArrayList<CarUnAvailableData> = null ?: ArrayList()
}
