package com.towsal.towsal.network.serializer.payment

import com.google.gson.annotations.SerializedName

data class PaymentMethodDetails(

    @SerializedName("cvv") var cvv: String? = null,
    @SerializedName("card_number") var cardNumber: String? = null,
    @SerializedName("expiry_date") var expiryDate: String? = null,
    @SerializedName("card_holder_name") var cardHolderName: String? = null,
    @SerializedName("account_number") var accountNumber: String? = null,
    @SerializedName("account_title") var accountTitle: String? = null


)