package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for user profile
 * */
class ProfileModel : Serializable {
    @SerializedName(DataKeyValues.Profile.NAME)
    @Expose
    var name: String = null ?: ""

    @SerializedName(DataKeyValues.Profile.IMAGE)
    @Expose
    var image: String = null ?: ""
}