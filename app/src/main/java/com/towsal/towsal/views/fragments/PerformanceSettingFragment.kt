package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentPerformanceSettingBinding
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.PerformanceResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for performance settings
 * */
class PerformanceSettingFragment : BaseFragment(),
    OnNetworkResponse {

    val settingsViewModel: SettingsViewModel by viewModel()

    lateinit var binding: FragmentPerformanceSettingBinding
    private var modelResponse = PerformanceResponseModel()
    private var progr = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_performance_setting, container, false)
        binding.pbAcceptRate.progress = 0
        binding.pbResponseRate.progress = 0
        binding.pbCommitmentRate.progress = 0
        binding.pbFiveStarRate.progress = 0
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clMainRoot.isVisible = false
        binding.fragment = this

        Handler(Looper.getMainLooper()).postDelayed({

            settingsViewModel.getPerformaceSetting(
                true,
                Constants.API.GET_PERFORMANCE_SETTING,
                this
            )
        }, NotificationsFragment.delayTimeMilliSeconds)
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_PERFORMANCE_SETTING -> {
                modelResponse = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    PerformanceResponseModel::class.java
                )
                binding.clMainRoot.isVisible = true

                binding.pbAcceptRate.progress = 100
                binding.pbAcceptRate.progress = modelResponse.performance.acceptance_rate_percentage
                binding.acceptanceRate.text = "" + binding.pbAcceptRate.progress + "%"
                binding.tvAcceptRate.text = modelResponse.performance.acceptanceRate

                binding.pbResponseRate.progress = 100
                binding.tvResponseRate.text = modelResponse.performance.responseRate
                binding.pbResponseRate.progress = modelResponse.performance.response_rate_percentage
                binding.responseRate.text =
                    "" + binding.pbResponseRate.progress + "%"

                binding.pbCommitmentRate.progress = 100
                binding.pbCommitmentRate.progress = modelResponse.performance.commitment_rate_percentage
                binding.tvCommitmentRate.text = modelResponse.performance.commitmentRate
                binding.commitmentRate.text = "" + binding.pbCommitmentRate.progress + "%"


                binding.pbFiveStarRate.progress = 100
                binding.pbFiveStarRate.progress = modelResponse.performance.five_star_rating_percentage
                binding.fiveStarRate.text = "" + binding.pbFiveStarRate.progress + "%"
                binding.tvFiveStarRate.text = modelResponse.performance.fiveStarRating

            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

}