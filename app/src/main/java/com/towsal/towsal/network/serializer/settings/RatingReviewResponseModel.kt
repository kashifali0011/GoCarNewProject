package com.towsal.towsal.network.serializer.settings

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.utils.Constants
import java.io.Serializable
import java.text.SimpleDateFormat

/**
 * Response model for rating and reviews
 * */
class RatingReviewResponseModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.HOST_NAME)
    @Expose
    var host_name: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.HOST_AVG_RESPONSE_TIME)
    @Expose
    var host_avg_response_time: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.TOTAL_TRIPS)
    @Expose
    var total_trips: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.HOST_JOIN_DATE)
    @Expose
    var host_join_date: String = null ?: ""
        @SuppressLint("SimpleDateFormat")
        get() {
            if (field.isNotEmpty()) {
                val simpleDateFormat = SimpleDateFormat(Constants.ServerDateTimeFormat)
                val convertDateFormat = SimpleDateFormat(Constants.ResponseFormatY)
                val date = simpleDateFormat.parse(field)
                return try {
                    convertDateFormat.format(date!!)
                } catch (e: Exception) {
                    ""
                }
            }
            return field
        }

    @SerializedName(DataKeyValues.VehicleInfoSetting.HOST_OVERALL_RATING)
    @Expose
    var host_overall_rating: Float = null ?: 0f

    @SerializedName(DataKeyValues.VehicleInfoSetting.HOST_PROFILE_IMAGE)
    @Expose
    var host_profile_image: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.REVIEWS)
    @Expose
    var reviewsList: ArrayList<ReviewsModel> = null ?: ArrayList()

}