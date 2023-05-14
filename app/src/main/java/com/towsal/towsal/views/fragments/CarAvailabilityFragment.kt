package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentCarAvailabilityBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.step2.CarAvailabilityModel
import com.towsal.towsal.network.serializer.carlist.step2.CarAvailabilityPostModel
import com.towsal.towsal.network.serializer.carlist.step2.Step2CarInfoModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarListViewModel

/**
 * Fragment class for car availability
 * */
class CarAvailabilityFragment : BaseFragment(), OnNetworkResponse {

    private var carListDataModel = CarListDataModel()
    private var step2CarInfoModel: Step2CarInfoModel? = null
    private lateinit var binding: FragmentCarAvailabilityBinding
    private val carListViewModel: CarListViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_car_availability,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        setData()
    }


    /**
     * Function for setting up views data
     * */
    private fun setData() {
        carListDataModel = preferenceHelper.getObject(
            Constants.DataParsing.CAR_LIST_DATA_MODEL,
            CarListDataModel::class.java
        ) as CarListDataModel

        if (carListDataModel.disableViews) {
            binding.spNoticePeriod.isEnabled = false
            binding.spMinTripDuration.isEnabled = false
            binding.spMaxTripDuration.isEnabled = false
        }
        binding.noteCarAvailability.text =
            uiHelper.spanString(
                getString(R.string.not_msg_car_availablity),
                getString(R.string.note),
                R.font.bold, requireContext()
            )

        if (step2CarInfoModel == null)
            callApi()
        else
            populateData()

    }

    private fun callApi() {
        carListViewModel.getCarList(
            showLoader = true,
            tag = Constants.API.GET_CAR_LIST,
            callback = this,
            step = Constants.CarStep.STEP_2_COMPLETED
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
                    (step2CarInfoModel?.carAdvanceNoticeList ?: emptyList()).isEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.advance_notice_hour_msg)
                        )
                    }
                  /*  binding.spMinTripDuration.selectedItemPosition == 0 -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.min_hour_err_msg)
                        )
                    }*/
                    binding.spMaxTripDuration.selectedItemPosition == 0 -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.max_hour_err_msg)
                        )
                    }
                    else -> {
                        val postModel = CarAvailabilityPostModel()
                        postModel.car_id = carListViewModel.carId
                        postModel.step = Constants.CarStep.STEP_2_COMPLETED
                        step2CarInfoModel?.let {
                            postModel.adv_notice_trip_start_id = it.carAdvanceNoticeList[binding.spNoticePeriod.selectedItemPosition ].id
                            postModel.min_trip_dur = "" + it.min_hour_list[binding.spMinTripDuration.selectedItemPosition ].id
                            postModel.max_trip_dur = "" + it.max_hour_list[binding.spMaxTripDuration.selectedItemPosition ].id

                        }
                    /*    var aaa = postModel.adv_notice_trip_start_id
                        var aa = aaa - 1
                        Log.d("aaaa" , aaa.toString())*/
                        carListViewModel.sendCarInfo(
                            postModel,
                            true,
                            Constants.API.SEND_CAR_DATA,
                            this
                        )
                    }
                }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SEND_CAR_DATA -> {
                step2CarInfoModel?.let {
                    it.car_availability = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        CarAvailabilityModel::class.java
                    )
                }

                val navController = findNavController()
                preferenceHelper.saveObject(
                    carListDataModel,
                    Constants.DataParsing.CAR_LIST_DATA_MODEL
                )
                navController.navigate(
                    R.id.action_carAvailabilityFragment_to_carPhotosFragment
                )
            }

            Constants.API.GET_CAR_LIST -> {
                step2CarInfoModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    Step2CarInfoModel::class.java
                )
                populateData()
            }
        }
    }

    private fun populateData() {


        val list = ArrayList<String>()
        var selectedIndex = 0
        step2CarInfoModel?.let {
            for (i in it.carAdvanceNoticeList.indices) {
                list.add(it.carAdvanceNoticeList[i].name)
                if (it.carAdvanceNoticeList[i].id == it.car_availability.adv_notice_trip_start.id) {
                    selectedIndex = i
                }
            }
            uiHelper.setSpinner(requireActivity(), list, binding.spNoticePeriod)
            binding.spNoticePeriod.setSelection(selectedIndex)




            uiHelper.setSpinner(requireActivity() , list , binding.spBufferPeriod)
            binding.spBufferPeriod.setSelection(selectedIndex)

            val minList = ArrayList<String>()
         //   minList.add(getString(R.string.minimum_trip_duration))
            var selectedIndexMinList = 0
            for (i in it.min_hour_list.indices) {
                minList.add(it.min_hour_list[i].name + " " + getString(R.string.hours))
                if (it.min_hour_list[i].id == it.car_availability.min_trip_dur.id) {
                    selectedIndexMinList = i
                }
            }
            uiHelper.setSpinner(requireActivity(), minList, binding.spMinTripDuration)
            binding.spMinTripDuration.setSelection(selectedIndexMinList)

            val maxList = ArrayList<String>()
         //   maxList.add(getString(R.string.maximum_trip_duration))

            var selectedIndexMaxList = 0
            for (i in it.max_hour_list.indices) {
                val text = if (i == 0) {
                    getString(R.string.day)
                } else {
                    getString(R.string.days)
                }
                maxList.add(it.max_hour_list[i].name + " " + text)
                if (it.max_hour_list[i].id == it.car_availability.max_trip_dur.id) {
                    selectedIndexMaxList = i
                }
            }
            selectedIndexMaxList = maxList.size - 1
            uiHelper.setSpinner(requireActivity(), maxList, binding.spMaxTripDuration)
            binding.spMaxTripDuration.setSelection(selectedIndexMaxList)

        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireContext(), response?.message)
    }


}