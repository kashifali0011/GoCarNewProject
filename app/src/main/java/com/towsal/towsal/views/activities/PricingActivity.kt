package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityPricingBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.CarPriceModel
import com.towsal.towsal.network.serializer.settings.CustomPriceModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for pricing screen
 * */
class PricingActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityPricingBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var model = CarPriceModel()
    private var isForEdit = false
    var mArrayList = ArrayList<CustomPriceModel>()
    var carId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pricing)
        binding.activity = this
        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        uiHelper.hideActionBar(supportActionBar)
        prepareList()
        actionBarSetting()
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        settingsViewModel.getCarPricing(carId, true, Constants.API.GET_CAR_PRICE, this)
    }

    private fun prepareList() {
        val customPriceModel = CustomPriceModel()
        customPriceModel.dayName = getString(R.string.monday)
        customPriceModel.key = getString(R.string.mon)
        mArrayList.add(
            customPriceModel
        )
        val customPriceModel1 = CustomPriceModel()
        customPriceModel1.dayName = getString(R.string.tuesday)
        customPriceModel1.key = getString(R.string.tue)
        mArrayList.add(
            customPriceModel1
        )
        val customPriceModel2 = CustomPriceModel()
        customPriceModel2.dayName = getString(R.string.wednesday)
        customPriceModel2.key = getString(R.string.wed)
        mArrayList.add(
            customPriceModel2
        )
        val customPriceModel3 = CustomPriceModel()
        customPriceModel3.dayName = getString(R.string.thursday)
        customPriceModel3.key = getString(R.string.thu)
        mArrayList.add(
            customPriceModel3
        )
        val customPriceModel4 = CustomPriceModel()
        customPriceModel4.dayName = getString(R.string.friday)
        customPriceModel4.key = getString(R.string.fri)
        mArrayList.add(
            customPriceModel4
        )
        val customPriceModel5 = CustomPriceModel()
        customPriceModel5.dayName = getString(R.string.saturday)
        customPriceModel5.key = getString(R.string.sat)
        mArrayList.add(
            customPriceModel5
        )
        val customPriceModel6 = CustomPriceModel()
        customPriceModel6.dayName = getString(R.string.sunday)
        customPriceModel6.key = getString(R.string.sun)
        mArrayList.add(
            customPriceModel6
        )
    }


    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.pricing,
            supportActionBar
        )
    }

    /**
     * Function of click listeners
     * */
    fun onClick(view: View) {
        val bundle = Bundle()
        bundle.putSerializable(Constants.DataParsing.MODEL, model)
        when (view) {
            binding.clDailyPrice -> {
                isForEdit = true
                bundle.putInt(Constants.TYPE, Constants.CarPricing.DEFAULT_PRICE)
            }
            binding.clDiscount -> {
                isForEdit = true
                bundle.putInt(Constants.TYPE, Constants.CarPricing.DISCOUNT_PRICE)

            }
            binding.clCustomPrice -> {
                isForEdit = true
                bundle.putInt(Constants.TYPE, Constants.CarPricing.CUSTOM_PRICE)
            }
            binding.savePricingInfo -> {
                isForEdit = false
                if (model.defaultPrice.replace(",", "").toDouble() != 0.0) {
//
                    settingsViewModel.saveCarPricing(
                        carId,
                        model,
                        true,
                        Constants.API.SEND_CAR_PRICE,
                        this
                    )
                } else {
                    uiHelper.showLongToastInCenter(
                        this,
                        getString(R.string.car_default_price_save_err_msg)
                    )
                    return
                }
            }

        }
        if (isForEdit) {
            uiHelper.openActivityForResult(
                this,
                AddPricingActivity::class.java,
                true,
                Constants.RequestCodes.ACTIVTIY_PRICING, bundle
            )
        }

    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_PRICE -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarPriceModel::class.java
                )
                if (model.customPricingList.isNotEmpty()) {
                    for (i in model.customPricingList.withIndex()) {
                        mArrayList[i.index].price = i.value.price
                    }
                }
                model.customPricingList = mArrayList
                binding.mainLayout.visibility = View.VISIBLE
            }
            Constants.API.SEND_CAR_PRICE -> {
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
                Constants.RequestCodes.ACTIVTIY_PRICING -> {
                    val bundle = data?.getBundleExtra(Constants.DataParsing.MODEL)
                    model = bundle?.getSerializable(Constants.DataParsing.MODEL) as CarPriceModel
                }
            }
        }
    }
}