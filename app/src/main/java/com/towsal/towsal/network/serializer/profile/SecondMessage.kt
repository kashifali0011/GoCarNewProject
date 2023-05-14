package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues

class SecondMessage {

    @SerializedName("message")
    @Expose
    var message: String = null ?: ""
}