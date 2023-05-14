package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for trips
 * */
class TripResponseModel:Serializable {
    @SerializedName(DataKeyValues.Trips.BOOKING_DETAILS)
    @Expose
    var bookingDetails: TripDetailModel = null ?: TripDetailModel()

}