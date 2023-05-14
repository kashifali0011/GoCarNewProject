package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request and Response model for user info step two
 * */
class UserInfoStepTwoModel : Serializable {

    @SerializedName(DataKeyValues.CarDetails.PROFILE_IMAGE)
    @Expose
    var profile_image: String = null ?: ""

}