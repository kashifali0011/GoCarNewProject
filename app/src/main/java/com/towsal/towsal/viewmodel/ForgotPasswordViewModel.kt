package com.towsal.towsal.viewmodel

import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.register.ResetPasswordModel
import com.towsal.towsal.respository.DataRepository

/**
 * View model for forgot password process
 * */
class ForgotPasswordViewModel :
    BaseViewModel() {

    /**
     * Function for reset password request
     * */
    fun resetPasswordRequest(
        value: String?,
        email: Boolean,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val map = HashMap<String?, String?>()
        if (email) {
            map[DataKeyValues.User.EMAIL] = value
        } else {
            map[DataKeyValues.PHONE] = value
        }
        dataRepository.callApi(
            dataRepository.network.apis()!!.resetPasswordRequest(map),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for reset password
     * */
    fun resetPassword(
        model: ResetPasswordModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.resetPassword(model),
            tag,
            callback,
            showLoader
        )
    }
}