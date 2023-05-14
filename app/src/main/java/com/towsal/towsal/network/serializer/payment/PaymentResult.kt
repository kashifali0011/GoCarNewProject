package com.towsal.towsal.network.serializer.payment

import com.google.gson.annotations.SerializedName

data class PaymentResult(
    @SerializedName("code") var code: String = null ?: "",
    @SerializedName("description") var description: String = null ?: ""
)