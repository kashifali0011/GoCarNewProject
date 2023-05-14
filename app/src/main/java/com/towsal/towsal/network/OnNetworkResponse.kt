package com.towsal.towsal.network

import com.towsal.towsal.network.serializer.BaseResponse

/**
 * Interface for handling network responses
 * */
interface OnNetworkResponse {

    /**
    * Function for handling success response
    * */
    fun onSuccess(
        response: BaseResponse?,
        tag: Any?
    )

    /**
     * Function for handling failure response
     * */
    fun onFailure(
        response: BaseResponse?,
        tag: Any?
    )
}