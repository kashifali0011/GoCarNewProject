package com.towsal.towsal.network.serializer.register

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request model for resetting password
 * */
class ResetPasswordModel : Serializable {
    @SerializedName(DataKeyValues.User.TOKEN_REST_PASSWORD)
    @Expose
    var token_rest_password: String = null ?: ""

    @SerializedName(DataKeyValues.User.PASSWORD)
    @Expose
    var password: String = null ?: ""

    @SerializedName(DataKeyValues.User.CONFIRM_PASSWORD)
    @Expose
    var confirm_password: String = null ?: ""

    @SerializedName(DataKeyValues.PHONE)
    @Expose
    var phone: String = null ?: ""
}