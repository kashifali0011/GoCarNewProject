package com.towsal.towsal.viewmodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class MessengerModel: Serializable {
    @SerializedName("name")
    var name: String? = ""
    @SerializedName("id")
    var id: Int? = 0

    @SerializedName("user_type")
    var user_type: String? = ""

    @SerializedName("profile")
    var profile: String? = ""

    @SerializedName("joining_date")
    var joining_date: String? = ""

    @SerializedName("total_trips")
    var total_trips: Int? = 0
}