package com.towsal.towsal.network.serializer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Base response model for collecting data from server
 * */
class BaseResponse : Serializable {
    @SerializedName(DataKeyValues.RESPONSE_CODE)
    @Expose
    var code = 0

    @SerializedName(DataKeyValues.RESPONSE_MESSAGE)
    @Expose
    var message: String = ""

    @SerializedName(DataKeyValues.DATA)
    @Expose
    var dataObject: Any = null ?: Object()
}