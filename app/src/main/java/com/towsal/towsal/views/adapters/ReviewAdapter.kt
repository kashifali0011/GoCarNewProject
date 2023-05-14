package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemReviewBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.settings.ReviewsModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil

/**
 * Adapter class for reviews
 * */
class ReviewAdapter(
    val context: Activity,
    val uiHelper: UIHelper,
    val mArrayList: ArrayList<ReviewsModel>
) : BaseAdapter() {
    lateinit var binding: ItemReviewBinding

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<ReviewsModel> {
        return mArrayList
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_review,
            parent,
            false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return if (mArrayList.size > 4) 4 else mArrayList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {

        }

    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.binding.position = holder.adapterPosition
        viewHolder.binding.model = mArrayList[viewHolder.binding.position]
        Glide.with(context)
            .load(BuildConfig.IMAGE_BASE_URL + mArrayList[viewHolder.binding.position].rating_review_by_image)
            .into(viewHolder.binding.image)

        viewHolder.binding.ratingBar.rating = mArrayList[viewHolder.binding.position].rating
        viewHolder.binding.executePendingBindings()
        var date = DateUtil.changeDateFormat(
            requiredFormat = Constants.DateFormatWithDay,
            currentFormat = Constants.ServerDateTimeFormat,
            dateString = mArrayList[viewHolder.binding.position].reviewAt
        )
        binding.name.text = "${mArrayList[position_].rating_review_by.split(" ")[0]}, $date"
    }

    class ViewHolder(var binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)
}
