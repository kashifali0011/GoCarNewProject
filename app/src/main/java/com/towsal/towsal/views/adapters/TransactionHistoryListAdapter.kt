package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemTranactionHistoryBinding
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.trips.TripsModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.activities.TransactionDetailActivity

/**
 * Adapter class for for transaction history as host or gues
 * */
class TransactionHistoryListAdapter(
    var context: Activity,
    val uiHelper: UIHelper,
    val isHost: Boolean
) : BaseAdapter() {
    var mArrayList: ArrayList<TripsModel> = ArrayList()
    lateinit var binding: ItemTranactionHistoryBinding

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<TripsModel> {
        return mArrayList
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<TripsModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_tranaction_history, parent, false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return mArrayList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        when (view.id) {
            R.id.messageView -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.VIEW_TYPE, mArrayList[position].viewType)
                bundle.putInt(Constants.DataParsing.ID, mArrayList[position].id)
                if (mArrayList[position].viewType == TripsMultiViewAdapter.HOST_TYPE) {
                    bundle.putString(Constants.DataParsing.NAME, mArrayList[position].booked_by)
                } else {
                    bundle.putString(Constants.DataParsing.NAME, mArrayList[position].host_name)
                }
                bundle.putString(Constants.DataParsing.IMAGE, mArrayList[position].user_image)
                bundle.putBoolean(Constants.DataParsing.IS_HOST, isHost)
                uiHelper.openActivity(context, TransactionDetailActivity::class.java, bundle)

            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        val position = viewHolder.adapterPosition
        viewHolder.binding.position = position
        viewHolder.binding.bookingId.text =
            context.getString(R.string.book_id) + " " + mArrayList[position].id
        if (mArrayList[position].viewType == TripsMultiViewAdapter.HOST_TYPE) {
            viewHolder.binding.guestName.text = context.getString(R.string.guest_name) + ": "
            viewHolder.binding.tvName.text = mArrayList[position].booked_by
            viewHolder.binding.tvEarnedAndPaid.text = context.getString(R.string.earned) + ": "
            viewHolder.binding.tvSAR.text =
                context.getString(R.string.sar) + " " + mArrayList[position].total
//            + "\n" + context.getString(
//                    R.string.earned
//                ) + " " + context.getString(
//                    R.string.sar
//                ) + " " + mArrayList[position].total
        } else {
            viewHolder.binding.guestName.text = context.getString(R.string.host_name) + ": "
            viewHolder.binding.tvName.text = mArrayList[position].host_name
            viewHolder.binding.tvEarnedAndPaid.text = context.getString(R.string.paid) + ": "
            viewHolder.binding.tvSAR.text = context.getString(
                R.string.sar
            ) + " " + mArrayList[position].total
        }
        uiHelper.glideLoadImage(
            context,
            BuildConfig.IMAGE_BASE_URL + mArrayList[position].user_image,
            viewHolder.binding.image
        )
        viewHolder.binding.executePendingBindings()
    }

    class ViewHolder(var binding: ItemTranactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)
}