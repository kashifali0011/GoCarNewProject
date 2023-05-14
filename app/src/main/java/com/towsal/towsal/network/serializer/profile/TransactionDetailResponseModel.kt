package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.trips.TripDetailModel
import java.io.Serializable

/**
 * Response model for transaction details
 * */
class TransactionDetailResponseModel : Serializable {

    @SerializedName(DataKeyValues.Profile.TRIP_DETAILS)
    @Expose
    var model: TripDetailModel = null ?: TripDetailModel()
}