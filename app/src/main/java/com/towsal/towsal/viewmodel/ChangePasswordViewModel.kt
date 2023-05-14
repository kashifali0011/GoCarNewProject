package com.towsal.towsal.viewmodel

import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.respository.DataRepository

/**
 * View model for change password process
 * */
class ChangePasswordViewModel :
    BaseViewModel() {

    /**
     * Function for change password
     * */
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.changePassword(oldPassword,newPassword),
            tag,
            callback,
            showLoader
        )
    }
}