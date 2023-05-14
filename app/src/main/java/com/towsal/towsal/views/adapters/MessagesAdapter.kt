package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemChatListBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.messages.MessagesDetailModel
import com.towsal.towsal.network.serializer.trips.TripsModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.views.activities.MessageDetailActivity
import com.towsal.towsal.views.fragments.HostFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter class for threads
 * */
class MessagesAdapter(
    val context: Activity,
    val uiHelper: UIHelper,
    val callback: RecyclerViewItemClick
) : BaseAdapter(), Filterable {
    var mArrayList = ArrayList<MessagesDetailModel>()
    var mArrayListFiltered = ArrayList<MessagesDetailModel>()
    var tripsData = ArrayList<TripsModel>()
    var mListner: AllIsSeen? = null


    lateinit var binding: ItemChatListBinding

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<MessagesDetailModel>) {
        this.mArrayList = arrayList
        mArrayListFiltered = mArrayList
        notifyDataSetChanged()
    }

    /**
     * Function for getting attached list
     * */
    fun getList(): ArrayList<MessagesDetailModel> {
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
            R.layout.item_chat_list, parent, false
        )
        binding.adapter = this

        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return mArrayListFiltered.size
    }

    /**
     * Function for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.messageView -> {
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.ID,
                    mArrayListFiltered[position].threadId
                )
                uiHelper.openActivityForResult(
                    context,
                    MessageDetailActivity::class.java,
                    true,
                    Constants.RequestCodes.GUEST_TRIP_DETAIL, bundle
                )
            }
        }

    }


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.binding.position = holder.adapterPosition

        Log.d("profilePicc", BuildConfig.IMAGE_BASE_URL + mArrayListFiltered[position].sender_profile_image)

        Glide.with(context)
            .load(BuildConfig.IMAGE_BASE_URL + mArrayListFiltered[position].sender_profile_image)
            .into(viewHolder.binding.image)
        if (mArrayListFiltered[position].is_seen == 0) {
            viewHolder.binding.messageIcon.visibility = View.VISIBLE
        } else {
            viewHolder.binding.messageIcon.visibility = View.GONE
        }

        if (isAllSeen()) {
            mListner?.onAllMessagesSeen()
        }
        viewHolder.binding.tvBookingIdTxt.text =
            mArrayListFiltered[position].booking_id.toString()
        viewHolder.binding.name.text = mArrayListFiltered[viewHolder.binding.position].car
     /*   viewHolder.binding.name.text =
            mArrayListFiltered[viewHolder.binding.position].car_make + " " + mArrayListFiltered[viewHolder.binding.position].car_model + " " + mArrayListFiltered[viewHolder.binding.position].car_year*/
        viewHolder.binding.message.text = mArrayListFiltered[position].message
        viewHolder.binding.senderName.text = mArrayListFiltered[position].sender_name


        val date = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            HostFragment.dateTimeConversionFormat,
            mArrayListFiltered[position].message_at
        )
        viewHolder.binding.time.text = date

        viewHolder.binding.executePendingBindings()

    }

    /**
     * Function for checking that all items are read or not.
     * */
    fun isAllSeen(): Boolean {

        var isSeen = true
        for (row in mArrayListFiltered) {
            if (row.is_seen == 0) {
                isSeen = false
                break
            }
        }
        return isSeen
    }

    class ViewHolder(var binding: ItemChatListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    mArrayListFiltered = mArrayList
                } else {
                    val filteredList: ArrayList<MessagesDetailModel> = ArrayList()
                    for (row in mArrayList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        //it was done before
                        /*if (row.sender_name.toLowerCase(Locale.ROOT)
                                .contains(charString.toLowerCase(Locale.ROOT))
                        ) {
                            filteredList.add(row)
                        }
*/
                        //trying to set the filter on the chat
                        Log.d("carMake", "carMake = " + row.car_make)
                        if (row.car_make.lowercase(Locale.ROOT)
                                .contains(charString.lowercase(Locale.ROOT))
                        ) {
                            filteredList.add(row)
                        } else if (row.car_model.lowercase(Locale.ROOT)
                                .contains(charString.lowercase(Locale.ROOT))
                        ) {
                            filteredList.add(row)
                        }
                    }
                    mArrayListFiltered = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = mArrayListFiltered
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                mArrayListFiltered =
                    filterResults.values as ArrayList<MessagesDetailModel>
                if (mArrayListFiltered.isNullOrEmpty()) {
                    callback.onItemClick(0)
                } else {
                    callback.onItemClick(1)
                }
                notifyDataSetChanged()
            }
        }
    }

    /**
     * Function for setting listener
     * */
    fun setListener(listener: AllIsSeen) {
        mListner = listener
    }

    interface AllIsSeen {
        fun onAllMessagesSeen()
    }

}
