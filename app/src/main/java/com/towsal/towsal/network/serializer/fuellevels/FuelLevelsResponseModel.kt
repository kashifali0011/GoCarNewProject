package com.towsal.towsal.network.serializer.fuellevels

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues

class FuelLevelsResponseModel {
    @SerializedName(
        DataKeyValues.FUEL_LEVELS
    )
    val list: List<FuelLevels> = null ?: emptyList()
}