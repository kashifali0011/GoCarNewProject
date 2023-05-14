package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.SimpleTextViewLayoutBinding
import com.towsal.towsal.views.listeners.ClickListener

/**
 * Adapter class for car makes
 * */
class MakeAdapter(val context: Context, var list: List<String>, private val clickListener: ClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = SimpleTextViewLayoutBinding.bind(LayoutInflater.from(context).inflate(R.layout.simple_text_view_layout, parent, false))
        return ViewHolder(binding = binding)
    }

    /**
     * Function for changing list with new list
     * */
    fun changeList(list: List<String>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.textView.text = list[position]
        viewHolder.binding.textView.setOnClickListener {
            clickListener.onClick(viewHolder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: SimpleTextViewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)


}