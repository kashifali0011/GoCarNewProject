package com.towsal.towsal.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemRatingsLayoutBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.userdetails.Reviews
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil


class RatingsAdapter(
    val uiHelper: UIHelper
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mArrayList: List<Reviews> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemRatingsLayoutBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ratings_layout, parent, false)
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.rbRatings.rating = mArrayList[position].rating ?: 0f
        viewHolder.binding.tvRatingDescription.text = mArrayList[position].review
        viewHolder.binding.tvDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateFormat,
            Constants.DateFormatWithDay,
            mArrayList[position].createdAt
        )

        viewHolder.binding.tvUserName.text = mArrayList[position].ratingReviewBy + ","
        mArrayList[position].ratingReviewByImage?.let {
            uiHelper.glideLoadImage(
                viewHolder.binding.tvDate.context,
                BuildConfig.IMAGE_BASE_URL + it,
                viewHolder.binding.ivUserProfileImage
            )
        }

    }

    override fun getItemCount() = mArrayList.size
    fun setList(carReviewsList: List<Reviews>) {
        mArrayList = carReviewsList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemRatingsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}