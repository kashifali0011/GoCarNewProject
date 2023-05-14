package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

/**
 * Response model for cars custom availability setting
 * */
class CarUnAvailableData {
    @SerializedName("id")
    @Expose
    var id: Int = null ?: 0

    @SerializedName("car_id")
    @Expose
    var carId: Int = null ?:0

    @SerializedName("day")
    @Expose
    var day: String = null ?:""

    @SerializedName("from")
    @Expose
    var from: String = null ?: ""

    @SerializedName("to")
    @Expose
    var to: String = null ?: ""

    @SerializedName("created_at")
    @Expose
    var createdAt: String = null ?: ""

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String = null ?: ""
    @SerializedName("message")
    @Expose
    var message: String = null ?: ""
    @SerializedName("is_unavailable")
    @Expose
    var isUnavailable: Int = 0
}
