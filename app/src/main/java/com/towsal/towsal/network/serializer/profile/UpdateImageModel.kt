package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for profile image
 * */
class UpdateImageModel : Serializable {
    @SerializedName(DataKeyValues.Profile.PROFILE_IMAGE)
    @Expose
    var profile_image: String = null ?: ""
}