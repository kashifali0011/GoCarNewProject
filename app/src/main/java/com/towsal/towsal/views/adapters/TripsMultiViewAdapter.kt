package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemGuestTripBinding
import com.towsal.towsal.databinding.ItemHostTripBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.trips.TripsModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.views.activities.GuestReviewsListActivity
import com.towsal.towsal.views.activities.TripDetailsActivity
import android.widget.Toast
import com.towsal.towsal.extensions.showToast


/**
 * Adapter class for trips
 * */
class TripsMultiViewAdapter(
    var context: Activity,
    val uiHelper: UIHelper
) : BaseAdapter() {

    companion object {
        const val HOST_TYPE = 1
        const val GUEST_TYPE = 2
    }

    var mArrayList = ArrayList<TripsModel>()
    lateinit var guestTripBinding: ItemGuestTripBinding
    lateinit var hostTripBinding: ItemHostTripBinding

    /**
     * Function for changing list
     * */
    fun notifyItems(arrayList: ArrayList<TripsModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            GUEST_TYPE -> {
                //Check if View Type is Guest Then Set Guest Item
                guestTripBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_guest_trip, parent, false
                )
                guestTripBinding.adapter = this
                return ViewHolderGuestTrip(guestTripBinding)
            }
            HOST_TYPE -> {
                //Check if View Type is Host Then Set Host Item
                hostTripBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_host_trip, parent, false
                )
                hostTripBinding.adapter = this
                return ViewHolderHostTrip(hostTripBinding)
            }
            else -> {
                //For Default Type set Guest Item
                guestTripBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_guest_trip, parent, false
                )
                guestTripBinding.adapter = this
                return ViewHolderGuestTrip(guestTripBinding)
            }
        }

    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        setUpUiAccordingToStatus(mArrayList[position_].status,mArrayList[position_].isAccident ?: false, holder)
    }

    /**
     * Function for setting ui for host
     * */
    private fun setUpUiForHost(holder: ViewHolderHostTrip, statusId: Int, colorId: Int) {
        val position = holder.adapterPosition
        holder.binding.position = position
        holder.binding.imageViewHost.loadImage(BuildConfig.IMAGE_BASE_URL + mArrayList[position].car_image)
        holder.binding.bookingId.text =
            context.getString(R.string.trip_id, mArrayList[position].id.toString())
        holder.binding.bookingId.setOnLongClickListener {
            val cm: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            cm?.setPrimaryClip(ClipData.newPlainText("trip id", mArrayList[position].id.toString()))
            context.showToast("Copied!")
            true
        }
        holder.binding.tvBookedBy.text =
            context.getString(R.string.booked_by) + " " + mArrayList[position].booked_by
        holder.binding.rating.text = "" + mArrayList[position].ratings
        holder.binding.trips.text =
            "(${mArrayList[position].total_trips} ${context.getString(R.string.trips)})"
        holder.binding.tvStartTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateTimeFormat,
            mArrayList[position].car_pick_up
        )
        holder.binding.tvFinishTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateTimeFormat,
            mArrayList[position].car_drop_off
        )
        holder.binding.tvBookingDate.text =
            "Booked ${
                DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    "MMM dd, yyyy",
                    mArrayList[position].createdAt
                )
            } at ${
                DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    "hh:mm a",
                    mArrayList[position].createdAt
                )
            }"
        holder.binding.rating.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"),
                Color.parseColor("#1420E4")
            )
        )
        holder.binding.tvLocation.text = mArrayList[position].booking_street_address
        holder.binding.status.text = context.getString(statusId) + ""
        holder.binding.status.setTextColor(context.getColor(colorId))
        holder.binding.executePendingBindings()
    }


    /**
     * Function for setting ui for guest
     * */
    private fun setUpUiForGuest(holder: ViewHolderGuestTrip, statusId: Int, colorId: Int) {
        val position = holder.adapterPosition
        holder.binding.position = position
        Glide.with(context)
            .load(BuildConfig.IMAGE_BASE_URL + mArrayList[position].car_image)
            .into(holder.binding.imageView)
        holder.binding.bookingId.text =
            context.getString(R.string.trip_id, mArrayList[position].id.toString())
        holder.binding.bookingId.setOnLongClickListener {
            val cm: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            cm?.setPrimaryClip(ClipData.newPlainText("trip id", mArrayList[position].id.toString()))
            context.showToast("Copied!")
            true
        }
        holder.binding.tvCarName.text =
            mArrayList[position].car_make + " " + mArrayList[position].car_model
        holder.binding.rating.text = "" + mArrayList[position].ratings
        holder.binding.trips.text =
            "(${mArrayList[position].total_trips} ${context.getString(R.string.trips)})"
        holder.binding.tvBookingDate.text =
            "Booked ${
                DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    "MMM dd, yyyy",
                    mArrayList[position].createdAt
                )
            } at ${
                DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    "hh:mm a",
                    mArrayList[position].createdAt
                )
            }"
        holder.binding.tvHostName.text = "${mArrayList[position].host_name}'s"
        holder.binding.tvStartTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateTimeFormat,
            mArrayList[position].car_pick_up
        )
        holder.binding.tvFinishTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateTimeFormat,
            mArrayList[position].car_drop_off
        )
        holder.binding.tvLocation.text = mArrayList[position].booking_street_address
        holder.binding.status.text = context.getString(statusId) + ""
        holder.binding.status.setTextColor(context.getColor(colorId))

        holder.binding.rating.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"),
                Color.parseColor("#1420E4")
            )
        )

        holder.binding.executePendingBindings()
    }


    /**
     * Function for setting up ui according to status
     * */
    private fun setUpUiAccordingToStatus(status: Int,isAccident:Boolean, holder: RecyclerView.ViewHolder) {
        var statusId = 0
        var colorId = 0
        when (status) {
            Constants.BookingStatus.IN_PROGRESS -> {
                statusId = R.string.in_progress
                colorId = R.color.borderColor
            }
            Constants.BookingStatus.FUTURE -> {
                statusId = R.string.future
                colorId = R.color.borderColor
            }
            Constants.BookingStatus.COMPLETED -> {
                statusId = R.string.completed
                colorId = R.color.borderColor
            }
            Constants.BookingStatus.PENDING -> {
                statusId = R.string.pending
                colorId = R.color.borderColor
            }
            Constants.BookingStatus.CONFLICTED -> {
                statusId = if(isAccident)
                    R.string.completed
                else
                    R.string.conflicted
                colorId = R.color.grey_text_color_new
            }
            Constants.BookingStatus.CANCELLED -> {
                statusId = R.string.cancelled
                colorId = R.color.borderColor
            }
            Constants.BookingStatus.CANCELLATION_PENDING -> {
                statusId = R.string.cancelled
                colorId = R.color.borderColor
            }
            Constants.BookingStatus.REJECTED -> {
                statusId = R.string.rejected
                colorId = R.color.borderColor
            }
            Constants.BookingStatus.EXPIRED -> {
                statusId = R.string.expired
                colorId = R.color.borderColor
            }
            else -> {
                statusId = R.string.undefined
                colorId = R.color.statusColorGrey
            }
        }

        when (holder) {
            is ViewHolderGuestTrip -> {
                setUpUiForGuest(holder, statusId, colorId)
            }
            is ViewHolderHostTrip -> {
                setUpUiForHost(holder, statusId, colorId)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mArrayList[position].viewType
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolderGuestTrip(var binding: ItemGuestTripBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ViewHolderHostTrip(var binding: ItemHostTripBinding) :
        RecyclerView.ViewHolder(binding.root)


    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        when (view.id) {
            R.id.host, R.id.guest -> {
                Log.d("hostView", "guestSelected")
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, mArrayList[position].id)
                bundle.putInt(Constants.DataParsing.VIEW_TYPE, mArrayList[position].viewType)
                uiHelper.openActivityForResult(
                    context,
                    TripDetailsActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVITY_TRIPS, bundle
                )
            }
            R.id.rating, R.id.ivRating -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, mArrayList[position].user_id)
                uiHelper.openActivityForResult(
                    context,
                    GuestReviewsListActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVITY_TRIPS, bundle
                )
            }
        }
    }
}