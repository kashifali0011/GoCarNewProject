package com.towsal.towsal.views.adapters

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.CarUnavailableDaysItemLayoutBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.settings.CarUnAvailableData
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.activities.CarAvailabilitySettingActivity

/**
 * Adapter class for host custom availability
 * */
class CarUnAvailableDatesAdapter(private val dateslist: ArrayList<CarUnAvailableData>, var context: Activity, val uiHelper: UIHelper) : BaseAdapter() {

    class ViewHolder(val itemLayoutBinding: CarUnavailableDaysItemLayoutBinding) :
        RecyclerView.ViewHolder(itemLayoutBinding.root)

    lateinit var binding: CarUnavailableDaysItemLayoutBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = DataBindingUtil.inflate<CarUnavailableDaysItemLayoutBinding>(
            LayoutInflater.from(context), R.layout.car_unavailable_days_item_layout, parent, false)
        binding.adapter = this
        return CarUnAvailableDatesAdapter.ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: ViewHolder =
            holder as ViewHolder
        viewHolder.itemLayoutBinding.tvDays.text = dateslist[position].day
        binding.position = position
        if (dateslist[position].isUnavailable == Constants.Availability.CUSTOM_AVAILABILITY) {
            if (dateslist[position].from.isNotEmpty() && dateslist[position].to.isNotEmpty())
                viewHolder.itemLayoutBinding.tvTime.text =
                    context.resources.getString(
                        R.string.to,
                        dateslist[position].from,
                        dateslist[position].to
                    )
            else
                viewHolder.itemLayoutBinding.tvTime.isVisible = false
        } else {
            viewHolder.itemLayoutBinding.tvTime.text = dateslist[position].message
        }

    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.layoutTimeDate -> {
                //have to create new screen and call api
                val bundle = Bundle()
                bundle.putString("day", dateslist[position].day)
                bundle.putInt("carId", dateslist[position].carId)
                bundle.putString("fromTime", dateslist[position].from)
                bundle.putString("toTime", dateslist[position].to)
                bundle.putInt("is_unavailable", dateslist[position].isUnavailable)
                uiHelper.openActivity(
                    context,
                    CarAvailabilitySettingActivity::class.java,
                    true,
                    bundle
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return dateslist.size
    }
}