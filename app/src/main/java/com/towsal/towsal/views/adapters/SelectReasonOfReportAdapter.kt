package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemDeleteReasonBinding
import com.towsal.towsal.databinding.SelectReasonOfReportingLayoutBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.cardetails.ReasonrResponse
import com.towsal.towsal.network.serializer.cardetails.ReportReasonDetails
import com.towsal.towsal.network.serializer.home.CarHomeModel

/**
 * Adapter class for delete listing reasons
 * */
class SelectReasonOfReportAdapter(var context: Context, val uiHelper: UIHelper, val mArrayList: ArrayList<ReportReasonDetails> , var mListener: ((Int) -> Unit)? = null) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = SelectReasonOfReportingLayoutBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.select_reason_of_reporting_layout, parent, false))

        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return mArrayList.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.binding.checkBox.text = mArrayList[position_].name
        val position = viewHolder.adapterPosition
        holder.binding.checkBox.isChecked = mArrayList[position].selected

        holder.binding.checkBox.setOnClickListener {
            mListener?.invoke(mArrayList[position].id)
            for (x in 0 until mArrayList.size) {
                mArrayList[x].selected = false
            }
            mArrayList[position].selected = true
            notifyDataSetChanged()

        }


    }


    class ViewHolder(var binding: SelectReasonOfReportingLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}