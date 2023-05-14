package com.towsal.towsal.network.serializer.payment

import com.google.gson.annotations.SerializedName

data class PaymentCheckOutIdResponseModel(
    @SerializedName("checkoutId") var checkOutId: String = null ?: ""
)