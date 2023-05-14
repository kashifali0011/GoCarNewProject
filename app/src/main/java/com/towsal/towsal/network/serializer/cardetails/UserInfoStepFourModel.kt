package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request and Response model for user info step four
 * */
class UserInfoStepFourModel:Serializable {

    @SerializedName(DataKeyValues.CarDetails.STEP)
    @Expose
    var step: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.TITLE)
    @Expose
    var title: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.CARD_NUMBER)
    @Expose
    var card_number: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.EXP_DATE)
    @Expose
    var exp_date: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.CVC)
    @Expose
    var cvc: String = null ?: ""
}