package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.AdapterShowDateBinding
import com.towsal.towsal.databinding.ItemReciverMessageBinding
import com.towsal.towsal.databinding.ItemSendMessageBinding
import com.towsal.towsal.databinding.SimpleTextViewLayoutBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.messages.DateAndMessageList
import com.towsal.towsal.network.serializer.messages.GetMessageDetails
import com.towsal.towsal.network.serializer.messages.MessagesDetailModel
import com.towsal.towsal.views.fragments.HostFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Adapter class for messages adapter
 * */
class MessagesDateAdapter(var context: Activity, val uiHelper: UIHelper, var userModel: UserModel ,var mArrayList: ArrayList<DateAndMessageList>) : BaseAdapter() {

   /* var mArrayList = ArrayList<DateAndMessageList>()

    fun notifyItems(arrayList: ArrayList<DateAndMessageList>) {
            this.mArrayList = arrayList
            notifyDataSetChanged()
        }

     fun getArrayList(): ArrayList<DateAndMessageList> {
            return if (!mArrayList.isNullOrEmpty())
                mArrayList
            else
                ArrayList()
        }*/

    lateinit var binding: AdapterShowDateBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       binding = AdapterShowDateBinding.bind(LayoutInflater.from(context).inflate(R.layout.adapter_show_date, parent, false))
        return ViewHolder(binding = binding)

    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
       val mHolder = holder as ViewHolder
        mHolder.binding.tvDate.text = mArrayList[position_].date

      /*  val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)

        mHolder.binding.listMessages.layoutManager = LinearLayoutManager(context)
        var adapter = MessagesDetailAdapter(context , uiHelper , userModel , mArrayList = mArrayList[position_].messagesList)
        mHolder.binding.listMessages.adapter = adapter*/


    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }


       class ViewHolder(val binding: AdapterShowDateBinding) :
            RecyclerView.ViewHolder(binding.root)

}
