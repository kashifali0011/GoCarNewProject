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
import com.towsal.towsal.databinding.AdapterVehicleListBinding
import com.towsal.towsal.databinding.ItemCarDocsBinding
import com.towsal.towsal.databinding.ItemVehicleSettingBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.network.serializer.settings.VehicleSettingModel

/**
 * Adapter class for vehicle setting
 * */
class VehicleTypeAdapter(private val settingList: List<CarMakeInfoModel>, var context: Activity, val uiHelper: UIHelper ,var mListener: OnItemClickListener?) : BaseAdapter() {

    var number = 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AdapterVehicleListBinding.bind(LayoutInflater.from(context).inflate(R.layout.adapter_vehicle_list, parent, false))
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.cbVehicleList.text = settingList[position].name
        viewHolder.binding.cbVehicleList.setOnClickListener {
            if (viewHolder.binding.cbVehicleList.isChecked){
                mListener?.onItemClick(position , settingList[position].id, settingList[position].name , true)

            }else{
                mListener?.onItemClick(position , settingList[position].id, settingList[position].name , false)
            }
        }

        if (number == 1){
            number++
            if (position == 0){
                mListener?.onItemClick(9999, settingList[position].id , settingList[position].name , true)
                viewHolder.binding.cbVehicleList.isChecked = true
            }
        }


    }

    override fun getItemCount(): Int {
        return settingList.size
    }

    class ViewHolder(var binding: AdapterVehicleListBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {
        fun onItemClick(position: Int , carId: Int ,carName: String, isCheck: Boolean )
    }
}