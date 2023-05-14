package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for reviews
 * */
class ReviewsModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.RATING_REVIEW_BY)
    @Expose
    var rating_review_by: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.RATING_REVIEW_BY_IMAGE)
    @Expose
    var rating_review_by_image: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.REVIEW)
    @Expose
    var review: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.RATING)
    @Expose
    var rating: Float = null ?: 0f

    @SerializedName(DataKeyValues.VehicleInfoSetting.JOIN_DATE)
    @Expose
    var join_date: String = null?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.REVIEWED_AT)
    @Expose
    var reviewAt: String = null?: ""

}