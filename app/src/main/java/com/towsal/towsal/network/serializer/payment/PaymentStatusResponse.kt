package com.towsal.towsal.network.serializer.payment

import com.google.gson.annotations.SerializedName

data class PaymentStatusResponse(
    @SerializedName("result") var result: PaymentResult = null ?: PaymentResult(),
    @SerializedName("transaction_no") var transactionNo: Int = null ?: 0
)