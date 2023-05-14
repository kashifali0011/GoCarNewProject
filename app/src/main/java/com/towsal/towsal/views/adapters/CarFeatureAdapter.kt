package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCarFeatureBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel

/**
 * Adapter for car features
 * */
class CarFeatureAdapter(
    var context: Context,
    val uiHelper: UIHelper,
    private val isRadioButtonShow : Boolean = true
) : BaseAdapter() {

    var mArrayList = ArrayList<CarMakeInfoModel>()

    /**
     *  Functions for setting up list
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

        val binding = ItemCarFeatureBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_car_feature, parent, false)
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
        viewHolder.binding.tvFeatureName.text = mArrayList[position].name
        viewHolder.binding.scFeatureSelected.isChecked = mArrayList[position].checked
        viewHolder.binding.scFeatureSelected.isInvisible = !isRadioButtonShow
        viewHolder.binding.ivCarFeatures.loadImage(mArrayList[position].image_url)

        viewHolder.binding.scFeatureSelected.setOnCheckedChangeListener { buttonView, isChecked ->
            mArrayList[position].checked = isChecked
        }

    }

    class ViewHolder(var binding: ItemCarFeatureBinding) :
        RecyclerView.ViewHolder(binding.root)
}
