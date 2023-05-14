package com.towsal.towsal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.respository.DataRepository

/**
 * View model for otp functioning
 * */
class OTPViewModel : BaseViewModel() {

    /**
     * Function for verifying otp
     * */
    fun verifyOTP(
        userId: Int,
        otp: String?,
        type: String?,
        tag: Int,
        networkCallback: OnNetworkResponse,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!
                .verifyOtp(
                    otp, userId,type
                ),
            tag,
            networkCallback,
            showLoader
        )
    }

    /**
     * Function for re sending otp
     * */
    fun resendOTP(
        userId: Int,
        tag: Int,
        networkCallback: OnNetworkResponse,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!
                .resendOtp(
                    userId
                ),
            tag,
            networkCallback,
            showLoader
        )
    }

}