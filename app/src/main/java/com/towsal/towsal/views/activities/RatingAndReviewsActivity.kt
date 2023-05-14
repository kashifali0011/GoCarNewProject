package com.towsal.towsal.views.activities

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityRatingAndReviewsBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.network.serializer.userdetails.Reviews
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.adapters.ReviewsAdapter
import com.towsal.towsal.views.adapters.ReviewsNewAdapter

/**
 * Activity class for rating and reviews
 * */
class RatingAndReviewsActivity : BaseActivity() {

    lateinit var binding: ActivityRatingAndReviewsBinding
    lateinit var reviewsList: List<Reviews>
    var heading = ""
    var review = ""
    var rating = 0f
    var ratingLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rating_and_reviews)
        uiHelper.hideActionBar(supportActionBar)
        setData()
        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.review,
            actionBar = null,
            arrowVisible = true
        )
    }

    /**
     * Function for setting up data in views
     * */


    private fun setData() {
        if (intent.extras != null) {
            heading = intent.extras!!.getString(Constants.DataParsing.HEADING) as String
            review = intent.extras!!.getString(Constants.DataParsing.REVIEW) as String
            rating = intent.extras!!.getFloat(Constants.DataParsing.RATING) as Float
            reviewsList = intent.extras!!.getSerializable(Constants.DataParsing.REVIEWS_LIST) as List<Reviews>
            ratingLength = reviewsList.size
        }
        binding.tvHowItWorks.text = heading
        binding.ratingBar.rating = rating
        binding.tvReview.text = "$ratingLength ratings"

                //  actionBarSetting()
        binding.recyclerView.apply {
            adapter = ReviewsNewAdapter(this@RatingAndReviewsActivity, reviewsList, uiHelper)
            layoutManager = LinearLayoutManager(this@RatingAndReviewsActivity)
        }
    }

}