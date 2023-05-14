package com.towsal.towsal.viewmodel

import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.register.RegisterModel
import com.towsal.towsal.respository.DataRepository

/**
 * View model for user registration process
 * */
class RegistrationViewModel : BaseViewModel() {

    /**
     * Function for registering user in the app
     * */
    fun registerUser(
        registerModel: RegisterModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.registerApi(registerModel),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for sending a request for resend email
     * */
    fun resendEmail(
        userId:Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ){
        dataRepository.callApi(
            dataRepository.network.apis()!!.resendEmail(userId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for verifying email
     * */
    fun verifyEmail(
        fcm_token:String,
        token:String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ){
        dataRepository.callApi(
            dataRepository.network.apis()!!.verifyEmail(fcm_token,token),
            tag,
            callback,
            showLoader
        )
    }

}