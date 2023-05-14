package com.towsal.towsal.network.serializer.settings

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.utils.Constants
import java.io.Serializable
import java.text.SimpleDateFormat

/**
 * Response model for guest rating reviews
 * */
class GuestRatingReviewsModel  : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.GUEST_NAME)
    @Expose
    var guest_name: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.GUEST_AVG_RESPONSE_TIME)
    @Expose
    var guest_avg_response_time: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.TOTAL_TRIPS)
    @Expose
    var total_trips: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.GUEST_JOIN_DATE)
    @Expose
    var guest_join_date: String = null ?: ""
        @SuppressLint("SimpleDateFormat")
        get() {
            if (field.isNotEmpty()) {
                val simpleDateFormat = SimpleDateFormat(Constants.ServerDateTimeFormat)
                val convertDateFormat = SimpleDateFormat(Constants.ResponseFormat)
                val date = simpleDateFormat.parse(field)
                return try {
                    convertDateFormat.format(date!!)
                } catch (e: Exception) {
                    ""
                }
            }
            return field
        }

    @SerializedName(DataKeyValues.VehicleInfoSetting.GUEST_OVERALL_RATE)
    @Expose
    var guest_overall_rate: Float = null ?: 0f

    @SerializedName(DataKeyValues.VehicleInfoSetting.GUEST_PROFILE_IMAGE)
    @Expose
    var guest_profile_image: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.REVIEWS)
    @Expose
    var reviewsList: ArrayList<GuestReviewsModel> = null ?: ArrayList()

}