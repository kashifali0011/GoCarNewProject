package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCarFeatureVerticalBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel

/**
 * Adapter for car basic features
 * */
class CarFeatureAdapterVertical(
    var context: Context,
    val uiHelper: UIHelper
) : BaseAdapter() {

    var mArrayList = ArrayList<CarMakeInfoModel>()
    lateinit var binding: ItemCarFeatureVerticalBinding
    var holder = 0

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<CarMakeInfoModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    /**
     *  Functions for getting attached list
     * */
    fun getList() = mArrayList

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_car_feature_vertical, parent, false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount() = mArrayList.size


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        val position = viewHolder.adapterPosition
        viewHolder.binding.position = position
        uiHelper.glideLoadImage(
            context,
            mArrayList[position].image_url,
            binding.image
        )
        binding.text.text = mArrayList[position].name
    }

    class ViewHolder(var binding: ItemCarFeatureVerticalBinding) :
        RecyclerView.ViewHolder(binding.root)

}
