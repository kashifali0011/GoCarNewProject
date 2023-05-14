package com.towsal.towsal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.home.CarSearchModel
import com.towsal.towsal.network.serializer.home.CarSearchResponseModel
import com.towsal.towsal.network.serializer.messages.SendMessageModel
import com.towsal.towsal.network.serializer.profile.TransactionHistoryModel
import com.towsal.towsal.network.serializer.trips.FilterTripsModel
import com.towsal.towsal.respository.DataRepository

/**
 * View model for main screen module
 * */
class MainScreenViewModel : BaseViewModel() {


    private val _carSearchResponseModel: MutableLiveData<CarSearchResponseModel> =
        MutableLiveData()
    val carSearchResponseModel: LiveData<CarSearchResponseModel> =
        _carSearchResponseModel

    private val _carSearchModel: MutableLiveData<CarSearchModel> =
        MutableLiveData()
    val carSearchModel: LiveData<CarSearchModel> =
        _carSearchModel

    private val _calendarDateTimeModel: MutableLiveData<CalendarDateTimeModel> =
        MutableLiveData()
    val calendarDateTimeModel: LiveData<CalendarDateTimeModel> =
        _calendarDateTimeModel

    private val _transactionModel: MutableLiveData<TransactionHistoryModel> =
        MutableLiveData()

    val transactionModel: LiveData<TransactionHistoryModel> =
        _transactionModel

    private val _filters: MutableLiveData<ArrayList<FilterTripsModel>> =
        MutableLiveData()

    val filters: LiveData<ArrayList<FilterTripsModel>> =
        _filters

