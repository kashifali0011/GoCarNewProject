package com.towsal.towsal.network.serializer.fuellevels

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

class FuelLevels : Serializable {

    @SerializedName(
        DataKeyValues.NAME
    )
    val name: String = null ?: ""

    @SerializedName(
        DataKeyValues.ID
    )
    val id: Int = null ?: 1
}