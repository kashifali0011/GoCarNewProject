package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for host performance
 * */
class PerformanceModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.RESPONSE_RATE_PERCENTAGE)
    @Expose
    var response_rate_percentage: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.RESPONSE_RATE_TEXT)
    @Expose
    var response_rate_text: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.ACCEPTANCE_RATE_PERCENTAGE)
    @Expose
    var acceptance_rate_percentage: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.ACCEPTANCE_RATE_TEXT)
    @Expose
    var acceptance_rate_text: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.COMMITMENT_RATE_PERCENTAGE)
    @Expose
    var commitment_rate_percentage: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.COMMITMENT_RATE_TEXT)
    @Expose
    var commitment_rate_text: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.FIVE_STAR_RATING_PERCENTAGE)
    @Expose
    var five_star_rating_percentage: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.FIVE_STAR_RATING_TEXT)
    @Expose
    var five_star_rating_text: String = null ?: ""


    @SerializedName(DataKeyValues.VehicleInfoSetting.RESPONSE_RETE)
    @Expose
    var responseRate : String = null ?:""

    @SerializedName(DataKeyValues.VehicleInfoSetting.ACCEPTANCE_RATE)
    @Expose
    var acceptanceRate: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.COMMITMENT_TATE)
    @Expose
    var commitmentRate: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.FIVE_STAR_RATING)
    @Expose
    var fiveStarRating: String = null ?: ""




}