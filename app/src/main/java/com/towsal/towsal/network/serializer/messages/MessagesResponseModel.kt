package com.towsal.towsal.network.serializer.messages

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.trips.PaginationModel
import com.towsal.towsal.viewmodel.MessengerModel
import java.io.Serializable

/**
 * Response model for messages
 * */
class MessagesResponseModel : Serializable {
    @SerializedName(DataKeyValues.Messages.MESAAGES_LIST)
    @Expose
    var messagesList: ArrayList<MessagesDetailModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.MESSENGER)
    var messenger: MessengerModel = MessengerModel()

    @SerializedName(DataKeyValues.Trips.PAGINATION)
    @Expose
    var pagination: PaginationModel = null ?: PaginationModel()
}