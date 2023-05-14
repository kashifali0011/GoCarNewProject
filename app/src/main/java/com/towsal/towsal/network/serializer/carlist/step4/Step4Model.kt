package com.towsal.towsal.network.serializer.carlist.step4

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for Step 4 car listing process model
 * */
class Step4Model : Serializable {
    @SerializedName(DataKeyValues.CarInfo.CAR_PROTECTION)
    @Expose
    var vehicleProtection: VehicleProtectionPostModel = null ?: VehicleProtectionPostModel()
}