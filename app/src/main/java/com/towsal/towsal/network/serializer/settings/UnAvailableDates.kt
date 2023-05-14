package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Request model for car unavailable dates
 * */
class UnAvailableDates {
    @SerializedName("dates")
    @Expose
    var dates: List<DatesList>? = null
}