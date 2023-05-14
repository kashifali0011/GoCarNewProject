package com.towsal.towsal.network.serializer.messages

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request model for sending message
 * */
class SendMessageModel : Serializable {

    @SerializedName(DataKeyValues.Messages.MESSAGE)
    @Expose
    var message: String = null ?: ""

    @SerializedName(DataKeyValues.THREAD_ID)
    @Expose
    var threadId: Int = null ?: 0
}