package com.towsal.towsal.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityCarInformationBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.network.serializer.carlist.MakesAndModelsResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarListViewModel
import kotlinx.android.synthetic.main.fragment_filters.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for car information
 * */
class CarInformationActivity : BaseActivity(), PopupCallback, OnNetworkResponse {

    private lateinit var binding: ActivityCarInformationBinding
    private var carInfoModel = CarInformationModel()
    private var modelList = ArrayList<String>()
    private val yearList = ArrayList<String>()
    private val carTypesList = ArrayList<String>()
    private val makeList = ArrayList<String>()
    private val odometerList = ArrayList<String>()
    private var makesResponseList: MutableList<CarMakeInfoModel> = ArrayList()
    private var modelsResponseList: MutableList<CarMakeInfoModel> = ArrayList()
    private var yearSelectedIndex = -1
    private var carTypeSelectedIndex = -1
    private var makeSelectedIndex = -1
    private var modelSelectedIndex = -1
    private var odometerSelectedIndex = -1
    private var runFirstTime = false
    private val carListViewModel: CarListViewModel by viewModel()
    private var carYears = ArrayList<CarMakeInfoModel>()
    private var carTypes = ArrayList<CarMakeInfoModel>()
    private var carOdometers = ArrayList<CarMakeInfoModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_information)
        binding.activity = this
      //  actionBarSetting()

        binding.layoutToolBar.setAsHostToolBar(
            R.string.list_your_car,
            supportActionBar
        )
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            carInfoModel =
                intent?.extras?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
            yearList.add(getString(R.string.select))
            carTypesList.add(getString(R.string.select))

            callApi()
        }
    }

    private fun callApi() {
        carListViewModel.getCarAttributes(
            Constants.API.CAR_ATTRIBUTES,
            this,
            true
        )
    }

    /**
     * Function for setting up spinners
     * */
    private fun setSpinner(
        list: ArrayList<String>,
        spinner: Spinner
    ) {
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.spinner_simple_item, list
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.next -> {
                try {
                    when {
                        binding.yearList.selectedItemPosition == 0 -> {
                            uiHelper.showLongToastInCenter(this, getString(R.string.year_err_msg))
                        }
                        makeSelectedIndex == -1 -> {
                            uiHelper.showLongToastInCenter(this, getString(R.string.make_err_msg))
                        }
                        binding.modelList.selectedItemPosition == 0 -> {
                            uiHelper.showLongToastInCenter(this, getString(R.string.model_err_msg))
                        }
//                        binding.spCarTypes.selectedItemPosition == 0 -> {
//                            uiHelper.showLongToastInCenter(
//                                this,
//                                getString(R.string.car_type_err_msg)
//                            )
//                        }
                        binding.odometerList.selectedItemPosition == 0 -> {
                            uiHelper.showLongToastInCenter(
                                this,
                                getString(R.string.odometer_err_message)
                            )
                        }
                        else -> {
                            carInfoModel.year_id =
                                carYears[binding.yearList.selectedItemPosition - 1].id
                            carInfoModel.year =
                                carYears[binding.yearList.selectedItemPosition - 1].year
                            carInfoModel.make_id =
                                makesResponseList[makeSelectedIndex].id
                            carInfoModel.makeName =
                                makesResponseList[makeSelectedIndex].name
                            carInfoModel.model_id =
                                modelsResponseList[modelSelectedIndex].id
                            carInfoModel.modelName =
                                modelsResponseList[modelSelectedIndex].name
                            carInfoModel.odometer =
                                carOdometers[binding.odometerList.selectedItemPosition - 1].id
//                            carInfoModel.carTypeId =
//                                carTypes[binding.spCarTypes.selectedItemPosition - 1].id


                            val intent = Intent()
                            val bundle = Bundle()
                            bundle.putSerializable(Constants.DataParsing.MODEL, carInfoModel)
                            intent.putExtra(Constants.DataParsing.MODEL, bundle)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    uiHelper.showLongToastInCenter(this, e.localizedMessage)
                    e.printStackTrace()
                }

            }

            binding.spinner2LL.id -> {
                uiHelper.showMakeSearchPopUp(this, this, makeList)

            }
        }
    }

    override fun popupButtonClick(value: Int) {

    }

    override fun popupButtonClick(value: Int, text_id_model: Any) {
        val input = text_id_model as String
        makeSelectedIndex = makeList.indexOfFirst {
            it == input
        }
        binding.makeList.text = makeList[makeSelectedIndex]

        setModels()
    }

    /**
     * Function for setting up models
     * */
    private fun setModels() {
        try {
            binding.modelList.setSelection(0, true)
            if (makesResponseList[makeSelectedIndex].id != 0) {
                binding.pbModel.isVisible = true
                carListViewModel.getModels(
                    carYears[yearSelectedIndex - 1].id,
                    makesResponseList[makeSelectedIndex].id,
                    this,
                    Constants.API.MODEL_SELECTION
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.MAKE_SELECTION -> {
                makeList.clear()
                makesResponseList.clear()
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    MakesAndModelsResponseModel::class.java
                )
                binding.pbMake.isVisible = false

//                Log.e("Result", responseModel.carMakesList.size.toString())
                makesResponseList = responseModel.carMakesList as MutableList<CarMakeInfoModel>
                val carMakeInfoModel = CarMakeInfoModel()
                carMakeInfoModel.name = "Make"
                makesResponseList.add(0, carMakeInfoModel)
                makesResponseList.mapTo(makeList) {
                    it.name
                }

                if (makesResponseList.isNotEmpty()) {
                    makeSelectedIndex =
                        makesResponseList.indexOfFirst { it.id == carInfoModel.make_id }
                    binding.spinner2LL.isClickable = true

                    if (carYears[yearSelectedIndex - 1].id == carInfoModel.year_id) {
                        if (makeSelectedIndex > -1) {
                            binding.makeList.text = makeList[makeSelectedIndex]
                            setModels()
                        } else {
                            binding.makeList.text = makeList[0]
                        }
                    }

                } else {
                    binding.spinner2LL.isClickable = false
                }
//                binding.makeList.isEnabled = true

                Log.e("Result", makeList.size.toString())
            }

            Constants.API.MODEL_SELECTION -> {
                binding.modelList.isEnabled = true
                modelsResponseList.clear()
                modelList.clear()
                binding.pbModel.isVisible = false
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    MakesAndModelsResponseModel::class.java
                )

//                Log.e("Result", responseModel.carMakesList.size.toString())
                modelsResponseList =
                    responseModel.carModelsList as MutableList<CarMakeInfoModel>
                val carMakeInfoModel = CarMakeInfoModel()
                carMakeInfoModel.name = "Select"
                modelsResponseList.add(0, carMakeInfoModel)
                modelsResponseList.mapTo(modelList) {
                    it.name
                }

                if (modelList.isNotEmpty()) {
                    uiHelper.setSpinner(activity, modelList, binding.modelList)
                    if (carYears[yearSelectedIndex - 1].id == carInfoModel.year_id && makesResponseList[makeSelectedIndex].id == carInfoModel.make_id) {
                        modelSelectedIndex = modelsResponseList.indexOfFirst {
                            it.id == carInfoModel.model_id
                        }
                        if (modelSelectedIndex == -1) {
                            binding.modelList.setSelection(0)
                        } else {
                            binding.modelList.setSelection(modelSelectedIndex)
                        }
                    } else {
                        binding.modelList.setSelection(0)
                    }
                } else {
                    binding.modelList.setSelection(0)
                }

            }

            Constants.API.CAR_ATTRIBUTES -> {
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarListDataModel::class.java
                )

                carYears = responseModel.car_year
                carTypes = responseModel.carTypes
                carOdometers = responseModel.odometer

                val carMakeInfoModel = CarMakeInfoModel()
                val carMakeInfoModel2 = CarMakeInfoModel()
                carMakeInfoModel.name = "Select"
                carMakeInfoModel2.name = "Select"
                makesResponseList.add(0, carMakeInfoModel)
                modelsResponseList.add(0, carMakeInfoModel2)

                yearSelectedIndex =
                    carYears.indexOfFirst { it.id == carInfoModel.year_id }
                carTypeSelectedIndex =
                    carTypes.indexOfFirst { it.id == carInfoModel.carTypeId }
                yearList.addAll(carYears.map { it.year })
                carTypesList.addAll(carTypes.map { it.name })
//            makeList.add(getString(R.string.make))

                modelsResponseList.mapTo(modelList) {
                    it.name
                }
                makesResponseList.mapTo(makeList) {
                    it.name
                }
                uiHelper.setSpinner(activity, modelList, binding.modelList)
                binding.modelList.setSelection(0)
                binding.makeList.text = makeList[0]
                makeSelectedIndex = 0



                for (i in carOdometers.indices) {
                    if (carInfoModel.odometer == carOdometers[i].id) {
                        odometerSelectedIndex = i
                    }
                    odometerList.add(carOdometers[i].range)
                }
                odometerList.add(0, getString(R.string.select))

//            setSpinner(modelList, binding.modelList)
                setSpinner(yearList, binding.yearList)
                setSpinner(odometerList, binding.odometerList)
                setSpinner(carTypesList, binding.spCarTypes)
                binding.makeList.text = makeList[0]


                binding.yearList.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {

                        binding.spinner2LL.isClickable = false
                        binding.modelList.isEnabled = false
                        binding.makeList.text = makeList[0]
                        binding.modelList.setSelection(0)
                        makeSelectedIndex = 0
                        modelSelectedIndex = 0
                        yearSelectedIndex = position
                        binding.pbMake.isVisible = runFirstTime && position != 0

                        if (runFirstTime && position != 0) {
                            val yearId = carYears[position - 1].id
                            carListViewModel.getMakes(
                                yearId,
                                this@CarInformationActivity,
                                Constants.API.MAKE_SELECTION
                            )
                        } else {
                            runFirstTime = true
                        }
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                        // your code here
                    }
                }
                binding.modelList.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View?,
                        position: Int,
                        id: Long
                    ) {
                        modelSelectedIndex = position
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }

                if (yearSelectedIndex != -1) {
                    runFirstTime = true
                    binding.yearList.setSelection(yearSelectedIndex + 1)
                    binding.odometerList.setSelection(odometerSelectedIndex + 1)
                    binding.spCarTypes.setSelection(carTypeSelectedIndex + 1)
                } else {
                    binding.yearList.setSelection(0)
                }

            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(activity, response?.message)
        binding.pbMake.isVisible = false
        //pbModel.isVisible = false
    }


}