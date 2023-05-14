package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for trips filtering
 * */
class FilterTripsModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: -1

    @SerializedName(DataKeyValues.NAME)
    @Expose
    var name: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.IMAGE_URL)
    @Expose
    var image_url: String = null ?: ""

    var selected = false
}