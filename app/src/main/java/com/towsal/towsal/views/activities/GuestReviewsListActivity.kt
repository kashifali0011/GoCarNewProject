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
import com.towsal.towsal.databinding.ActivityGuestReviewsListBinding
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.GuestRatingReviewsModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.GuestReviewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for guest reviews
 * */
class GuestReviewsListActivity : BaseActivity(), OnNetworkResponse {

    val settingsViewModel: SettingsViewModel by viewModel()

    lateinit var binding: ActivityGuestReviewsListBinding

    var userid = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guest_reviews_list)
        binding.activity = this
        actionBarSetting()
        val bundle = intent.extras

        if (bundle != null) {
            userid = bundle.getInt(Constants.DataParsing.ID, 0)
            settingsViewModel.getGuestReviewWithId(
                userid,
                true,
                Constants.API.GET_Guest_REVIEW,
                this
            )
        } else {
            settingsViewModel.getGuestReview(
                true,
                Constants.API.GET_Guest_REVIEW,
                this)
        }
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        setActionBar = SetActionBar(supportActionBar, this)
        setActionBar?.setActionBarHeaderText(getString(R.string.reviews_and_ratings))
        setActionBar?.setBackButton()
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_Guest_REVIEW -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    GuestRatingReviewsModel::class.java
                )
                binding.model = model
                Glide.with(this)
                    .load(BuildConfig.IMAGE_BASE_URL + model.guest_profile_image)
                    .into(binding.image)

                binding.ratingBar.rating = model.guest_overall_rate
                binding.reviewRecyclerView.adapter =
                    GuestReviewAdapter(this, uiHelper, model.reviewsList)
                binding.executePendingBindings()
                binding.mainLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}