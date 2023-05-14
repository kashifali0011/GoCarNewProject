package com.towsal.towsal.network.serializer.messages

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.viewmodel.MessengerModel
import java.io.Serializable

class DateAndMessageList : Serializable {

    @SerializedName(DataKeyValues.Messages.DATE)
    @Expose
    var date: String = null?: ""

    @SerializedName(DataKeyValues.Messages.MESAAGES_LIST)
    @Expose
    var messagesList: ArrayList<MessagesDetailModel> = null ?: ArrayList()



    /*@SerializedName(DataKeyValues.Messages.MESAAGES_LIST)
    @Expose
    var messagesList: ArrayList<GetMessageDetails> = null ?: ArrayList()*/



}