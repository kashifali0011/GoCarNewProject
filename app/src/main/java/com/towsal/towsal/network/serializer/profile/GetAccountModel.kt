package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for user account information
 * */
class GetAccountModel : Serializable {

    @SerializedName(DataKeyValues.Profile.PASSWORD)
    @Expose
    var password: Boolean = null ?: false

    @SerializedName("error_type")
    @Expose
    var error_type : Int = null ?: 1

    @SerializedName(DataKeyValues.Profile.FIRST_NAME)
    @Expose
    var first_name: String = null ?: ""

    @SerializedName(DataKeyValues.Profile.LAST_NAME)
    @Expose
    var last_name: String = null ?: ""

    @SerializedName(DataKeyValues.Profile.PHONE)
    @Expose
    var phone: String = null ?: ""

    @SerializedName(DataKeyValues.Profile.EMAIL)
    @Expose
    var email: String = null ?: ""

}