package com.towsal.towsal.views.adapters

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemVehicleSettingBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.settings.VehicleSettingModel

/**
 * Adapter class for vehicle setting
 * */
class VehicleSettingAdapter(private val settingList: List<VehicleSettingModel>, var context: Activity, val uiHelper: UIHelper, private val recyclerViewItemClick: RecyclerViewItemClick) : BaseAdapter() {

    class ViewHolder(val itemLayoutBinding: ItemVehicleSettingBinding) :
        RecyclerView.ViewHolder(itemLayoutBinding.root)

    lateinit var binding: ItemVehicleSettingBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.item_vehicle_setting, parent, false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return settingList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.viewClick -> {
                recyclerViewItemClick.onItemClick(position)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.itemLayoutBinding.position = viewHolder.adapterPosition
        binding.imageView.setImageResource(settingList[viewHolder.itemLayoutBinding.position].image)
      /*  binding.icInfo.setImageResource(if(settingList[position].infoImg != 0) settingList[position].infoImg else R.drawable.ic_left_arrow)
        binding.icInfo.isVisible = settingList[position].infoImg != 0
        binding.icArrow.isInvisible = binding.icInfo.isVisible*/
        binding.name.text = settingList[viewHolder.itemLayoutBinding.position].name
        binding.name.setTextColor(if(settingList[position].infoImg != 0)  Color.parseColor("#FF7170") else Color.parseColor("#000000" ))

        if (position == settingList.size - 1){
            binding.view.isVisible = false
        }


    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }



}