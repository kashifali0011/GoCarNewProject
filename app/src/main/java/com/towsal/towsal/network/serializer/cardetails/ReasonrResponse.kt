package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

class ReasonrResponse : Serializable {
    @SerializedName("report_reasons")
    @Expose
    var reportReasonDetails: ArrayList<ReportReasonDetails> = null ?: ArrayList()


}