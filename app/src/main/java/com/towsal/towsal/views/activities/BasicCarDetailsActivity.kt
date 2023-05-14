package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityBasicCarDetailsBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.network.serializer.settings.BasicCarPostModel
import com.towsal.towsal.network.serializer.settings.BasicCarResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.CarFeatureAdapterVertical
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for basic car details
 * */
class BasicCarDetailsActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityBasicCarDetailsBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var model = BasicCarResponseModel()
    var carId = 0
    var carName = ""
    lateinit var adapter: CarFeatureAdapterVertical

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_basic_car_details)
        binding.activity = this
        actionBarSetting()
        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        carName = intent?.extras?.getString(Constants.DataParsing.NAME, "") ?: ""
        adapter = CarFeatureAdapterVertical(
            this,
            uiHelper
        )
        settingsViewModel.getCarDetail(carId, true, Constants.API.GET_CAR_DETAIL, this)
        binding.editCarFeatures.setGradientTextColor(
            intArrayOf(
                getColor(R.color.send_msg_bg),
                getColor(R.color.text_receive_msg)
            )
        )
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.details,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            binding.editCarFeatures.id -> {
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.DATECOMPLETEMODEL, model)
                bundle.putString(Constants.DataParsing.NAME, carName)
//                bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carListDataModel)
                uiHelper.openActivityForResult(
                    this,
                    CarFeaturesActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVITY_CAR_FEATURE, bundle
                )
            }
            binding.save.id -> {
                when {
                    binding.spNoOfSeats.selectedItemPosition == 0 -> {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.select_no_of_seat_err_msg)
                        )
                    }
                    binding.spDoors.selectedItemPosition == 0 -> {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.select_no_of_door_err_msg)
                        )
                    }
                    binding.spFuel.selectedItemPosition == 0 -> {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.select_fuel_type_err_msg)
                        )
                    }
                    model.car_featuresID.isNullOrEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.add_car_feature_err_msg)
                        )
                    }
                    else -> {
                        val modelPost = BasicCarPostModel()
                        modelPost.car_feature = model.car_featuresID
                        modelPost.fuel_type =
                            model.car_fuel_type[binding.spFuel.selectedItemPosition - 1].id
                        modelPost.number_of_door =
                            model.car_number_of_doors[binding.spDoors.selectedItemPosition - 1].id
                        modelPost.number_of_seat =
                            model.car_number_of_seats[binding.spNoOfSeats.selectedItemPosition - 1].id

                        settingsViewModel.saveBasicCarDetail(
                            carId,
                            modelPost,
                            true,
                            Constants.API.SAVE_CAR_DETAIL,
                            this
                        )
                    }
                }
            }

            binding.tvSeeMore.id -> {
                binding.tvSeeMore.isVisible = false
                binding.tvViewLess.isVisible = true
                adapter.setList(
                    ArrayList(model.car_detail.car_feature)
                )
            }
            binding.tvViewLess.id -> {
                binding.tvSeeMore.isVisible = true
                binding.tvViewLess.isVisible = false
                adapter.setList(
                    ArrayList(model.car_detail.car_feature.take(4))
                )
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_DETAIL -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    BasicCarResponseModel::class.java
                )
                setData()
            }
            Constants.API.SAVE_CAR_DETAIL -> {
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
        model.car_detail.car_feature.mapTo(
            model.car_featuresID
        ) {
            it.id
        }
        binding.carFeatureList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.carFeatureList.adapter = adapter
        binding.tvSeeMore.isVisible = model.car_detail.car_feature.size > 4
        adapter.setList(ArrayList(model.car_detail.car_feature.take(4)))
        val carSeats = ArrayList<String>()
        carSeats.add(getString(R.string.num_seats))
        val selectedIndex = model.car_number_of_seats.indexOfFirst {
            it.id == model.car_detail.number_of_seat.id
        }
        model.car_number_of_seats.mapTo(carSeats) {
            it.value
        }
        uiHelper.setSpinner(this, carSeats, binding.spNoOfSeats)
        binding.spNoOfSeats.setSelection(
            selectedIndex + 1
        )

        val carDoors = ArrayList<String>()
        val selectedIndexCarDoor = model.car_number_of_doors.indexOfFirst {
            it.id == model.car_detail.number_of_door.id
        }
        carDoors.add(getString(R.string.num_doors))
        model.car_number_of_doors.mapTo(carDoors) {
            it.value
        }
        uiHelper.setSpinner(this, carDoors, binding.spDoors)
        binding.spDoors.setSelection(
            selectedIndexCarDoor + 1
        )

        val fuelTypes = ArrayList<String>()
        val selectedIndexFuelTypes = model.car_fuel_type.indexOfFirst {
            it.id == model.car_detail.fuel_type.id
        }
        fuelTypes.add(getString(R.string.fuel))
        model.car_fuel_type.mapTo(
            fuelTypes
        ) {
            it.name
        }

        uiHelper.setSpinner(this, fuelTypes, binding.spFuel)
        binding.spFuel.setSelection(selectedIndexFuelTypes + 1)
        binding.mainLayout.visibility = View.VISIBLE
    }


    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RequestCodes.ACTIVITY_CAR_FEATURE -> {
                    val bundle = data?.getBundleExtra(Constants.DataParsing.MODEL)
                    val carInfoModel =
                        bundle?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel

                    model.car_featuresID.clear()
                    val carFeature: ArrayList<CarMakeInfoModel> = ArrayList(model.car_features.filter { parent ->
                        carInfoModel.car_features.indexOfFirst { it == parent.id } != -1
                    })
                    carFeature.mapTo(model.car_featuresID) {
                        it.id
                    }
                    binding.carFeatureList.layoutManager = LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    binding.carFeatureList.adapter = adapter
                    binding. tvViewLess.isVisible = carFeature.size > 4
                    binding.tvSeeMore.isVisible = false
                    adapter.setList(carFeature)

                    var listResponse = model.car_detail.car_feature.size
                    var listGet = carFeature.size


                    if (listResponse != listGet){
                        model.car_detail.car_feature.clear()
                        model.car_detail.car_feature.addAll(carFeature)
                    }

                }
            }

        }
    }

}