package com.towsal.towsal.network.serializer.payment

import com.google.gson.annotations.SerializedName

data class PaymentsResponseModel(

    @SerializedName("id") var id: Int = null ?: 1,
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("payment_brand") var paymentBrand: String? = null,
    @SerializedName("payment_method_type") var paymentMethodType: Int? = null,
    @SerializedName("details") var details: PaymentMethodDetails = null ?: PaymentMethodDetails(),
    @SerializedName("is_default") var isDefault: Int? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("account_title") var accountTitle: String? = null,
    @SerializedName("account_number") var accountNumber: String? = null

)