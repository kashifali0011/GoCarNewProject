package com.towsal.towsal.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityRatingReviewBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for rating and reviews
 * */
class RatingReviewActivity : BaseActivity(), OnNetworkResponse {

    var id = 0
    var car_id = 0
    var view_type = ""
    lateinit var binding: ActivityRatingReviewBinding
    val tripsViewModel: TripsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rating_review)
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
            id = bundle.getInt(Constants.DataParsing.ID, 0)
            car_id = bundle.getInt(Constants.DataParsing.CAR_ID, 0)
            view_type = bundle.getString("viewType", "")
        }

    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsGuestToolBar(
            R.string.rating_amp_reviews_,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view) {
            binding.btnSubmit -> {
                when {
                    binding.ratingBar.rating == 0f -> {
                        uiHelper.showLongToastInCenter(this, "Enter Rating")
                    }
                    binding.descriptionEdt.text.toString().isEmpty() -> {
                        uiHelper.showLongToastInCenter(this, "Enter Review")
                    }
                    else -> {
                        if (view_type == "host") {
                            tripsViewModel.addReview(
                                id,
                                car_id,
                                binding.ratingBar.rating,
                                binding.descriptionEdt.text.toString(),
                                1,
                                true,
                                Constants.API.ADD_REVIEW,
                                this
                            )
                        } else if (view_type == "guest") {
                            tripsViewModel.addReview(
                                id,
                                car_id,
                                binding.ratingBar.rating,
                                binding.descriptionEdt.text.toString(),
                                0,
                                true,
                                Constants.API.ADD_REVIEW,
                                this
                            )
                        }

                    }
                }
            }

            binding.layoutToolBar.ivArrowBack -> {
                onBackPressed()
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.ADD_REVIEW -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                val intent = Intent()
                val bundle = Bundle()
//        bundle.putSerializable(Constants.DataParsing.MODEL, model)
                intent.putExtra(Constants.DataParsing.MODEL, bundle)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}