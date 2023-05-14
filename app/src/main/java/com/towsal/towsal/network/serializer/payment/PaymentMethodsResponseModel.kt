package com.towsal.towsal.network.serializer.payment

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.utils.Constants

class PaymentMethodsResponseModel {
    @SerializedName("payment_methods")
    val list: List<PaymentsResponseModel> = null ?: emptyList()

    @SerializedName(DataKeyValues.IS_HOST)
    val isHost: Int = null ?: 0
}