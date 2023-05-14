package com.towsal.towsal.network.serializer.checkoutcarbooking

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for booking confirmation
 * */
class BookingConfirmationResponseModel : Serializable {

    @SerializedName(DataKeyValues.CarCheckOut.MAKE)
    @Expose
    var make: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.MODEL)
    @Expose
    var model: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.YEAR)
    @Expose
    var year: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.HOSTED_BY)
    @Expose
    var hosted_by: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.PICK_UP)
    @Expose
    var pickUp: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.DROP_OFF)
    @Expose
    var dropOff: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.CHECKOUT_ID)
    @Expose
    var checkout_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.HOSTED_ID)
    @Expose
    var hosted_id: Int = null ?: 0

    var hostProfileImage: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL)
    @Expose
    var total: Double = null ?: 0.0
}