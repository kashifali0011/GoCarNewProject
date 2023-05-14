package com.towsal.towsal.network.serializer.carlist.step5

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for Car safety model
 * */
class CarSafetyInfoModel : Serializable {
    @SerializedName(DataKeyValues.CarInfo.STEP)
    @Expose
    var step: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.IS_CAR_SAFETY_QUALITY)
    @Expose
    var carSafetyCheck: Boolean = null ?: false

    val userAgreedToTermsAndConditions: Int
        get() {
            return if (carSafetyCheck)
                1
            else
                0
        }

    @SerializedName(DataKeyValues.User.USER_TYPE)
    @Expose
    var user_type: Int = null ?: 0
}