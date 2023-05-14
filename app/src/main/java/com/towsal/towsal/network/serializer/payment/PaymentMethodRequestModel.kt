package com.towsal.towsal.network.serializer.payment

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.utils.Constants.DataParsing.ACCOUNT_NUMBER
import com.towsal.towsal.utils.Constants.DataParsing.ACCOUNT_TITLE
import com.towsal.towsal.utils.Constants.DataParsing.CARD_HOLDER_NAME
import com.towsal.towsal.utils.Constants.DataParsing.CARD_NUMBER
import com.towsal.towsal.utils.Constants.DataParsing.CVV
import com.towsal.towsal.utils.Constants.DataParsing.EXPIRY
import com.towsal.towsal.utils.Constants.DataParsing.PAYMENT_BRAND
import com.towsal.towsal.utils.Constants.DataParsing.PAYMENT_METHOD_TYPE

data class PaymentMethodRequestModel(
    @SerializedName(PAYMENT_METHOD_TYPE)
    val paymentMethodType: Int,
    @SerializedName(CARD_NUMBER)
    val cardNumber: String? = null,
    @SerializedName(CARD_HOLDER_NAME)
    val cardHolderName: String? = null,
    @SerializedName(EXPIRY)
    val expiry: String? = null,
    @SerializedName(CVV)
    val cvv: String? = null,
    @SerializedName(ACCOUNT_NUMBER)
    val accountNumber: String? = null,
    @SerializedName(ACCOUNT_TITLE)
    val accountTitle: String? = null,
    @SerializedName(PAYMENT_BRAND)
    val paymentBrand: String? = null,
)