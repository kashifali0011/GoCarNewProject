package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request and Response model for user info step three
 * */
class UserInfoStepThreeModel:Serializable {
    
    @SerializedName(DataKeyValues.CarDetails.FIRST_NAME)
    @Expose
    var first_name: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.LAST_NAME)
    @Expose
    var last_name: String = null ?: ""


    @SerializedName(DataKeyValues.CarDetails.LICENSE_EXPIRY_DATE)
    @Expose
    var licence_exp_date: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.LICENSE_NUMBER)
    @Expose
    var licenseNumber: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.DOB)
    @Expose
    var dob: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.SNAP_OF_ID)
    @Expose
    var snap_of_id: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.SNAP_OF_DRIVING_LICENCE)
    @Expose
    var snap_of_driving_licence: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.STEP)
    var step = null ?: 3

}