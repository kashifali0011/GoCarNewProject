package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityHostPickAndReturnHoursBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.CarUnAvailableDatesResponse
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.CarUnAvailableDatesAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for host pickup and return hours
 * */
class HostPickAndReturnHoursActivity : BaseActivity(), OnNetworkResponse, PopupCallback {

    lateinit var binding: ActivityHostPickAndReturnHoursBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var datesModel = CarUnAvailableDatesResponse()
    var carId = 0
    var showToast = 1
    var isFirst = true
    var isAlwaysAvailable = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_host_pick_and_return_hours)
        binding.activity = this
        actionBarSetting()
    }

    override fun onResume() {
        super.onResume()
        isFirst = true
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            carId = intent.extras!!.getInt(Constants.DataParsing.CAR_ID, 0)
        }
        settingsViewModel.getCarAvailabilityInfo(
            carId,
            true,
            Constants.API.GET_CAR_AVAILABILITY_INFO,
            this
        )
        //calling the always available api
        binding.btnAlwaysAvailable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isAlwaysAvailable = 1
                if (isFirst) {
                    setAlwaysAvailable()
                } else {
                    uiHelper.showPopup(
                        activity = this,
                        callback = this,
                        positiveButtonId = R.string.yes,
                        negativeButtonId = R.string.no,
                        titleId = R.string.confirmation,
                        messageId = R.string.like_to_book_car_always_available,
                        isLogout = false
                    )
                }
            } else {
                if (isAlwaysAvailable != 0) {
                    isAlwaysAvailable = 0
                    settingsViewModel.carAlwaysAvailable(
                        carId,
                        isAlwaysAvailable,
                        true,
                        Constants.API.CAR_IS_ALWAYS_AVAILABLE,
                        this
                    )
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }

        }
    }

    /**
     * Function for setting up host is always available
     * */
    private fun setAlwaysAvailable() {
        settingsViewModel.carAlwaysAvailable(
            carId,
            isAlwaysAvailable,
            true,
            Constants.API.CAR_IS_ALWAYS_AVAILABLE,
            this
        )
        binding.recyclerView.visibility = View.GONE
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.availability,
            supportActionBar
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.CAR_IS_ALWAYS_AVAILABLE -> {
                /*Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    BaseResponse::class.java
                )*/
                if (showToast != 0) {
                    uiHelper.showLongToastInCenter(this, response?.message)
                }

                if (isAlwaysAvailable == 0) {
                    settingsViewModel.getCarAvailabilityInfo(
                        carId,
                        true,
                        Constants.API.GET_CAR_AVAILABILITY_INFO,
                        this
                    )
                }

            }
            Constants.API.GET_CAR_AVAILABILITY_INFO -> {
                datesModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarUnAvailableDatesResponse::class.java
                )

                if (datesModel.isAlwaysAvailable == 1) {
                    isAlwaysAvailable = 1
                    binding.btnAlwaysAvailable.isChecked = true
                    binding.recyclerView.visibility = View.GONE
                    showToast = 0
                } else if (datesModel.isAlwaysAvailable == 0) {
                    isAlwaysAvailable = 0
                    showToast = 0
                    binding.btnAlwaysAvailable.isChecked = false
                    binding.recyclerView.visibility = View.VISIBLE
                }
                if (isFirst)
                    isFirst = false


                //binding.nestedScrollableRecyclerView.visibility = View.VISIBLE
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    this@HostPickAndReturnHoursActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                binding.recyclerView.adapter =
                    CarUnAvailableDatesAdapter(
                        datesModel.availabilityData,
                        this@HostPickAndReturnHoursActivity,
                        uiHelper
                    )

            }
        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            isAlwaysAvailable = 1
            setAlwaysAvailable()
        } else {
            isAlwaysAvailable = 0
            binding.btnAlwaysAvailable.isChecked = false
        }
    }

    override fun popupButtonClick(value: Int, text_id_model: Any) {

    }
}