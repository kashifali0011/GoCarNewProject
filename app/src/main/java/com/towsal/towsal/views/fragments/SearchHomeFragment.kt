package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentSearchHomeBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cities.CitiesListModel
import com.towsal.towsal.network.serializer.home.HomeDataModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.viewmodel.LanguageSelectionViewModel
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.activities.*
import com.towsal.towsal.views.adapters.CarTypesAdapter
import com.towsal.towsal.views.adapters.CarTypessAdapter
import com.towsal.towsal.views.listeners.OnEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Fragment class for home search fragment
 * */
class SearchHomeFragment : BaseFragment(), OnNetworkResponse, PopupCallback, OnEvent {

    private var shouldResume: Boolean = true
    lateinit var binding: FragmentSearchHomeBinding
    private val homeViewModel: MainScreenViewModel by viewModel()
    private val languageSelectionViewModel: LanguageSelectionViewModel by viewModel()
    private val carDetailViewModel: CarDetailViewModel by viewModel()
    private var model = HomeDataModel()
    private var popupWindow = PopupWindow()
    private var position: Int = -1
    private var selectedPosition: Int = -1
    private var cityId: Int = 0
    private var lng = 0.0
    private var lat = 0.0
    private var cityName = ""
    private var cityModel = CitiesListModel()
    private var isCitySelected = false

