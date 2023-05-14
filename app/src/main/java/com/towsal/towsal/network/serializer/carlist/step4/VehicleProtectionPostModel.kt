package com.towsal.towsal.network.serializer.carlist.step4

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import okhttp3.MultipartBody
import java.io.Serializable

/**
 * Response model for Vehicle protection policy
 * */
class VehicleProtectionPostModel : Serializable {
    @SerializedName(DataKeyValues.CarInfo.STEP)
    @Expose
    var step: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.HAVE_INSURANCE)
    @Expose
    var have_insurance: Boolean = null ?: true      //it was false

    @SerializedName(DataKeyValues.CarInfo.INSURANCE_NUMBER)
    @Expose
    var insurance_number: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.DATE_OF_EXP)
    @Expose
    var date_of_exp: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.LIABILITY_COVERED)
    @Expose
    var liability_covered: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.INSURANCE_COST_COVERED)
    @Expose
    var insurance_covered_cost: Boolean = null ?: false

    @SerializedName(DataKeyValues.CarInfo.SNAP_INSURANCE)
    @Expose
    var snap_insurance: String = null ?: ""

    @Transient
    var insuranceImage: MultipartBody.Part? = null

}