    /**
     * Function for getting home screen data
     * */
    fun getHomeData(
        lat: Double,
        lng: Double,
        cityId: Int,
        cityName: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        // clear()
        val map: HashMap<String, Any> = HashMap()
        if (cityId == 0) {
            map[DataKeyValues.LAT] = lat
            map[DataKeyValues.LNG] = lng
        } else {
            map[DataKeyValues.Home.CITY_ID] = cityId
        }
        map[DataKeyValues.CITY_NAME] = cityName
        dataRepository.callApi(
            dataRepository.network.apis()!!.homeData(map),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for filtering cars
     * */
    fun getCarSearch(
        carSearchModel: CarSearchModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val map: HashMap<String, Any> = HashMap()
        if (carSearchModel.cityID != 0) {
            map[DataKeyValues.Home.CITY_ID] = carSearchModel.cityID
        } else if (carSearchModel.lat != 0.0) {
            map[DataKeyValues.LAT] = carSearchModel.lat
            map[DataKeyValues.LNG] = carSearchModel.lng
        }
        if (carSearchModel.carTypeId != 0) {
            map[DataKeyValues.Home.CAR_TYPE_ID] = carSearchModel.carTypeId
        }

        if (carSearchModel.car_location != 0) {
            map[DataKeyValues.Home.CAR_LOCATION] = carSearchModel.car_location
        }
        if (carSearchModel.delivery_fee.isNotEmpty()) {
            map[DataKeyValues.Home.DELIVERY_FEE] = carSearchModel.delivery_fee
        }
        if (carSearchModel.sort_by != 0) {
            map[DataKeyValues.Home.SORT_BY] = carSearchModel.sort_by
        }
        if (carSearchModel.year > 0) {
            map[DataKeyValues.Home.YEAR] = carSearchModel.year
        }
        if (carSearchModel.make != 0) {
            map[DataKeyValues.Home.MAKE] = carSearchModel.make
        }
        if (carSearchModel.model != 0) {
            map[DataKeyValues.Home.MODEL] = carSearchModel.model
        }
        if (carSearchModel.odometer != 0) {
            map[DataKeyValues.Home.ODOMETER] = carSearchModel.odometer
        }
        if (carSearchModel.price.isNotEmpty()) {
            map[DataKeyValues.Home.PRICE] = carSearchModel.price
        }
        if (carSearchModel.bookInstantly != 0)
            map[DataKeyValues.Home.IS_BOOKED_IMMEDIATELY] = carSearchModel.bookInstantly

        if (carSearchModel.distance_id != 0)
            map[DataKeyValues.Home.DISTANCE_ID] = carSearchModel.distance_id

        if (carSearchModel.date_from.isNotEmpty())
            map[DataKeyValues.Home.DATE_FROM] = carSearchModel.date_from

        if (carSearchModel.date_to.isNotEmpty())
            map[DataKeyValues.Home.DATE_TO] = carSearchModel.date_to

        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarSearch(map),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for logging out the user from application
     * */
    fun logout(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.logout(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting un read messages
     * */
    fun getMessages(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getMessages(),
            tag,
            callback,
            showLoader
        )
    }



    /**
     * Function for getting un read messages
     * */
    fun getMessagesNew(
        pageNumber : Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getMessagesNew(pageNumber),
            tag,
            callback,
            showLoader
        )
    }


    /**
     * Function for getting messages of the thread
     * */
    fun getMessageDetail(
        carCheckoutId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getMessageDetail(carCheckoutId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting messages of the thread
     * */
    fun getMessageDetailNew(
        threadId: Int,
        pageNumber: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getMessageDetailNew(pageNumber , threadId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for sending message in the thread
     * */
    fun sendMessage(
        model: SendMessageModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.sendMessageThread(model),
            tag,
            callback,
            showLoader
        )
    }

    fun createThreadId(
        receiverId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ){

        dataRepository.callApi(
            dataRepository.network.apis()!!.createThread(receiverId),
            tag,
            callback,
            showLoader
        )

    }

    /**
     * Function for getting profile data
     * */
    fun getProfile(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getProfile(),
            tag,
            callback,
            showLoader
        )
    }



    /**
     * Function for updating account information
     * */
    fun sendMessage(
        message: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.sendMessage(message),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for updating account information
     * */
    fun sendMessageWithId(
        message: String,
        id: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.sendMessageWithId(message , id),
            tag,
            callback,
            showLoader
        )
    }


    /**
     * Function for updating profile image
     * */
    fun updateImage(
        uri: String?, showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.updateImage(
                uiHelper.multipartImage(uri!!, "profile_image")
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting account information
     * */
    fun getAccountInfo(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getAccountInfo(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for updating account information
     * */
    fun updateAccount(
        firstName: String,
        lastName: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.accountUpdate(firstName, lastName),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting transaction history
     * */
    fun getTransactionList(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getTransactionHistory(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting transaction details
     * */
    fun getTransactionDetail(
        id: Int,
        viewType: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getTransactionDetail(id, viewType),
            tag,
            callback,
            showLoader
        )
    }


    /**
     * Function for getting un read notifications
     * */
    fun unReadNotification(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.unReadNotification(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting makes of cars
     * */
    fun getMakes(
        yearId: Int,
        callback: OnNetworkResponse,
        tag: Int
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.makeSelection(yearId),
            tag,
            callback,
            false
        )
    }

    /**
     * Function for getting models of cars
     * */
    fun getModels(
        yearId: Int,
        makeId: Int,
        callback: OnNetworkResponse,
        tag: Int
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.modelSelection(yearId, makeId),
            tag,
            callback,
            false
        )
    }

    /**
     * Function for changing message status to read
     * */
    fun seenMessage(
        messageId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.seenMessage(messageId),
            tag,
            callback,
            showLoader
        )
    }

    fun getCarSearchAttributes(tag: Int, callBack: OnNetworkResponse, showLoader: Boolean) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getSearchCarAttributes(),
            tag,
            callBack,
            showLoader
        )
    }

    fun setSearchResponseModel(
        carSearchResponseModel: CarSearchResponseModel
    ) {
        _carSearchResponseModel.postValue(carSearchResponseModel)
    }


    fun setCarSearchModel(carSearchModel: CarSearchModel) {
        _carSearchModel.postValue(carSearchModel)
    }

    fun setCalendarDateTimeModel(
        calendarDateTimeModel: CalendarDateTimeModel
    ){
        _calendarDateTimeModel.postValue(calendarDateTimeModel)
    }

    fun setFilters(
        filters: ArrayList<FilterTripsModel>
    ){
        _filters.postValue(filters)
    }

    fun setTransactionModel(
        transactionModel: TransactionHistoryModel
    ){
        _transactionModel.postValue(transactionModel)
    }

}

