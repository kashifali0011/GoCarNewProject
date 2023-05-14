package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for guest reviews
 * */
class GuestReviewsModel:Serializable {
    @SerializedName(DataKeyValues.VehicleInfoSetting.RATING_REVIEW_BY)
    @Expose
    var rating_review_by: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR)
    @Expose
    var car :String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.LICENCE_PLATE_NUMBER)
    @Expose
    var licence_plate_no : String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.RATING_REVIEW_BY_IMAGE)
    @Expose
    var rating_review_by_image: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.REVIEW)
    @Expose
    var review: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.RATING)
    @Expose
    var rating: Float = null ?: 0f

}