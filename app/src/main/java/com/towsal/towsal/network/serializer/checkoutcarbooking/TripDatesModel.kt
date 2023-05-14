package com.towsal.towsal.network.serializer.checkoutcarbooking

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for trips start and end date
 * */
class TripDatesModel : Serializable {

    @SerializedName(DataKeyValues.CarCheckOut.PICK_UP)
    @Expose
    var pickUp: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.DROP_OFF)
    @Expose
    var dropOff: String = null ?: ""

}