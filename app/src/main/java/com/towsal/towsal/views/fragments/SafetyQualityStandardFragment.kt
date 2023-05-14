package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentSafteyQualityStandardBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.step5.CarSafetyInfoModel
import com.towsal.towsal.network.serializer.carlist.step5.Step5Model
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarListViewModel
import com.towsal.towsal.views.activities.MainActivity
import com.towsal.towsal.views.adapters.SafetyListAdapter

/**
 * Fragment class for safety quality standards
 * */
class SafetyQualityStandardFragment : BaseFragment(), OnNetworkResponse {

    lateinit var binding: FragmentSafteyQualityStandardBinding
    var carListDataModel = CarListDataModel()
    val carListViewModel: CarListViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_saftey_quality_standard,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        carListDataModel = preferenceHelper.getObject(
            Constants.DataParsing.CAR_LIST_DATA_MODEL,
            CarListDataModel::class.java
        ) as CarListDataModel
        if (carListDataModel.disableViews) {
            binding.agreeCheck.isEnabled = false
        }
        getQualityStandards()
    }

    private fun getQualityStandards() {
        carListViewModel.getCarList(
            true,
            Constants.API.GET_CAR_LIST,
            this,
            Constants.CarStep.STEP_5_COMPLETED
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.agreeContinueBtn -> {
                if (!binding.agreeCheck.isChecked) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        "Please Agree to Safety Standard"
                    )
                } else {
                    callApi()
                }
            }
        }
    }

    /**
     * Function for calling api
     * */
    private fun callApi() {
        val carSafetyModel = CarSafetyInfoModel()
        carSafetyModel.step = Constants.CarStep.STEP_5_COMPLETED
        carSafetyModel.carSafetyCheck = binding.agreeCheck.isChecked
        carSafetyModel.user_type = Constants.USER_TYPE_HOST
        carListViewModel.sendSafetyStandardCheck(
            carSafetyModel,
            true,
            Constants.API.SEND_CAR_DATA,
            this
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SEND_CAR_DATA -> {
                uiHelper.showLongToastInCenter(requireContext(), response?.message)
                MainActivity.GOTO_FRAGMENT = MainActivity.GOTO_HOST_FRAGMENT
                preferenceHelper.clearKey(Constants.DataParsing.CAR_LIST_DATA_MODEL)
                uiHelper.openAndClearActivity(requireActivity(), MainActivity::class.java)
            }
            Constants.API.GET_CAR_LIST -> {
                val step5Model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as LinkedTreeMap<*, *>),
                    Step5Model::class.java
                )
                val adapter = SafetyListAdapter(requireContext(), uiHelper)
                binding.safetyListRecyclerView.adapter = adapter
                adapter.setList(step5Model.car_safety_quality_standard_list)
                binding.agreeCheck.isChecked =
                    step5Model.car_safety_info.carSafetyCheck
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireContext(), response?.message)

    }

}