package com.towsal.towsal.views.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemGuestReviewBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.settings.GuestReviewsModel

/**
* Adapter class for guest reviews
* */
class GuestReviewAdapter(
    val context: Activity,
    val uiHelper: UIHelper,
    val mArrayList: ArrayList<GuestReviewsModel>
) : BaseAdapter() {

    lateinit var binding: ItemGuestReviewBinding

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<GuestReviewsModel> {
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
            R.layout.item_guest_review,parent,false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder : ViewHolder = holder as ViewHolder
        viewHolder.binding.position = holder.adapterPosition
        viewHolder.binding.model = mArrayList[viewHolder.binding.position]
        Glide.with(context)
            .load(BuildConfig.IMAGE_BASE_URL + mArrayList[viewHolder.binding.position].rating_review_by_image)
            .into(viewHolder.binding.image)
        viewHolder.binding.ratingBar.rating = mArrayList[viewHolder.binding.position].rating
        viewHolder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {

        }

    }

    class ViewHolder(var binding: ItemGuestReviewBinding) :
        RecyclerView.ViewHolder(binding.root)
}