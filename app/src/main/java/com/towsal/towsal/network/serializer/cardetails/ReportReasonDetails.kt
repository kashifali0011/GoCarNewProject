package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues

class ReportReasonDetails {

    @SerializedName(DataKeyValues.CarDetails.ID)
    var id : Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.NAME)
    @Expose
    var name: String = null ?: ""

    var selected = false
}