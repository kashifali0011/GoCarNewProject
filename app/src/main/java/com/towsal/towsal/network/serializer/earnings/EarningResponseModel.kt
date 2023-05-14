package com.towsal.towsal.network.serializer.earnings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for host earnings
 * */
class EarningResponseModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.GRAPH_DATA)
    @Expose
    var barChartValues: ArrayList<EarningBarChart> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.TOTAL_REVENUE)
    @Expose
    var total_revenue: String? = null

    @SerializedName(DataKeyValues.VehicleInfoSetting.TOTAL_BOOKING)
    @Expose
    var total_booking: String = null ?: "0"

    @SerializedName(DataKeyValues.VehicleInfoSetting.PREV_DATE)
    @Expose
    var prev_date: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.NEXT_DATE)
    @Expose
    var next_date: String = null ?: ""
}