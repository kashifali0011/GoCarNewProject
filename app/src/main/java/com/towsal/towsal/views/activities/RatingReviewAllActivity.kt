package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityRatingReviewAllBinding
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.RatingReviewResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.ReviewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Function for setting up action bar
 * */
class RatingReviewAllActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityRatingReviewAllBinding
    var userid = 0
    val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rating_review_all)
        binding.activity = this
        actionBarSetting()
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        val bundle = intent.extras
        if (bundle != null) {
            userid = bundle.getInt(Constants.DataParsing.ID, 0)
            settingsViewModel.getHostReviewWithId(
                userid,
                true,
                Constants.API.GET_HOST_REVIEW,
                this
            )
        }
//        binding.reviewRecyclerView.adapter = ReviewAdapter(this, uiHelper)

    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        setActionBar = SetActionBar(supportActionBar, this)
        setActionBar?.setActionBarHeaderText(getString(R.string.rating_review))
        setActionBar?.setBackButton()
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
                    .into(binding.image)
                binding.ratingBar.rating = model.host_overall_rating
                binding.reviewRecyclerView.adapter =
                    ReviewAdapter(this, uiHelper, model.reviewsList)
                binding.executePendingBindings()
                binding.mainLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}