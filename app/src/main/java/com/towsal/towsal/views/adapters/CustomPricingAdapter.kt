package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCustomPricingBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.settings.CustomPriceModel

/**
 * Adapter class for day selection for pricing
 * */
class CustomPricingAdapter(
    var context: Context,
    val uiHelper: UIHelper,
    val callback: RecyclerViewItemClick,
    var mArrayList: ArrayList<CustomPriceModel>
) : BaseAdapter() {


    /**
     *  Functions for getting attached list
     * */
    fun getList() = mArrayList

    override fun getItemViewType(position: Int) = position


    override fun getItemId(position: Int) = position.toLong()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = ItemCustomPricingBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_custom_pricing, parent, false)
        )
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount() = mArrayList.size

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        when (view.id) {
            R.id.dayName -> {
                callback.onItemClick(position)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.binding.apply {
            position = position_
            dayName.text = mArrayList[viewHolder.adapterPosition].dayName
            if (mArrayList[position].price != null) {
                edtCustomPrice.setText(
                    mArrayList[position].price
                )
            }
            edtCustomPrice.addTextChangedListener {
                if (
                    it.toString().isNotEmpty()
                ) {
                    mArrayList[position].price = it.toString()
                } else {
                    mArrayList[position].price = null
                }
            }
        }


    }

    class ViewHolder(var binding: ItemCustomPricingBinding) :
        RecyclerView.ViewHolder(binding.root)

}
