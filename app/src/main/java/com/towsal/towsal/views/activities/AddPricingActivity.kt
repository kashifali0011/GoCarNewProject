package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityAddPricingBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.settings.CarPriceModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.adapters.CustomPricingAdapter

/**
 * Activity class for add car pricing
 * */
class AddPricingActivity : BaseActivity(), RecyclerViewItemClick  , TextWatcher {

    var model = CarPriceModel()
    lateinit var adapter: CustomPricingAdapter
    lateinit var binding: ActivityAddPricingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_pricing)
        binding.activity = this
        setData()
        setListener()
    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("SetTextI18n")
    private fun setData() {
        if (intent.extras != null) {
            var headerName: Int = R.string.default_price
            val type = intent.extras!!.getInt(Constants.TYPE, 0)
            model = intent.extras!!.getSerializable(Constants.DataParsing.MODEL) as CarPriceModel
            binding.defaultPriceLayout.isVisible = type == Constants.CarPricing.DEFAULT_PRICE
            binding.discountPriceLayout.isVisible = type == Constants.CarPricing.DISCOUNT_PRICE
            binding.customPriceLayout.isVisible = type == Constants.CarPricing.CUSTOM_PRICE
            when (type) {
                Constants.CarPricing.DEFAULT_PRICE -> {
                    headerName = R.string.default_price
                    binding.defaultPriceEdt.setText(model.defaultPrice)
                }
                Constants.CarPricing.DISCOUNT_PRICE -> {
                    headerName = R.string.discounts
                    if (model.threeDayDiscountPrice != 0)
                        binding.threeDayPriceEdt.setText("" + model.threeDayDiscountPrice)
                    if (model.sevenDayDiscountPrice != 0)
                        binding.sevenDayPriceEdt.setText("" + model.sevenDayDiscountPrice)
                    if (model.thirtyDayDiscountPrice != 0)
                        binding.thirtyDayPriceEdt.setText("" + model.thirtyDayDiscountPrice)
                }
                Constants.CarPricing.CUSTOM_PRICE -> {
                    headerName = R.string.custom_prices
                }
            }
            actionBarSetting(headerName)
        }
        setRecyclerViewData()

    }

    private fun setListener(){
        binding.threeDayPriceEdt.addTextChangedListener(this)
        binding.sevenDayPriceEdt.addTextChangedListener(this)
        binding.thirtyDayPriceEdt.addTextChangedListener(this)
    }

    /**
     * Function for setting data in recyclerView
     * */
    private fun setRecyclerViewData() {

        binding.recyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        adapter = CustomPricingAdapter(this, uiHelper, this, model.customPricingList)
        binding.recyclerView.adapter = adapter
    }

    /**
     * Function for setting action bar
     * */
    fun actionBarSetting(titleId: Int) {
        binding.layoutToolBar.setAsHostToolBar(
            titleId,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.saveDefaultPriceInfo -> {
                if (binding.defaultPriceEdt.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        this,
                        getString(R.string.car_default_price_err_msg)
                    )
                    return
                } else {
                    val value = binding.defaultPriceEdt.text.toString()
                    //model.defaultPrice = value.toInt()

                    model.defaultPrice = value        //it was int
                }
            }
            R.id.saveDiscountPriceInfo -> {
                when {
                    else -> {
                        var value3 = ""
                        value3 = binding.threeDayPriceEdt.text.toString().ifEmpty {
                            "0"
                        }
                        val value7 = binding.sevenDayPriceEdt.text.toString().ifEmpty {
                            "0"
                        }
                        val value30 =
                            binding.thirtyDayPriceEdt.text.toString().ifEmpty {
                                "0"
                            }
                        model.threeDayDiscountPrice = value3.toInt()
                        model.sevenDayDiscountPrice = value7.toInt()
                        model.thirtyDayDiscountPrice = value30.toInt()

                    }
                }
            }
            R.id.saveCustomPriceInfo -> {
                model.customPricingList = adapter.getList()
            }

        }
        val intent = intent
        val bundle = Bundle()
        bundle.putSerializable(Constants.DataParsing.MODEL, model)
        intent.putExtra(Constants.DataParsing.MODEL, bundle)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


    }

    override fun afterTextChanged(p0: Editable?) {
        if (binding.threeDayPriceEdt.hasFocus()){
            if (binding.threeDayPriceEdt.text.toString().length == 1){
                if (binding.threeDayPriceEdt.text.toString() == "0"){
                    binding.threeDayPriceEdt.setText("")
                }
            }
        }
        if (binding.sevenDayPriceEdt.hasFocus()){
            if (binding.sevenDayPriceEdt.text.toString().length == 1){
                if (binding.sevenDayPriceEdt.text.toString() == "0"){
                    binding.sevenDayPriceEdt.setText("")
                }
            }
        }
        if (binding.thirtyDayPriceEdt.hasFocus()){
            if (binding.thirtyDayPriceEdt.text.toString().length == 1){
                if (binding.thirtyDayPriceEdt.text.toString() == "0"){
                    binding.thirtyDayPriceEdt.setText("")
                }
            }
        }


    }

}