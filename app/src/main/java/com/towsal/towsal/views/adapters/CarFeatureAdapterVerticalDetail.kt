package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCarFeatureVerticalDetailBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel

/**
 * Adapter class for car features
 * */
class CarFeatureAdapterVerticalDetail(
    var context: Context,
    val uiHelper: UIHelper,
    val isBasic: Boolean = false,
) : BaseAdapter() {

    var mArrayList = ArrayList<CarMakeInfoModel>()
    lateinit var binding: ItemCarFeatureVerticalDetailBinding
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
    fun getList(): ArrayList<CarMakeInfoModel> {
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
            R.layout.item_car_feature_vertical_detail, parent, false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return if (mArrayList.size > 4)
            4
        else mArrayList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        val position = viewHolder.adapterPosition
        viewHolder.binding.position = position
//        uiHelper.glideLoadImage(
//            context,
//            BuildConfig.IMAGE_BASE_URL + mArrayList[position].image_url,
//            binding.image
//        )
        binding.text.text = if(isBasic) mArrayList[position_].value + " " +  mArrayList[position_].name else mArrayList[position_].name
    }

    class ViewHolder(var binding: ItemCarFeatureVerticalDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

}
