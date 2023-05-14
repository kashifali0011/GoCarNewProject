package com.towsal.towsal.network.serializer.carlist.step3

import android.net.Uri
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.utils.Constants
import java.io.Serializable

/**
 * Response model for Car photos
 * */
class CarPhotoListModel : Serializable {

    @SerializedName("filename")
    var url: String = null ?: ""

    @SerializedName(Constants.ICON)
    var iconUrl: String = null ?: ""

    @Transient
    var uri: Uri = Uri.EMPTY

    @SerializedName("reason")
    var reason: String = null ?: ""
    var name: String = null ?: ""

    @SerializedName("status")
    var status: Int = null ?: -1

    @SerializedName("type")
    var type: String = null ?: ""

    @SerializedName("sequence")
    var sequence: Int? = null ?: 0

    override fun equals(other: Any?): Boolean {
        val otherObject = other as CarPhotoListModel
        return url == otherObject.url && status == otherObject.status && sequence == otherObject.sequence
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + status
        result = 31 * result + (sequence ?: 0)
        return result
    }
}