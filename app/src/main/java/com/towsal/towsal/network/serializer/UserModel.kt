package com.towsal.towsal.network.serializer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Response model for user data
 * */
class UserModel {
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

    @SerializedName(DataKeyValues.User.CONFIRM_PASSWORD)
    @Expose
    var confirm_password: String = null ?: ""

    @SerializedName(DataKeyValues.User.TIME_ZONE)
    @Expose
    var timeZone: String = null ?: ""

//    @SerializedName(DataKeyValues.User.DEVICE_TOKEN)
//    @Expose
//    var deviceToken: String = null ?: ""

//    @SerializedName(DataKeyValues.DEVICE_TYPE)
//    @Expose
//    var deviceType: String = null ?: ""

//    @SerializedName(DataKeyValues.User.LANGUAGE)
//    @Expose
//    var language: String = null ?: ""
//
//    @SerializedName(DataKeyValues.User.GENDER)
//    @Expose
//    var gender: String = null ?: ""

    @SerializedName(DataKeyValues.User.ACCESS_TOKEN)
    @Expose
    var access_token: String = null ?: ""

    @SerializedName(DataKeyValues.User.CREATED_AT)
    @Expose
    var created_at: String = null ?: ""

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.User.USER_TYPE)
    @Expose
    var user_type: Int = null ?: 0

    @SerializedName(DataKeyValues.HOST_TYPE)
    @Expose
    var hostType: Int = null ?: 0

    @SerializedName(DataKeyValues.User.IS_EMAIL_VERIFIED)
    @Expose
    var is_email_verified: Boolean = null ?: false

    @SerializedName(DataKeyValues.User.IS_OTP_VERIFIED)
    @Expose
    var is_otp_verified: Boolean = null ?: false

    @SerializedName(DataKeyValues.User.IS_ACCEPT_TC)
    @Expose
    var is_accept_tc: Boolean = null ?: false

    @SerializedName(DataKeyValues.User.TOKEN_REST_PASSWORD)
    @Expose
    var token_rest_password: String = null ?: ""

    var name =""
    var imageUrl =""


}