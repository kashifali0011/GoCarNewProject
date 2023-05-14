package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.AdapterTimeSlotBinding

class ToDateAdapter(val context: Context , var list: List<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AdapterTimeSlotBinding.bind(LayoutInflater.from(context).inflate(R.layout.adapter_time_slot, parent, false))
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.tvTimeSelect.text = list[position].ifEmpty { " " }
    }

    override fun getItemCount(): Int {
        return list.size

    }

    fun getLists() : List<String> {
        return list
    }
    class ViewHolder(val binding: AdapterTimeSlotBinding):
            RecyclerView.ViewHolder(binding.root)
}