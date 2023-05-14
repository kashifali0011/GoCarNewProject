package com.towsal.towsal.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.towsal.towsal.network.ApiInterface
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.carlist.step2.CarAvailabilityPostModel
import com.towsal.towsal.network.serializer.settings.*

/**
 * View model for settings module
 * */
class SettingsViewModel : BaseViewModel() {

    private val _myCarsList = MutableLiveData<MutableList<VehicleInfoSettingModel>>()
    val myCasList: LiveData<MutableList<VehicleInfoSettingModel>> = _myCarsList

    /**
     * Function for getting user listed vehicle info
     * */
    fun getVehicleInfo(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getVehicleInfo(
                carId
            ),
            tag,
            callback,
            showLoader
        )
    }


    /**
     * Function for getting user listed vehicle price details
     * */
    fun getCarPricing(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarPricing(
                carId
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving user listed vehicle pricing
     * */
    fun saveCarPricing(
        carId: Int,
        model: CarPriceModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hashMap = HashMap<String, Any?>()
        hashMap[ApiInterface.Companion.ParamNames.DEFAULT_PRICE] = model.defaultPrice
        hashMap[ApiInterface.Companion.ParamNames.THREE_DAY_PRICE] =
            model.threeDayDiscountPrice.toString()
        hashMap[ApiInterface.Companion.ParamNames.SEVEN_DAY_PRICE] =
            model.sevenDayDiscountPrice.toString()
        hashMap[ApiInterface.Companion.ParamNames.THIRTY_DAY_PRICE] =
            model.thirtyDayDiscountPrice.toString()
        val hashMap2 = HashMap<String, String?>()
        for (i in model.customPricingList) {
            Log.e("saveCarPricing: ", i.key)
            hashMap2[i.key] = i.price
        }
        hashMap[ApiInterface.Companion.ParamNames.CUSTOM_PRICES] = hashMap2
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveCarPricing(
                carId,
                hashMap
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for updating user listed vehicle status
     * */
    fun updateCarStatus(
        status: Int,
        id: String,
        from: String,
        to: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.updateCarStatus(
                id,
                status,
                from,
                to
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting car delete reasons
     * */
    fun getCarDeleteReasonList(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarDeleteReason(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for deleting user listed vehicle
     * */
    fun deleteCarList(
        carId: Int,
        list: ArrayList<Int>,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.deleteCarListing(carId, list),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting user listed vehicle calendar
     * */
    fun getCarCalendar(
        showLoader: Boolean,
        carId: Int,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarCalendar(carId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for changing status of user listed vehicle dates to unavailable
     * */
    fun markUnavailable(
        carId: Int,
        model: UnAvailableDates,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.markUnavailableDate(
                carId,
                model
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting getting car preferences
     * */
    fun getCarPreferences(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarPreferences(
                carId
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving trip preferences data
     * */
    fun sendTripPreferenceData(
        carId: Int,
        data: CarAvailabilityPostModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveCarTripPreferences(
                carId,
                data
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting user listed car guidelines
     * */
    fun getGuidelines(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getGuideLinesByHost(carId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving user listed vehicle guidelines
     * */
    fun saveGuidelines(
        carId: Int,
        text: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveGuideLines(
                carId,
                text
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting distance included
     * */
    fun getDistanceIncluded(
        showLoader: Boolean,
        tag: Int,
        carId: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getDistanceIncluded(carId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving distance included info
     * */
    fun saveDistanceIncludedInfo(
        carId: Int,
        distanceincluded_id: Int,
        unlimited_distance: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!
                .saveDistanceInfo(carId, distanceincluded_id, unlimited_distance),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting user listed vehicle basic details and features
     * */
    fun getCarDetail(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarDetail(carId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving user listed vehicle basic details and features
     * */
    fun saveBasicCarDetail(
        carId: Int,
        model: BasicCarPostModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveCarDetail(
                carId,
                model
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting user listed vehicle location for delivery
     * */
    fun getCarLocationDelivery(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarLocationDelivery(carId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving user listed vehicle location for delivery
     * */
    fun saveCarLocationDelivery(
        carId: Int,
        model: CarLocationDeliveryModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveCarLocationDelivery(carId, model),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting user listed vehicle performance setting
     * */
    fun getPerformaceSetting(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getPerformanceData(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting user listed vehicle reviews
     * */
    fun getHostReview(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getHostReview(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting guest reviews
     * */
    fun getGuestReview(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getGuestReview(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Getting guest reviews with id
     * */
    fun getGuestReviewWithId(
        id: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getGuestReview(id),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Getting host reviews with id
     * */
    fun getHostReviewWithId(
        id: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getHostReview(id),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Getting earning report of user listed vehicle
     * */
    fun getEarningReport(
        enumType: Int, date: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getEarningReport(enumType, date),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Changing user listed vehicle status of always available
     * */
    fun carAlwaysAvailable(
        carId: Int,
        is_always_available: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.carAlwaysAvailable(carId, is_always_available),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Getting user listed vehicle car availability information
     * */
    fun getCarAvailabilityInfo(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarAvailabilityInfo(carId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Setting user listed vehicle availability
     * */
    fun setCarAvailability(
        model: CarAvailabilityModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.setCarUnAvailability(model),
            tag,
            callback,
            showLoader
        )
    }

    fun getMyVehicles(showLoader: Boolean, tag: Int, callback: OnNetworkResponse) {

        dataRepository.callApi(
            dataRepository.network.apis()!!.getMyVehicles(),
            tag,
            callback,
            showLoader
        )

    }

    fun setMyCars(
        myCarsList: MutableList<VehicleInfoSettingModel>
    ) {
        _myCarsList.postValue(myCarsList)
    }
}