    private var locationActivityLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            (activity as BaseActivity).showLoaderDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                (activity as BaseActivity).hideLoaderDialog()
                setData()
            }, 5000)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search_home,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.search.setGradientTextColor(colors)
        binding.mainLayout.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.setBadgeIcon()
        Log.d("Visibility", "onResumeCalled")

        if (shouldResume)
            setData()
        else
            shouldResume = true
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        if (isAdded) {
            when (tag) {
                Constants.API.HOME_SCREEN -> {
                    binding.mainLayout.visibility = View.VISIBLE
                    model = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        HomeDataModel::class.java
                    )
                    binding.viewAllBtn.isVisible = model.car_around_you.isNotEmpty()
                    binding.tvPlaceHolder.isVisible = model.car_around_you.isEmpty()
                    binding.rvCarsAroundYou.apply {
                        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        adapter = CarTypesAdapter(model.car_around_you, requireActivity(), uiHelper, this@SearchHomeFragment)
                    }
                    binding.rvCarTypes.apply {
                        layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        adapter =
                            CarTypessAdapter(
                                model.car_types,
                                requireActivity(),
                                uiHelper,
                                gpsTracker
                            )
                    }
                }

                Constants.API.DELETE_FAV_CAR -> {
                    model.car_around_you[position].carDetailInfo.isFavourite = 0
                    binding.rvCarsAroundYou.adapter!!.notifyItemChanged(position)
                }
                Constants.API.ADD_FAV_CAR -> {
                    model.car_around_you[position].carDetailInfo.isFavourite = 1
                    binding.rvCarsAroundYou.adapter!!.notifyItemChanged(position)
                }

                Constants.API.CITIES_LIST -> {
                    cityModel = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        CitiesListModel::class.java
                    )
                    if (cityModel.cityList.isNotEmpty()) {
                        languageSelectionViewModel.showCitySelectionPopup(
                            requireActivity(),
                            object : PopupCallback {
                                override fun popupButtonClick(value: Int) {
                                    super.popupButtonClick(value)
                                    if (cityModel.cityList.isNotEmpty()) {
                                        isCitySelected = true
                                        preferenceHelper.setIntOther(
                                            Constants.DataParsing.CITY_SELECTED_ID,
                                            cityModel.cityList[value].id
                                        )
                                        preferenceHelper.setString(
                                            Constants.DataParsing.CITY_SELECTED,
                                            cityModel.cityList[value].city
                                        )
                                        preferenceHelper.setLong(
                                            Constants.DataParsing.CITY_LAT,
                                            cityModel.cityList[value].lat.toLong()
                                        )
                                        preferenceHelper.setLong(
                                            Constants.DataParsing.CITY_LNG,
                                            cityModel.cityList[value].lng.toLong()
                                        )
                                        lat = cityModel.cityList[value].lat
                                        lng = cityModel.cityList[value].lng
                                        homeViewModel.getHomeData(
                                            lat,
                                            lng,
                                            cityModel.cityList[value].id,
                                            cityModel.cityList[value].city,
                                            true,
                                            Constants.API.HOME_SCREEN,
                                            this@SearchHomeFragment
                                        )
                                    }
                                    languageSelectionViewModel.dismissCitySelectionDialog()
                                }
                            },
                            ArrayList(cityModel.cityList.map {
                                it.city
                            })
                        )
                    }
                }
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        if (isAdded) {
            uiHelper.showLongToastInCenter(requireActivity(), response?.message)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (popupWindow.isAttachedInDecor) {
            popupWindow.dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val bundle = Bundle()
            var lat = 0.0
            var lng = 0.0
            if (uiHelper.isPermissionAllowed(requireContext(), PERMISSION)) {
                if (!gpsTracker!!.checkGPsEnabled()) {
                    showEnableLocationSetting()
                }
                lat = gpsTracker?.getLatitude()!!
                lng = gpsTracker?.getLongitude()!!
            }
            bundle.putDouble(Constants.LAT, lat)
            bundle.putDouble(Constants.LNG, lng)
            uiHelper.openActivity(
                requireActivity(),
                SearchCarActivity::class.java,
                true,
                bundle
            )
        } else {
            uiHelper.showLongToastInCenter(
                requireContext(),
                "Location permission not granted check app setting and allow location permission"
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.RequestCodes.ACTIVITY_CAR_DETAIL) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                if (model.car_around_you[selectedPosition].carDetailInfo.isFavourite !=
                    data!!.extras!!.getInt(Constants.DataParsing.FAVORITE_FLAG, 0)
                ) {
                    model.car_around_you[selectedPosition].carDetailInfo.isFavourite =
                        data.extras!!.getInt(Constants.DataParsing.FAVORITE_FLAG, 0)
                    binding.rvCarsAroundYou.adapter!!.notifyItemChanged(selectedPosition)
                }
            }
        } else if (requestCode != Constants.RequestCodes.ACTIVITY_CAR_DETAIL) {
            setData()
        }
    }


    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            uiHelper.openActivityForResult(
                requireActivity(),
                LoginActivity::class.java,
                Constants.RequestCodes.ACTIVITY_LOGIN
            )
        }
    }

    override fun openDetailsActivity(id: Int, position: Int) {
        this.selectedPosition = position
        val bundle = Bundle()
        if (uiHelper.isPermissionAllowed(
                requireActivity(),
                PERMISSION
            ) && !isCitySelected
        ) {
            if (!gpsTracker!!.checkGPsEnabled()) {
                showEnableLocationSetting()
            }
            lat = gpsTracker!!.getLatitude()
            lng = gpsTracker!!.getLongitude()
        }
        cityId =
            PreferenceHelper().getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        bundle.putString(
            Constants.DataParsing.ACTIVITY_NAME,
            SearchHomeFragment::javaClass.name
        )
        bundle.putDouble(Constants.LAT, lat)
        bundle.putDouble(Constants.LNG, lng)
        bundle.putInt(Constants.CITY_ID, cityId)
        bundle.putInt(Constants.CAR_ID, id)
        uiHelper.openActivityForResult(
            requireActivity(),
            CarDetailsActivity::class.java,
            bundle,
            Constants.RequestCodes.ACTIVITY_CAR_DETAIL
        )
    }

    override fun markFavourite(id: Int, position: Int) {
        this.position = position
        if (model.car_around_you[position].carDetailInfo.isFavourite == 1) {
            carDetailViewModel.deleteFavorite(
                id,
                true,
                Constants.API.DELETE_FAV_CAR,
                this
            )
        } else {
            carDetailViewModel.addFavorite(
                id,
                true,
                Constants.API.ADD_FAV_CAR,
                this
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setData() {
        cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        cityName = preferenceHelper.getString(Constants.DataParsing.CITY_SELECTED, "") ?: ""
        try {
            if (uiHelper.isPermissionAllowed(
                    requireContext(),
                    PERMISSION
                ) && !isCitySelected
            ) {

                if (!gpsTracker!!.checkGPsEnabled()) {
                    showEnableLocationSetting()
                }
                lat = gpsTracker?.getLatitude()!!
                lng = gpsTracker?.getLongitude()!!
                val geoCoder = Geocoder(requireActivity())
                val list = geoCoder.getFromLocation(lat, lng, 5)
                if ((list ?: emptyList()).isNotEmpty()) {
                    if (list?.get(0)?.locality != null) {
                        cityName = list[0]?.locality.toString()
                        homeViewModel.getHomeData(
                            lat,
                            lng,
                            0,
                            cityName,
                            true,
                            Constants.API.HOME_SCREEN,
                            this
                        )
                    } else {
                        if (!isCitySelected)
                            languageSelectionViewModel.getCitiesList(
                                true,
                                Constants.API.CITIES_LIST,
                                this
                            )
                        else {
                            homeViewModel.getHomeData(
                                lat,
                                lng,
                                cityId,
                                cityName,
                                true,
                                Constants.API.HOME_SCREEN,
                                this
                            )
                        }
                    }
                }
                cityId = 0
            } else {
                homeViewModel.getHomeData(
                    lat,
                    lng,
                    cityId,
                    cityName,
                    true,
                    Constants.API.HOME_SCREEN,
                    this
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            e.printStackTrace()
            Log.e("setData: ", e.message ?: "")
        }

    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
//            R.id.gotoCarList -> {
//                if (uiHelper.userLoggedIn(preferenceHelper)) {
//                    if (model.user_car_current_step == Constants.CarStep.STEP_5_COMPLETED) {
//                        val navHostFragment =
//                            requireActivity().supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
//                        val navController = navHostFragment.navController
//                        navController.navigate(R.id.bnv_host)
//                    } else
//                        uiHelper.openActivity(requireActivity(), CarListActivity::class.java, true)
//                } else {
//                    uiHelper.showPopup(
//                        requireActivity(),
//                        this,
//                        R.string.login,
//                        R.string.cancel_,
//                        R.string.login,
//                        R.string.would_login,
//                        false
//                    )
//                }
//            }
            R.id.search -> {
                //adding now for google search
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.ADVANCE_SEARCH_PARAM,
                    model.home_search_param
                )
                uiHelper.openActivity(
                    requireActivity(),
                    SearchPlacesActivity::class.java,
                    bundle
                )

            }

            R.id.viewAllBtn -> {
                (activity as BaseActivity).showLoaderDialog()
                CoroutineScope(Dispatchers.IO).launch {
                    openSearchActivity()
                }
            }

        }
    }

    /**
     * Function for opening search activity
     * */
    private fun openSearchActivity() {
        cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        if (uiHelper.isPermissionAllowed(requireContext(), PERMISSION) && !isCitySelected) {
            if (!gpsTracker!!.checkGPsEnabled()) {
                showEnableLocationSetting()
            }
            lat = gpsTracker?.getLatitude()!!
            lng = gpsTracker?.getLongitude()!!
            cityId = 0
        }
        val bundle = Bundle()
        bundle.putDouble(Constants.LAT, lat)
        bundle.putDouble(Constants.LNG, lng)
        bundle.putInt(Constants.CITY_ID, cityId)
        (activity as BaseActivity).hideLoaderDialog()
        uiHelper.openActivity(
            requireActivity(),
            SearchCarActivity::class.java,
            true,
            bundle
        )
    }

    /**
     * Function for showing enable location settings
     * */
    private fun showEnableLocationSetting() {
        requireActivity().let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                val states = response.locationSettingsStates
                if (states?.isLocationPresent!!) {
                    Log.e("GPS", "on Location")
                }
            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        Log.e("GPS", "Fail Response")
                        val intentSenderRequest: IntentSenderRequest =
                            IntentSenderRequest.Builder(
                                e.resolution.intentSender
                            ).build()
                        locationActivityLauncher.launch(intentSenderRequest)
                    } catch (sendEx: IntentSender.SendIntentException) {
                    }
                }
            }
        }
    }


}
