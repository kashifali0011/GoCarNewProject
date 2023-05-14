package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentReviewSettingBinding
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.RatingReviewResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.ReviewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for review settings
 * */
class ReviewSettingFragment : BaseFragment(), OnNetworkResponse {

    val settingsViewModel: SettingsViewModel by viewModel()

    lateinit var binding: FragmentReviewSettingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_review_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainLayout.isVisible = false
        binding.fragment = this

        Handler(Looper.getMainLooper()).postDelayed({
            settingsViewModel.getHostReview(
                true,
                Constants.API.GET_HOST_REVIEW,
                this
            )
        }, NotificationsFragment.delayTimeMilliSeconds)
    }


    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_HOST_REVIEW -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    RatingReviewResponseModel::class.java
                )
                binding.model = model
                Glide.with(this)
                    .load(BuildConfig.IMAGE_BASE_URL + model.host_profile_image)
                    .into(binding.ivProfile)
                binding.ratingBar.rating = model.host_overall_rating
                binding.reviewRecyclerView.adapter =
                    ReviewAdapter(requireActivity(), uiHelper, model.reviewsList)
                binding.executePendingBindings()
                binding.mainLayout.visibility = View.VISIBLE
                binding.tvReview.isVisible = model.reviewsList.size > 4

            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
    }


}