package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for guidelines
 * */
class GuidelinesHostResponseModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_GUIDELINES_BY_HOST)
    @Expose
    var model: GuidelinesByHostModel = null ?: GuidelinesByHostModel()
}