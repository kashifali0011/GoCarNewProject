package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCarFeatureViewOnlyBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel

/**
 * Adapter class for basic feature screen
 * */
class BasicCarFeatureViewOnlyAdapter(var context: Context, val uiHelper: UIHelper) : BaseAdapter() {
    var mArrayList = ArrayList<CarMakeInfoModel>()
    lateinit var binding: ItemCarFeatureViewOnlyBinding

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
            R.layout.item_car_feature_view_only, parent, false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return mArrayList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder

        val position = viewHolder.adapterPosition
        viewHolder.binding.position = position
        Glide.with(context).load(BuildConfig.IMAGE_BASE_URL + mArrayList[position].image_url)
            .into(binding.image)
        binding.text.text = mArrayList[position].name
        binding.checkBox.isChecked = mArrayList[position].checked
        binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            mArrayList[position].checked = isChecked
        }

    }

    class ViewHolder(var binding: ItemCarFeatureViewOnlyBinding) :
        RecyclerView.ViewHolder(binding.root)
}