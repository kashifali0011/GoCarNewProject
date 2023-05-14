package com.towsal.towsal.viewmodel

import com.towsal.towsal.network.ApiInterface
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.payment.PaymentMethodRequestModel

class PaymentsViewModel : BaseViewModel() {

    fun prepareCheckout(
        last4Digits: String,
        callback: OnNetworkResponse,
        tag: Int,
        showLoader: Boolean
    ) {
        val map = HashMap<String, String>()
        map[ApiInterface.Companion.ParamNames.LAST_4_DIGITS] = last4Digits
        dataRepository.callApi(
            dataRepository.network.apis()?.prepareCheckout(map)!!,
            tag,
            callback,
            showLoader,
        )
    }

    fun getPaymentStatus(
        citId: String,
        checkOutId: String?,
        callback: OnNetworkResponse,
        tag: Int,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()?.getPaymentStatus(citId, checkOutId)!!,
            tag,
            callback,
            showLoader,
        )
    }

    fun getCarRegistrationStatus(
        resourcePath: String,
        callback: OnNetworkResponse,
        tag: Int,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()?.getCardRegistrationStatus(resourcePath)!!,
            tag,
            callback,
            showLoader,
        )
    }

    fun getPaymentMethods(
        callback: OnNetworkResponse,
        tag: Int,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()?.getPaymentMethods()!!,
            tag,
            callback,
            showLoader,
        )
    }

    fun addPaymentMethod(
        paymentMethod: PaymentMethodRequestModel,
        tag: Int,
        onNetworkResponse: OnNetworkResponse,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()?.addPaymentMethod(paymentMethod)!!,
            tag,
            onNetworkResponse,
            showLoader,
        )
    }

    fun setDefaultCard(
        id: Int,
        tag: Int,
        onNetworkResponse: OnNetworkResponse,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()?.setDefaultPaymentMethod(
                id
            )!!,
            tag,
            onNetworkResponse,
            showLoader,
        )
    }

    fun deletePaymentMethod(
        id: Int,
        paymentMethodType: Int,
        onNetworkResponse: OnNetworkResponse,
        tag: Int,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()?.removePaymentMethod(
                id,
                paymentMethodType

            )!!,
            tag,
            onNetworkResponse,
            showLoader,
        )
    }

    fun holdPayment(
        shopperResultUrl: String,
        checkOutId: Int?,
        onNetworkResponse: OnNetworkResponse,
        tag: Int,
        showLoader: Boolean
    ) {
        dataRepository.network.apis()?.holdPayment(
            HashMap<String, Any?>().apply {
                this["shopper_redirect_url"] = shopperResultUrl
                this["checkout_id"] = checkOutId
            }
        )?.let { dataRepository.callApi(it, tag, onNetworkResponse, showLoader) }
    }
}
