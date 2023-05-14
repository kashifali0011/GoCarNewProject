package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues

data class MyCarsResponseModel(
    @SerializedName(DataKeyValues.VehicleInfoSetting.CARS_LIST)
    val myCarsList: MutableList<VehicleInfoSettingModel> = null ?: mutableListOf()
)