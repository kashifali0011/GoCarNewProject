package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.trips.TripsModel
import java.io.Serializable

/**
 * Response model for transactions as guest and host
 * */
class TransactionHistoryModel : Serializable {
    @SerializedName(DataKeyValues.Profile.GUEST_LIST)
    @Expose
    var guestList: ArrayList<TripsModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Profile.HOST_LIST)
    @Expose
    var hostList: ArrayList<TripsModel> = null ?: ArrayList()
}