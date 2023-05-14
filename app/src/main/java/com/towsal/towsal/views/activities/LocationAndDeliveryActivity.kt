package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityLocationAndDeliveryBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.settings.CarLocationDeliveryModel
import com.towsal.towsal.network.serializer.settings.LocationDeliveryResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for location and delivery
 * */
class LocationAndDeliveryActivity : BaseActivity(), OnNetworkResponse , TextWatcher {

    lateinit var binding: ActivityLocationAndDeliveryBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var carInfoModel = CarInformationModel()
    var modelResponse = LocationDeliveryResponseModel()
    var carId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_and_delivery)
        binding.activity = this
        actionBarSetting()
        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        setData()
        setListener()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        settingsViewModel.getCarLocationDelivery(
            carId,
            true,
            Constants.API.GET_LOCATION_DELIVERY,
            this
        )
        binding.enableDelivery.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.clDeliveryFeesSelection.isVisible = isChecked
        }

        binding.editInfoLocation.setGradientTextColor(
            intArrayOf(
                getColor(R.color.send_msg_bg),
                getColor(R.color.text_receive_msg)
            )
        )
    }

    private fun setListener(){
        binding.price.addTextChangedListener(this)
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.location_delivery,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.editInfoLocation -> {
                val bundle = Bundle()
                if (modelResponse.car_location_delivery.pin_address == null) {
                    modelResponse.car_location_delivery = CarLocationDeliveryModel()
                    modelResponse.car_location_delivery.pin_address = ""
                    modelResponse.car_location_delivery.zip_code = ""
                    modelResponse.car_location_delivery.pin_lat = 0.0
                    modelResponse.car_location_delivery.pin_long = 0.0
                    modelResponse.car_location_delivery.country_name = ""
                    modelResponse.car_location_delivery.state_name = ""
                    modelResponse.car_location_delivery.street_address = ""
                    modelResponse.car_location_delivery.city_name = ""
                }
                carInfoModel.pin_address = modelResponse.car_location_delivery.pin_address
                carInfoModel.zip_code = modelResponse.car_location_delivery.zip_code
                carInfoModel.pin_lat = modelResponse.car_location_delivery.pin_lat
                carInfoModel.pin_long = modelResponse.car_location_delivery.pin_long
                carInfoModel.country_name = modelResponse.car_location_delivery.country_name
                carInfoModel.state_name = modelResponse.car_location_delivery.state_name
                carInfoModel.street_address = modelResponse.car_location_delivery.street_address
                carInfoModel.city_name = modelResponse.car_location_delivery.city_name
                bundle.putSerializable(Constants.DataParsing.MODEL, carInfoModel)
                uiHelper.openActivityForResult(
                    this,
                    MapsActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVTIY_LOCATION, bundle
                )
            }
            R.id.save -> {

                //changing button transition

                if (binding.enableDelivery.isChecked) {
                    if (binding.price.text.toString().isEmpty()) {
                        uiHelper.showLongToastInCenter(this, getString(R.string.enter_price))
                    } else {
                        modelResponse.car_location_delivery.delivery_status = 1
                        modelResponse.car_location_delivery.updateLocation = 1
                        val value = binding.price.text.toString()
                        modelResponse.car_location_delivery.delivery_price = value.toInt()
                        modelResponse.car_location_delivery.pin_address = modelResponse.car_location_delivery.street_address
                        modelResponse.car_location_delivery.delivery_radius_id = modelResponse.location_delivery_redius[binding.radiusList.selectedItemPosition].id
                        settingsViewModel.saveCarLocationDelivery(
                            carId,
                            modelResponse.car_location_delivery,
                            true,
                            Constants.API.SAVE_LOCATION_DELIVERY,
                            this
                        )
                    }
                } else {
                    modelResponse.car_location_delivery.pin_address = modelResponse.car_location_delivery.street_address
                    modelResponse.car_location_delivery.delivery_status = 0
                    modelResponse.car_location_delivery.updateLocation = 1
                  //  val value = binding.price.text.toString()
                    modelResponse.car_location_delivery.delivery_price = 0
                    modelResponse.car_location_delivery.delivery_radius_id =
                        modelResponse.location_delivery_redius[binding.radiusList.selectedItemPosition].id

                    settingsViewModel.saveCarLocationDelivery(
                        carId,
                        modelResponse.car_location_delivery,
                        true,
                        Constants.API.SAVE_LOCATION_DELIVERY,
                        this
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_LOCATION_DELIVERY -> {
                modelResponse = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    LocationDeliveryResponseModel::class.java
                )
                val list = ArrayList<String>()
                var selectedIndex = 0
                for (i in modelResponse.location_delivery_redius.indices) {
                    list.add(modelResponse.location_delivery_redius[i].value + " " + getString(R.string.km_radius))
                    if (modelResponse.car_location_delivery.delivery_radius.isNotEmpty())
                        if (modelResponse.location_delivery_redius[i].value == modelResponse.car_location_delivery.delivery_radius) {
                            selectedIndex = i
                        }
                }
                uiHelper.setSpinner(this, list, binding.radiusList)
                binding.radiusList.setSelection(selectedIndex)
                binding.tvPickupLocation.text = modelResponse.car_location_delivery.street_address
                if (modelResponse.car_location_delivery.delivery_price != null) {
                    if (modelResponse.car_location_delivery.delivery_price != 0){
                        binding.price.setText("" + modelResponse.car_location_delivery.delivery_price)
                    }
                }
                binding.enableDelivery.isChecked = modelResponse.car_location_delivery.delivery_status != 0
                binding.mainLayout.visibility = View.VISIBLE
            }
            Constants.API.SAVE_LOCATION_DELIVERY -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RequestCodes.ACTIVTIY_LOCATION -> {
                    val bundle = data?.getBundleExtra(Constants.DataParsing.MODEL)
                    carInfoModel =
                        bundle?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
                    if (carInfoModel != null) {
                        binding.tvPickupLocation.text = carInfoModel.street_address
                        modelResponse.car_location_delivery.pin_address = carInfoModel.pin_address
                        modelResponse.car_location_delivery.zip_code = carInfoModel.zip_code
                        modelResponse.car_location_delivery.pin_lat = carInfoModel.pin_lat
                        modelResponse.car_location_delivery.pin_long = carInfoModel.pin_long
                        modelResponse.car_location_delivery.country_name = carInfoModel.country_name
                        modelResponse.car_location_delivery.state_name = carInfoModel.state_name
                        modelResponse.car_location_delivery.street_address =
                            carInfoModel.street_address
                        modelResponse.car_location_delivery.city_name = carInfoModel.city_name
                    }
                }
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(p0: Editable?) {
        if (binding.price.hasFocus()){
            if (binding.price.text.toString().length == 1){
                if (binding.price.text.toString() == "0"){
                    binding.price.setText("")
                }
            }
        }
    }
}


