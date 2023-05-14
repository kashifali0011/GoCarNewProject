package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemSafetyListBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel

/**
 * Adapter class for safety list
 * */
class SafetyListAdapter(
    var context: Context,
    val uiHelper: UIHelper
) : BaseAdapter() {

    var mArrayList = ArrayList<CarMakeInfoModel>()
    lateinit var binding: ItemSafetyListBinding
    var holder = 0

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
            R.layout.item_safety_list, parent, false
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
        binding.name.text = mArrayList[binding.position].name

    }

    class ViewHolder(var binding: ItemSafetyListBinding) :
        RecyclerView.ViewHolder(binding.root)

}