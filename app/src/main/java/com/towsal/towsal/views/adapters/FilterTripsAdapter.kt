package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemFilterTripBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.trips.FilterTripsModel

/**
 * Adapter class for filter trips
 * */
class FilterTripsAdapter(
    var context: Context,
    val uiHelper: UIHelper,
    val callback: RecyclerViewItemClick,
    val hideImage: Boolean
) : BaseAdapter() {

    var mArrayList = ArrayList<FilterTripsModel>()

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<FilterTripsModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<FilterTripsModel> {
        return mArrayList
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = ItemFilterTripBinding.bind(LayoutInflater.from(context).inflate(R.layout.item_filter_trip, parent, false))
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return mArrayList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        when (view.id) {
            R.id.filterItem -> {
                callback.onItemClick(position)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder

        viewHolder.binding.position = position_

        viewHolder.binding.image.isVisible = !hideImage

        when (position_) {
            0 -> {
                viewHolder.binding.image.setImageResource(R.drawable.ic_filter_all)
            }
            1 -> {
                viewHolder.binding.image.setImageResource(R.drawable.ic_filter_your_trip)
            }
            2 -> {
                viewHolder.binding.image.setImageResource(R.drawable.ic_filter_your_car)
            }
        }
        if (mArrayList[position_].selected) {
            viewHolder.binding.checkImage.setImageResource(R.drawable.ic_check_filter)
        } else {
            viewHolder.binding.checkImage.setImageResource(R.drawable.ic_unselected_option)
        }
        viewHolder.binding.text.text = mArrayList[position_].name


    }

    class ViewHolder(var binding: ItemFilterTripBinding) :
        RecyclerView.ViewHolder(binding.root)
}
