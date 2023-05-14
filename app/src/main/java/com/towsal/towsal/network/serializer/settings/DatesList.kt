package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

/**
 * Response model for dates
 * */
class DatesList {
    @SerializedName("date")
    @Expose
    var date: String? = null

    @SerializedName("day")
    @Expose
    var day: String? = null
}