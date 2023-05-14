package com.towsal.towsal.viewmodel

import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.respository.DataRepository

/**
 * View model for user profile
 * */
class ProfileViewModel : BaseViewModel() {

    /**
     * Function for getting user details
     * */
    fun getUserDetails(
        userId: Int,
        cityId: Int,
        lat: Double,
        lng: Double,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {

        dataRepository.callApi(
            dataRepository.network.apis()!!.getUserDetails(
                HashMap<String, Any>().apply {
                    this[DataKeyValues.CITY_ID] = cityId
                    this[DataKeyValues.USER_ID] = userId
                    this[DataKeyValues.LAT] = lat
                    this[DataKeyValues.LNG] = lng
                }
            ),
            tag,
            callback,
            showLoader
        )
    }
}