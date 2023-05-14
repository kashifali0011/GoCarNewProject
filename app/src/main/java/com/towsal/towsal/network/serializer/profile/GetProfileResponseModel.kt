package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for holding user profile
 * */
class GetProfileResponseModel : Serializable {
    @SerializedName(DataKeyValues.Profile.PROFILE)
    @Expose
    var profile: ProfileModel = null ?: ProfileModel()

    @SerializedName(DataKeyValues.Profile.SUPPORT_NUMBER)
    @Expose
    var supportNumber: String = null ?: ""

}