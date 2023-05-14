package com.towsal.towsal.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.ActivitySearchCarBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.showEnableLocationSetting
import com.towsal.towsal.helper.DelegateStatic
import com.towsal.towsal.interfaces.CalendarCallback
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.home.CarSearchModel
import com.towsal.towsal.network.serializer.home.CarSearchResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.activities.SearchPlacesActivity.Companion.OTHER_FLOW
import com.towsal.towsal.views.adapters.SearchCarAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Activity class for searching cars
 * */
class SearchCarActivity : BaseActivity(), OnNetworkResponse, CalendarCallback,
    RecyclerViewItemClick {

    lateinit var binding: ActivitySearchCarBinding
    val homeViewModel: MainScreenViewModel by viewModel()
    val carDetailViewModel: CarDetailViewModel by viewModel()
    var responseModel = CarSearchResponseModel()
    var calendarDateTimeModel = CalendarDateTimeModel()
    lateinit var adapter: SearchCarAdapter
    var locFirst: Boolean = true
    var itemAt = -1
    lateinit var navController: NavController
    private var address = ""
    var bundle: Bundle? = null

    companion object {
        var carSearchModel = CarSearchModel()
    }

    private val locationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            if (MainApplication.lastKnownLocation != null) {
                gpsTracker?.setLocation(MainApplication.lastKnownLocation)
            } else {
                Handler().postDelayed({
                    gpsTracker?.setLocation(MainApplication.getLastDeviceLocation())
                    setData()
                }, 3000)
            }
        } else {
            setData()
            //return
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        if (it) {
            if (MainApplication.lastKnownLocation != null) {
                gpsTracker?.setLocation(MainApplication.lastKnownLocation)
            } else {
                Log.d("llllloooccc", "comingHere")
                Log.d(
                    "llllloooccc",
                    "lastKnownLocation = " + MainApplication.getLastDeviceLocation()
                )
                Handler().postDelayed({
                    gpsTracker?.setLocation(MainApplication.getLastDeviceLocation())
                    setData()
                }, 3000)
            }
        } else {
            showEnableLocationSetting(locationLauncher)
            //return
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        it?.let {
            if (it.resultCode == Activity.RESULT_OK) {
                bundle = it.data?.extras
                setData()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_car)
        uiHelper.hideActionBar(supportActionBar)
        DelegateStatic.searchCarActivity = this
        binding.activity = this
        bundle = intent.extras
        handleDestinationChanges()
        setData()
        setObserver()
    }

    private fun setObserver() {
        homeViewModel.carSearchModel.observe(
            this
        ) {
            var carId = carSearchModel.carTypeId
            Log.d("carId","is Is $carId")
            callSearchApi(carSearchModel)
        }
    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("SetTextI18n")
    private fun setData() {
        carSearchModel = CarSearchModel()
        if (bundle != null) {
            readingBundleData()
        } else {
            Log.d("loc", "secondCall")
            //Check if cityId is available
            var lat = 0.0
            var lng = 0.0
            var cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
            binding.search.text = getString(R.string.current_location)
            if (cityId == 0 && !uiHelper.isPermissionAllowed(this, PERMISSION)) {
                permissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                return
            }
            if (uiHelper.isPermissionAllowed(this, PERMISSION)) {

                Log.d("loc", "permissionGrantedWith0CityId")
                if (!gpsTracker!!.checkGPsEnabled()) {
                    showEnableLocationSetting(locationLauncher)
                    Log.d("looocc", "comingHere")
                    locFirst = true
                    Log.d("locationVar", "secondCall " + locFirst)
                    return
                }
                lat = gpsTracker?.getLatitude()!!
                lng = gpsTracker?.getLongitude()!!
                cityId = 0

            }

            if (lat != 0.0 && lng != 0.0) {
                Log.d("loc", "thirdCall")
                carSearchModel.lat = lat
                carSearchModel.lng = lng
                carSearchModel.cityID = 0
            }
            if (cityId != 0) {
                carSearchModel.cityID = cityId
            }

        }
        binding.search.text = address
        calendarDateTimeModel.pickUpDateServer =
            uiHelper.getTomorrowDate() + " " + uiHelper.getCurrentTimeServer()
        calendarDateTimeModel.dropOffDateServer =
            uiHelper.getDateBeforeOrAfterCurrentDate(3) + " " + uiHelper.getCurrentTimeServer()
        calendarDateTimeModel.pickupTime = uiHelper.getCurrentTime()
        calendarDateTimeModel.dropOffTime = uiHelper.getCurrentTime()
        calendarDateTimeModel.minimumDateOnlyDefault = uiHelper.currentDate
        calendarDateTimeModel.minimumDateOnly = uiHelper.currentDate
        binding.pickupDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.ServerResponseDateTimeFormat,
            calendarDateTimeModel.pickUpDateServer,
        )
        binding.dropOffDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.ServerResponseDateTimeFormat,
            calendarDateTimeModel.dropOffDateServer,
        )
        binding.dropOffDate.visibility = View.VISIBLE
        carSearchModel.date_from = calendarDateTimeModel.pickUpDateServer
        carSearchModel.date_to = calendarDateTimeModel.dropOffDateServer

        homeViewModel.setCalendarDateTimeModel(
            calendarDateTimeModel
        )
        homeViewModel.setCarSearchModel(
            carSearchModel
        )
    }

    private fun readingBundleData() {
        bundle?.let {
            if (it.containsKey(Constants.LAT)) {
                Log.d("loc", "firstCall")
                carSearchModel.lat = it.getDouble(Constants.LAT, 0.0)
                carSearchModel.lng = it.getDouble(Constants.LNG, 0.0)
                carSearchModel.cityID = 0
            }
            if (it.containsKey(Constants.CITY_ID)) {
                carSearchModel.cityID = it.getInt(Constants.CITY_ID, 0)
            }
            if (it.containsKey(Constants.CAR_TYPE_ID)) {
                carSearchModel.carTypeId = it.getInt(Constants.CAR_TYPE_ID, 0)
            }
            address =
                it.getString(Constants.ADDRESS, getString(R.string.current_location))
        }

    }

    private fun handleDestinationChanges() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.findNavController()
        this.navController = navController
        navController.addOnDestinationChangedListener { controller, destination, argument ->
            binding.cvMapAndFilter.isVisible = destination.id != R.id.filtersFragment
            binding.cvLocationToolBar.isVisible = destination.id != R.id.filtersFragment
            binding.mapView.isVisible = destination.id != R.id.mapFragment
            binding.listView.isVisible = destination.id != R.id.searchCarFragment
            binding.toolBar.setAsGuestToolBar(
                if (destination.id == R.id.filtersFragment) R.string.filter else R.string.search,
                supportActionBar
            )

        }
    }


    /**
     * Function for calling search api
     * */
    private fun callSearchApi(carSearchModel: CarSearchModel) {
        homeViewModel.getCarSearch(carSearchModel, true, Constants.API.SEARCH_CARS, this)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {
            binding.clFilter -> {
                navController.navigate(R.id.filtersFragment)
            }
            binding.pickupDate, binding.dropOffDate -> {
                carDetailViewModel.showDateTimePicker(
                    activity = this,
                    model = calendarDateTimeModel,
                    callback = this,
                    maxDate = null,
                    bookedDates = ArrayList(),
                    carAvailability = null,
                    true
                )
            }
            binding.clLocationSearch -> {
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.ADVANCE_SEARCH_PARAM,
                    responseModel.home_search_param
                )
                if (carSearchModel.carTypeId > 0){
                    bundle.putInt(Constants.CAR_TYPE_ID, carSearchModel.carTypeId)
                }
                bundle.putInt(Constants.DataParsing.FLOW_COMING_FROM, OTHER_FLOW)
                val intent = Intent(activity, SearchPlacesActivity::class.java)
                intent.putExtras(bundle)
                launcher.launch(intent)
            }
            binding.mapView -> {
                navController.navigate(R.id.mapFragment)
            }
            binding.listView -> {
                navController.navigate(R.id.action_global_searchCarFragment)
            }

        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SEARCH_CARS -> {
                responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarSearchResponseModel::class.java
                )
                homeViewModel.setSearchResponseModel(responseModel)
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {

        locFirst = if (locFirst) {
            setData()
            false
        } else {
            uiHelper.showLongToastInCenter(this, response?.message)
            true
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCalendarDateTimeSelected(model: CalendarDateTimeModel) {
        calendarDateTimeModel = model
        binding.pickupDate.text =
            DateUtil.changeDateFormat(
                Constants.ServerDateTimeFormat,
                Constants.ServerResponseDateTimeFormat,
                calendarDateTimeModel.pickUpDateServer
            )
        binding.dropOffDate.text =
            DateUtil.changeDateFormat(
                Constants.ServerDateTimeFormat,
                Constants.ServerResponseDateTimeFormat,
                calendarDateTimeModel.dropOffDateServer
            )
        binding.dropOffDate.visibility = View.VISIBLE
        //  binding.searchBtn.visibility = View.VISIBLE
        carSearchModel.date_from = calendarDateTimeModel.pickUpDateServer
        carSearchModel.date_to = calendarDateTimeModel.dropOffDateServer

        homeViewModel.setCarSearchModel(carSearchModel)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (uiHelper.isPermissionAllowed(this, PERMISSION)) {
                if (!gpsTracker!!.checkGPsEnabled()) {
                    showEnableLocationSetting(locationLauncher)
                    return
                }
                setData()
            }

        } else {
            uiHelper.showLongToastInCenter(
                this,
                "Location Permission Not Granted Check App Setting and allow location permission"
            )
        }
    }
}