package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.AdapterShowDateBinding
import com.towsal.towsal.databinding.ItemReciverMessageBinding
import com.towsal.towsal.databinding.ItemSendMessageBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.messages.MessagesDetailModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.views.fragments.HostFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter class for messages adapter
 * */
class MessagesDetailAdapter(var context: Activity, val uiHelper: UIHelper, var userModel: UserModel/*, var mmArrayList: ArrayList<MessagesDetailModel>*/) : BaseAdapter() {

    companion object {
        const val DATE = 1
        const val SEND_MESSAGE = 2
        const val RECEIVE_MESSAGE = 3
    }

    var mArrayList = ArrayList<MessagesDetailModel>()


    fun notifyItems(arrayList: ArrayList<MessagesDetailModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    fun getArrayList(): ArrayList<MessagesDetailModel> {
        return if (!mArrayList.isNullOrEmpty()) mArrayList
        else ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        RECEIVE_MESSAGE -> {
            //Check if View Type is Guest Then Set Guest Item
            ViewHolderReceiveMessage(
                ItemReciverMessageBinding.bind(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_reciver_message, parent, false)
                )
            )
        }
        SEND_MESSAGE -> {
            //Check if View Type is Host Then Set Host Item
            ViewHolderSendMessage(
                ItemSendMessageBinding.bind(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_send_message, parent, false)
                )
            )
        }
        DATE -> {
            ViewHolderAdapterShowDateBinding(
                AdapterShowDateBinding.bind(
                    LayoutInflater.from(parent.context).inflate(R.layout.adapter_show_date, parent, false)
                )
            )
        }
        else -> {
            ViewHolderReceiveMessage(
                ItemReciverMessageBinding.bind(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_reciver_message, parent, false)
                )
            )
        }
    }


    override fun getItemCount(): Int {
        return mArrayList.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        when (holder) {
            is ViewHolderSendMessage -> {
                val position = holder.adapterPosition
                holder.binding.senderText.text = mArrayList[position].message
                holder.binding.tvStatus.isVisible = mArrayList[position].is_seen == 1
                val date = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat, HostFragment.timeFormatNew, mArrayList[position].message_at
                )
                holder.binding.time.text = date

                holder.binding.executePendingBindings()
            }
            is ViewHolderReceiveMessage -> {
                val position = holder.adapterPosition
                holder.binding.receiverText.text = mArrayList[position].message
                val dateFormat = SimpleDateFormat(HostFragment.dateTimeServerFormat)
                dateFormat.timeZone = TimeZone.getDefault()
              /*  val date = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat, HostFragment.timeFormatNew, mArrayList[position].message_at
                )*/
              //  holder.binding.time.text = date
                holder.binding.executePendingBindings()
            }
            is ViewHolderAdapterShowDateBinding -> {
                val position = holder.adapterPosition
                holder.binding.tvDate.text = mArrayList[position].message
            }

        }
    }

    override fun getItemViewType(position: Int) = if (mArrayList[position].viewType == 2) {
        DATE
    } else {
        if (mArrayList[position].sender_id == userModel.id) {
            SEND_MESSAGE
        } else {
            RECEIVE_MESSAGE
        }
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolderSendMessage(var binding: ItemSendMessageBinding) : RecyclerView.ViewHolder(binding.root)

    class ViewHolderReceiveMessage(var binding: ItemReciverMessageBinding) : RecyclerView.ViewHolder(binding.root)

    class ViewHolderAdapterShowDateBinding(var binding: AdapterShowDateBinding) : RecyclerView.ViewHolder(binding.root)

}
