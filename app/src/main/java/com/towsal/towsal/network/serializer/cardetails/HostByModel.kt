package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for the details of trips hosted by user
 * */
class HostByModel : Serializable {
    @SerializedName(DataKeyValues.CarDetails.TOTAL_TRIPS)
    @Expose
    var total_Trips: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.NAME)
    @Expose
    var name: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.JOINED)
    @Expose
    var joined: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.AVG_RESPONSE_TIME)
    @Expose
    var average_response_time: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.PROFILE_IMG)
    @Expose
    var profile_img: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.TEXT)
    @Expose
    var text: String = null ?: ""

    @SerializedName("average_rating")
    @Expose
    var averageRating: String = null ?: ""
}