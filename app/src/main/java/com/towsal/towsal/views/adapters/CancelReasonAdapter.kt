package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCancelReasonBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.home.CarHomeModel


/**
 * Adapter class for cancel reasons
 * */
class CancelReasonAdapter(var context: Context, val uiHelper: UIHelper, val mArrayList: ArrayList<CarHomeModel>, var mListener: ((Int) -> Unit)? = null ) : BaseAdapter() {

    lateinit var binding: ItemCancelReasonBinding

    /**
     *  Functions for getting attached list
     * */
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

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_cancel_reason, parent, false
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
        holder.binding.position = position
        holder.binding.checkBox.text = mArrayList[position].name
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

    class ViewHolder(var binding: ItemCancelReasonBinding) :
        RecyclerView.ViewHolder(binding.root)


}