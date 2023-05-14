package com.towsal.towsal.network.serializer.messages

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues

class CreateThreadModel {

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.SENDER_ID)
    @Expose
    var senderId: String = null ?: ""



}