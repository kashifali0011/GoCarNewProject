package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemDeleteReasonBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.home.CarHomeModel

/**
 * Adapter class for delete listing reasons
 * */
class DeleteCarReasonListingAdapter(var context: Context, val uiHelper: UIHelper, val mArrayList: ArrayList<CarHomeModel>, var mListener: onClickListener) : BaseAdapter() {
    var selectedPosition = -1
    fun getList(): ArrayList<CarHomeModel> {
        return mArrayList
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = ItemDeleteReasonBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.item_delete_reason, parent, false))
       // binding.adapter = this
        return ViewHolder(binding = binding)
    }


    override fun getItemCount(): Int {
        return mArrayList.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
       // viewHolder.binding.checkBox.text = mArrayList[position_].name
        viewHolder.binding.tvDeleteReason.text = mArrayList[position_].name

      /*  if (mArrayList[position_].selected){
            viewHolder.binding.tvDeleteReason.setTextColor(context.getColor(R.color.send_msg_bg))
        }else{
            viewHolder.binding.tvDeleteReason.setTextColor(context.getColor(R.color.black_text_black_variation_18))
        }*/
        viewHolder.binding.tvDeleteReason.setOnClickListener {
            mListener.onClick(mArrayList[position_].name)
            if (selectedPosition != -1 && selectedPosition != position_)
                mArrayList[selectedPosition].selected = false
            mArrayList[position_].selected = !mArrayList[position_].selected
            selectedPosition = position_
            notifyDataSetChanged()
        }

        if (position_ == mArrayList.size -1){
            viewHolder.binding.viewReason.visibility = View.GONE
        }

    }
    interface onClickListener{
        fun onClick(selectItem: String)
    }

    class ViewHolder(var binding: ItemDeleteReasonBinding) :
        RecyclerView.ViewHolder(binding.root)
}