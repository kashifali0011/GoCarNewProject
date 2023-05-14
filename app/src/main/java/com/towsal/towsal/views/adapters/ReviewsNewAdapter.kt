package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ReviewsItemLayoutBinding
import com.towsal.towsal.databinding.ReviewsItemLayoutNewBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.userdetails.Reviews
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil

/**
 * Adapter class for reviews
 * */
class ReviewsNewAdapter(
    val context: Context,
    private val reviewsList: List<Reviews>,
    val uiHelper: UIHelper
) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ReviewsItemLayoutNewBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.reviews_item_layout_new, parent, false)
        )
        return ReviewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder = holder as ReviewsViewHolder
        val position = viewHolder.adapterPosition

        val review = reviewsList[position]

        holder.binding.reviewerCity.text = review.ratingReviewBy+","
      //  holder.binding.reviewDate.text = ", " + review.createdAt

        holder.binding.reviewDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateFormat,
            Constants.DateFormatWithDay,
            review.createdAt
        )

        holder.binding.ratingBar.rating = review.rating!!
        holder.binding.reviewDescription.text = review.review
        uiHelper.glideLoadImage(
            context,
            BuildConfig.IMAGE_BASE_URL + review.ratingReviewByImage,
            holder.binding.reviewerImage
        )


    }

    override fun getItemCount(): Int {
        return reviewsList.size
    }

    class ReviewsViewHolder(val binding: ReviewsItemLayoutNewBinding) :
        RecyclerView.ViewHolder(binding.root)
}