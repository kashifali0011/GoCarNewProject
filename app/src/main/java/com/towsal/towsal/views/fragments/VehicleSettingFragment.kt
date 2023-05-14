package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentVehicleSettingBinding
import com.towsal.towsal.extensions.deleteCustomerCar
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.StatusListingResponseModel
import com.towsal.towsal.network.serializer.settings.VehicleInfoSettingModel
import com.towsal.towsal.network.serializer.settings.VehicleSettingModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.activities.*
import com.towsal.towsal.views.adapters.VehicleSettingAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for vehicle settings
 * */
class VehicleSettingFragment : BaseFragment(), RecyclerViewItemClick, OnNetworkResponse,
    PopupCallback {

    var model: VehicleInfoSettingModel = VehicleInfoSettingModel()
    var carId: Int = 0
    lateinit var binding: FragmentVehicleSettingBinding
    private val settingList: ArrayList<VehicleSettingModel> = ArrayList()
    private val settingsViewModel: SettingsViewModel by viewModel()
    private val args: VehicleSettingFragmentArgs by navArgs()

    companion object {
        const val GOTO_PHOTOS = 1
        const val GOTO_VEHICLE_PROTECTION = 2
        const val SNOOZE_CAR = "snooze_car"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        carId = args.carId

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_vehicle_setting, container, false)
        binding.tvViewListingDesign.paint.strokeWidth = 2f
        binding.tvViewListingDesign.paint.style = Paint.Style.STROKE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        binding.layoutToolBar.setAsHostToolBar(
            R.string.your_car,
            null
        )
    }


    override fun onResume() {
        super.onResume()
        settingsViewModel.getVehicleInfo(
            carId = carId,
            true,
            Constants.API.VEHICLE_INFO,
            this
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.VEHICLE_INFO -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    VehicleInfoSettingModel::class.java
                )
                settingList.clear()
                setData()
            }

            Constants.API.UPDATE_CAR_STATUS -> {
                val modelResponse = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    StatusListingResponseModel::class.java
                )
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message ?: "")
        when (tag) {
            Constants.API.UPDATE_CAR_STATUS -> {
                binding.rbActive.isChecked = model.status == Constants.CarStatus.LISTED
                binding.rbSnooze.isChecked = model.status == Constants.CarStatus.SNOOZED

            }
        }
    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("SetTextI18n")
    private fun setData() {
        binding.ivCarImage.apply {
            uiHelper.loadImage(
                this,
                BuildConfig.IMAGE_BASE_URL + model.images
            )
        }
        binding.carName.text = model.make + " - " + model.model
        binding.tvLicenseNumber.text = model.plateNumber

        if (model.rating == "0.0"){
            binding.rbCarRating.visibility = View.INVISIBLE
        }else{
            binding.rbCarRating.visibility = View.VISIBLE
            binding.rbCarRating.rating = model.rating.toFloat()
        }
        binding.rbActive.isChecked = model.status == Constants.CarStatus.LISTED
        binding.rbSnooze.isChecked = model.status == Constants.CarStatus.SNOOZED


        var model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.pricing)

        if (this.model.car_pricing_complete == 0){
            model.image = R.drawable.ic_price_new_red
        }else{
            model.image = R.drawable.ic_price_new
        }
        if (this.model.car_pricing_complete == 0) {
            model.infoImg = R.drawable.ic_alert_red
        }
        settingList.add(model)


        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.details)

        if (this.model.car_details_complete == 0) {
            model.image = R.drawable.ic_details_new_red
        }else {
            model.image = R.drawable.ic_details_new
        }
        if (this.model.car_details_complete == 0) {
            model.infoImg = R.drawable.ic_alert_red
        }
        settingList.add(model)


        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.mark_unavailable_dates)
        model.image = R.drawable.ic_calender_new
        settingList.add(model)

        model = VehicleSettingModel()
        model.name = getString(R.string.location_delivery)

        model.image = R.drawable.ic_location_and_location
        settingList.add(model)

        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.guidelines_by_host)
        model.image = R.drawable.ic_guidlines_by_host_new
        settingList.add(model)

        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.distance_included)
        model.image = R.drawable.ic_included_distance_new
        settingList.add(model)

        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.trip_prefrence)
        model.image = R.drawable.ic_trip_preference_new
        settingList.add(model)

        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.photos)
        model.image = R.drawable.ic_photo_new
        settingList.add(model)


        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.vehicle_protection)
        model.image = R.drawable.ic_vehicle_production_new
        if (this.model.isProtection == 0) {
            model.infoImg = R.drawable.ic_alert_red
        }
        settingList.add(model)


        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.availability)
        model.image = R.drawable.ic_pick_and_retrun_new
        settingList.add(model)


        model = VehicleSettingModel()
        model.name = requireActivity().getString(R.string.documents)
        model.image = R.drawable.ic_document_update
        settingList.add(model)


        /*binding.constraintLayout7.isVisible =
            this.model.car_pricing_complete == 0 || this.model.car_details_complete == 0*/

        binding.recyclerView.apply {
            adapter = VehicleSettingAdapter(
                settingList,
                requireActivity(),
                uiHelper,
                this@VehicleSettingFragment
            )

        }

        binding.mainLayout.visibility = View.VISIBLE
    }

    override fun onItemClick(position: Int) {
        val bundle = Bundle()
        bundle.putInt(Constants.DataParsing.CAR_ID, model.id)
        bundle.putString(Constants.DataParsing.NAME, "${model.make} ${model.model}")
        when (position) {
            0 -> {
                uiHelper.openActivityForResult(
                    requireActivity(),
                    PricingActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVTIY_PRICING, bundle
                )
            }
            1 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    BasicCarDetailsActivity::class.java,
                    true, bundle
                )
            }

            2 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    BookingListActivity::class.java, true, bundle
                )
            }
            3 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    LocationAndDeliveryActivity::class.java,
                    true, bundle
                )
            }
            4 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    GuidelinesByHostActivity::class.java,
                    true, bundle
                )
            }

            5 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    DistanceIncludedActivity::class.java,
                    true,
                    bundle
                )
            }
            6 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    TripPreferencesActivity::class.java,
                    bundle
                )
            }
            7 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    CarPhotosActivity::class.java,
                    true, bundle
                )
            }

        /*    */

            8 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    VehicleProtectionActivity::class.java,
                    true, bundle
                )
            }

            9 -> {
                bundle.putInt(Constants.DataParsing.CAR_ID, model.id)
                uiHelper.openActivity(
                    requireActivity(),
                    HostPickAndReturnHoursActivity::class.java,
                    true, bundle
                )
            }
            10 -> {
                uiHelper.openActivity(
                    requireActivity(),
                    ActivityCarDocuments::class.java,
                    bundle
                )
            }

        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            binding.tvViewList.id -> {
                if (model.car_details_complete != 0 && model.car_pricing_complete != 0) {
                    var lat = 0.0
                    var lng = 0.0
                    var cityId = 0

                    val bundle = Bundle()
                    if (uiHelper.isPermissionAllowed(
                            requireActivity(),
                            PERMISSION
                        )
                    ) {
                        if (!gpsTracker!!.checkGPsEnabled()) {

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
                    bundle.putInt(Constants.CAR_ID, carId)
                    bundle.putInt(Constants.MOVE_VIEW_LIST, 1)
                    uiHelper.openActivityForResult(
                        requireActivity(),
                        CarDetailsActivity::class.java,
                        bundle,
                        Constants.RequestCodes.ACTIVITY_CAR_DETAIL
                    )
                } else {
                    uiHelper.showLongToastInCenter(
                        requireActivity(),
                        "Please complete car details and pricing details first"
                    )
                }
            }
            binding.ivDeleteCar.id -> {
                activity?.deleteCustomerCar(
                    this
                )
            }
            binding.rbActive.id -> {
                settingsViewModel.updateCarStatus(
                    Constants.CarStatus.LISTED,
                    carId.toString(),
                    "",
                    "",
                    true,
                    Constants.API.UPDATE_CAR_STATUS,
                    this@VehicleSettingFragment
                )
            }

            binding.rbSnooze.id -> {
                settingsViewModel.updateCarStatus(
                    Constants.CarStatus.SNOOZED,
                    carId.toString(),
                    "",
                    "",
                    true,
                    Constants.API.UPDATE_CAR_STATUS,
                    this@VehicleSettingFragment
                )
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RequestCodes.ACTIVTIY_STATUS -> {
                    val bundle = data?.getBundleExtra(Constants.DataParsing.MODEL)
                    model =
                        bundle?.getSerializable(Constants.DataParsing.MODEL) as VehicleInfoSettingModel
                }
            }
        }
    }

    override fun popupButtonClick(value: Int) {
        val bundle = Bundle()
        bundle.putSerializable(Constants.DataParsing.MODEL, model)
        uiHelper.openActivityForResult(
            requireActivity(),
            DeleteCarListingActivity::class.java,
            true,
            Constants.RequestCodes.ACTIVTIY_STATUS, bundle
        )
    }
}