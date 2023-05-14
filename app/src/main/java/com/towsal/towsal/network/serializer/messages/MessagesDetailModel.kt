package com.towsal.towsal.network.serializer.messages

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for message details
 * */
class MessagesDetailModel : Serializable {

    @SerializedName(DataKeyValues.Messages.SENDER_NAME)
    @Expose
    var sender_name: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.USER_NAME)
    @Expose
    var user_name: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.SENDER_PROFILE_IMAGE)
    @Expose
    var sender_profile_image: String = null ?: ""


    @SerializedName(DataKeyValues.Messages.MESSAGE)
    @Expose
    var message: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.MASSAGE_AT)
    @Expose
    var massage_at: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.MESSAGE_AT)
    @Expose
    var message_at: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.MASSAGE_TYPE)
    @Expose
    var massage_type: String = null ?: ""

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.Messages.MASSAGE_TYPE_ID)
    @Expose
    var massage_type_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Messages.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Messages.BOOKING_ID)
    @Expose
    var booking_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Messages.SENDER_ID)
    @Expose
    var sender_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Messages.RECIVER_ID)
    @Expose
    var reciver_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Messages.CAR_IMAGE)
    @Expose
    var car_image: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.CAR_MAKE)
    @Expose
    var car_make: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.CAR)
    @Expose
    var car: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.CAR_YEAR)
    @Expose
    var car_year: String = null ?: ""

    @SerializedName(DataKeyValues.Messages.IS_SEEN)
    @Expose
    var is_seen: Int = null ?: 0

    @SerializedName(DataKeyValues.Messages.CAR_MODEL)
    @Expose
    var car_model: String = null ?: ""

    @SerializedName(DataKeyValues.THREAD_ID)
    @Expose
    var threadId: Int = null ?: 0


    val isReadVisible: Boolean
        get() {
            return this.massage_type == "reply" && is_seen == 1
        }

    var viewType: Int = 1

}