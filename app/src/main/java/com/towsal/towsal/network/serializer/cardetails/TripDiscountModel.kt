package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for trip discount on car by host
 * */
class TripDiscountModel : Serializable {
    @SerializedName(DataKeyValues.CarDetails.DAYS)
    @Expose
    var days: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.DISCOUNT)
    @Expose
    var discount: String = null ?: ""
}