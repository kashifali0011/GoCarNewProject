package com.towsal.towsal.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.AdapterFromTimeSlotBinding
import com.towsal.towsal.databinding.AdapterTimeSlotBinding
import com.towsal.towsal.databinding.SimpleTextViewLayoutBinding
import com.towsal.towsal.views.listeners.ClickListener

class FromDateAdapter(
    val context: Context,
    var list: List<String>,
    var mListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AdapterFromTimeSlotBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.adapter_from_time_slot, parent, false)
        )
        return ViewHolder(binding = binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.tvTimeSelect.text = list[position]


        viewHolder.binding.tvTimeSelect.setOnClickListener {
            mListener.onItemClick(position)
        }
        if (position == 0) {
            viewHolder.binding.tvTimeSelectView.visibility = View.INVISIBLE
            viewHolder.binding.tvTimeSelect.visibility = View.GONE
            viewHolder.binding.tvTimeSelectView.text = list[position]
        } else {
            viewHolder.binding.tvTimeSelectView.visibility = View.GONE
            viewHolder.binding.tvTimeSelect.visibility = View.VISIBLE
            viewHolder.binding.tvTimeSelectView.text = list[position]
        }
        /*
               if (position == list.size - 1){
                   viewHolder.binding.view1.visibility  = View.VISIBLE
               }else{
                   viewHolder.binding.view1.visibility  = View.GONE
               }*/
    }


    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    class ViewHolder(val binding: AdapterFromTimeSlotBinding) :
        RecyclerView.ViewHolder(binding.root)
}