package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Request and Response model for user info
 * */
class UserInfoResponseModel:Serializable {

    @SerializedName(DataKeyValues.CarDetails.CURRENT_STEP)
    @Expose
    var current_step: Int = null ?: 1

    @SerializedName(DataKeyValues.CarDetails.STEP_TWO)
    @Expose
    var stepTwo: UserInfoStepTwoModel = null ?: UserInfoStepTwoModel()

    @SerializedName(DataKeyValues.CarDetails.STEP_THREE)
    @Expose
    var stepThree: UserInfoStepThreeModel = null ?: UserInfoStepThreeModel()

    @SerializedName(DataKeyValues.CarDetails.STEP_FOUR)
    @Expose
    var stepFour: UserInfoStepFourModel = null ?: UserInfoStepFourModel()

}