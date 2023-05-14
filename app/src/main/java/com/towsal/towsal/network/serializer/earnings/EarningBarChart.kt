package com.towsal.towsal.network.serializer.earnings

import com.google.gson.annotations.SerializedName
import java.io.Serializable
/**
 * Response model for showing data on bar chart
 * */
class EarningBarChart : Serializable {
    @SerializedName("month")
    var label: String? = null ?: ""

    @SerializedName("total")
    var yAxisValue: Float = null ?: 0f

    @SerializedName("total_count")
    var total_count: Int = null ?: 0

    @SerializedName("date")
    var date: String? = null ?: ""


}