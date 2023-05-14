package com.towsal.towsal.viewmodel

import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.respository.DataRepository

/**
 * View model for login process
 * */
class LoginViewModel : BaseViewModel() {

    /**
     * Function for login user to the app
     * */
    fun getLoginData(
        userModel: UserModel, fcmToken: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.loginApi(userModel,fcmToken),
            tag,
            callback,
            showLoader
        )
    }
}