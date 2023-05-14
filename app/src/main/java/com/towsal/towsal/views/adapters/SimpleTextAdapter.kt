package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemSimpleTextBinding
import com.towsal.towsal.helper.UIHelper

/**
 * Adapter class for simple adapter used for showing names of steps in process
 * */
class SimpleTextAdapter(
    var context: Context,
    val uiHelper: UIHelper
) : BaseAdapter() {

    var mArrayList = ArrayList<String>()
    lateinit var binding: ItemSimpleTextBinding

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<String>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<String> {
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
            R.layout.item_simple_text, parent, false
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
        binding.textView.text = mArrayList[position]
    }

    class ViewHolder(var binding: ItemSimpleTextBinding) :
        RecyclerView.ViewHolder(binding.root)
}
