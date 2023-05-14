package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemStepcountBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.carlist.StepViewModel

/**
 * Adapter class for showing steps in process
 * */
class StepViewAdapter(
    var context: Context,
    val uiHelper: UIHelper,
    val userOtherItemLayout: Boolean
) : BaseAdapter() {

    var mArrayList = ArrayList<StepViewModel>()
    lateinit var binding: ItemStepcountBinding

    /**
     * Function for setting up list with new list
     * */
    fun setList(arrayList: ArrayList<StepViewModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layout = R.layout.item_stepcount
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layout, parent, false
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
        binding.view.isVisible = ((mArrayList.size - 1) != position)
        if (mArrayList[position].selected) {
            binding.textView2.background = context.getDrawable(R.drawable.circle_bg_fill)
            binding.textView2.setTextColor(context.getColor(R.color.colorWhite))
        } else {
            binding.textView2.background = context.getDrawable(R.drawable.circle_bg_hollow)
            binding.textView2.setTextColor(context.getColor(R.color.colorAccent))
        }
        binding.textView2.text = "" + mArrayList[position].number

        binding.executePendingBindings()
    }


    class ViewHolder(var binding: ItemStepcountBinding) :
        RecyclerView.ViewHolder(binding.root)
}
