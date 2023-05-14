package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityTripPreferencesBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.step2.CarAvailabilityPostModel
import com.towsal.towsal.network.serializer.settings.GetCarPreferencesModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for trip preferences
 * */
class TripPreferencesActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityTripPreferencesBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var model = GetCarPreferencesModel()
    var carId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trip_preferences)
        binding.activity = this
        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        actionBarSetting()
        settingsViewModel.getCarPreferences(carId, true, Constants.API.GET_CAR_PREFERENCES, this)
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.trip_prefrence,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.next -> {
                when {
                    model.advance_notice_list.isEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.advance_notice_hour_msg)
                        )
                    }
                    binding.spMinTripDuration.selectedItemPosition == 0 -> {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.min_hour_err_msg)
                        )
                    }
                    binding.spMaxTripDuration.selectedItemPosition == 0 -> {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.max_hour_err_msg)
                        )
                    }
                    else -> {
                        val postModel = CarAvailabilityPostModel()
                        postModel.is_delivery = if (binding.scDelivery.isChecked) 1 else 0
                        postModel.is_pickup_return = if (binding.scBookInstantly.isChecked) 1 else 0
                        postModel.adv_notice_trip_start_id =
                            model.advance_notice_list[binding.spNoticePeriod.selectedItemPosition ].id
                        postModel.min_trip_dur =
                            "" + model.min_hour_list[binding.spMinTripDuration.selectedItemPosition - 1].id
                        postModel.max_trip_dur =
                            "" + model.max_hour_list[binding.spMaxTripDuration.selectedItemPosition - 1].id

                        settingsViewModel.sendTripPreferenceData(
                            carId,
                            postModel,
                            true,
                            Constants.API.SAVE_CAR_PREFERENCES,
                            this
                        )
                    }
                }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_PREFERENCES -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    GetCarPreferencesModel::class.java
                )
                setData()
            }
            Constants.API.SAVE_CAR_PREFERENCES -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
        }
    }


    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        binding.mainLayout.visibility = View.VISIBLE
        binding.noteCarAvailability.text =
            uiHelper.spanString(
                getString(R.string.not_msg_car_availablity),
                getString(R.string.note),
                R.font.inter_bold, this
            )
        val minList = ArrayList<String>()
        minList.add(getString(R.string.minimum_trip_duration))
        var selectedIndexMinList = -1
        for (i in model.min_hour_list.indices) {
            minList.add(model.min_hour_list[i].name + " " + getString(R.string.hours))
            if (model.min_hour_list[i].id == model.car_availability.min_trip_dur.id) {
                selectedIndexMinList = i
            }
        }
        uiHelper.setSpinner(this, minList, binding.spMinTripDuration)
        binding.spMinTripDuration.setSelection(selectedIndexMinList + 1)

        val maxList = ArrayList<String>()
        maxList.add(getString(R.string.maximum_trip_duration))
        var selectedIndexMaxList = -1
        for (i in model.max_hour_list.indices) {
            val text = if (i == 0) {
                getString(R.string.day)
            } else {
                getString(R.string.days)
            }
            maxList.add(model.max_hour_list[i].name + " " + text)
            if (model.max_hour_list[i].id == model.car_availability.max_trip_dur.id) {
                selectedIndexMaxList = i
            }
        }
        uiHelper.setSpinner(this, maxList, binding.spMaxTripDuration)
        binding.spMaxTripDuration.setSelection(selectedIndexMaxList + 1)
        binding.scDelivery.isChecked =
            model.car_availability.is_delivery == 1 && model.car_availability.delivery_status == 1
        binding.scBookInstantly.isChecked = model.car_availability.is_pickup_return == 1
        binding.llDelivery.isVisible = model.car_availability.delivery_status == 1

        val list = ArrayList<String>()
        var selectedIndex = 0
        model.advance_notice_list.mapTo(list) {
            it.name
        }
        selectedIndex = model.advance_notice_list.indexOfFirst {
            it.id == model.car_availability.adv_notice_trip_start.id
        }
        uiHelper.setSpinner(this, list, binding.spNoticePeriod)
        binding.spNoticePeriod.setSelection(selectedIndex + 0)
    }

}