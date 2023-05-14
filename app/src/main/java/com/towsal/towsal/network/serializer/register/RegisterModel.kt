package com.towsal.towsal.network.serializer.register

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request model for Creating user
 * */
class RegisterModel : Serializable {
    @SerializedName(DataKeyValues.FIRST_NAME)
    @Expose
    var firstName: String = null ?: ""

    @SerializedName(DataKeyValues.LAST_NAME)
    @Expose
    var lastName: String = null ?: ""


    @SerializedName(DataKeyValues.User.EMAIL)
    @Expose
    var email: String = null ?: ""

    @SerializedName(DataKeyValues.PHONE)
    @Expose
    var phone: String = null ?: ""


    @SerializedName(DataKeyValues.User.PASSWORD)
    @Expose
    var password: String = null ?: ""

    @SerializedName(DataKeyValues.User.TIME_ZONE)
    @Expose
    var timeZone: String = null ?: ""

    @SerializedName(DataKeyValues.User.USER_TYPE)
    @Expose
    var user_type: Int = null ?: 0

    @SerializedName(DataKeyValues.User.IS_ACCEPT_TC)
    @Expose
    var is_accept_tc: Int = null ?: 0
}