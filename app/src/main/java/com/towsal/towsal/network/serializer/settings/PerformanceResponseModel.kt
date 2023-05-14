package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for host performance
 * */
class PerformanceResponseModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.PERFORMANCE)
    @Expose
    var performance: PerformanceModel = null ?: PerformanceModel()

}