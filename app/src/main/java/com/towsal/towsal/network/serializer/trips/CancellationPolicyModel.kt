package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.home.CarHomeModel
import java.io.Serializable

/**
 * Response model for trip cancellation policy
 * */
class CancellationPolicyModel : Serializable {

    @SerializedName(DataKeyValues.Trips.REJECT_REASON)
    @Expose
    var rejectReason: ArrayList<CarHomeModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Trips.CANCEL_REASON)
    @Expose
    var cancelReason: ArrayList<CarHomeModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Trips.CANCELLATION_POLICY)
    @Expose
    var cancellationPolicy: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.READ_MORE)
    @Expose
    var readMore: String = null ?: ""
}