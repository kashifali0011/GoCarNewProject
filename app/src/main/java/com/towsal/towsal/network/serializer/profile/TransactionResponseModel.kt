package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for transaction history
 * */
class TransactionResponseModel:Serializable {

    @SerializedName(DataKeyValues.Profile.TRANSACTION_HISTORY)
    @Expose
    var transactionhistory: TransactionHistoryModel = null ?: TransactionHistoryModel()
}