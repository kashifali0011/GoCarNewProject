package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.towsal.towsal.R
import com.towsal.towsal.databinding.SimpleTextViewLayoutBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.views.listeners.ClickListener

/**
 * Adapter class for auto complete places view screen
 * */
class AutoCompletePlacesAdapter(val context: Context, var list: List<AutocompletePrediction>, private val clickListener: ClickListener) : BaseAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = SimpleTextViewLayoutBinding.bind(LayoutInflater.from(context).inflate(R.layout.simple_text_view_layout, parent, false))
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.textView.text = list[position].getFullText(null).toString()
        viewHolder.binding.textView.setOnClickListener {
            it.preventDoubleClick()
            clickListener.onClick(viewHolder.adapterPosition)
        }
    }

    /**
     *  Functions for changing list with new list
     * */
    fun changeList(list: List<AutocompletePrediction>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: SimpleTextViewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}