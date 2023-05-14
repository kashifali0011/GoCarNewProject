package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemNotificationBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.trips.NotificationModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.views.activities.*
import com.towsal.towsal.views.fragments.HostFragmentDirections
import com.towsal.towsal.views.fragments.TripsFragmentDirections

/**
 * Adapter class for notifications
 * */
class NotificationAdapter(
    var context: Activity,
    val uiHelper: UIHelper,
    val supportFragmentManager: FragmentManager
) : BaseAdapter() {
    var notificationList: List<NotificationModel> = ArrayList()
    fun notifyItems(notificationList: ArrayList<NotificationModel>) {
        this.notificationList = notificationList
        notifyDataSetChanged()
    }

    companion object {
        const val UPLOAD_PICKUP_PHOTOS = 1
        const val PICKUP_PHOTOS_ACCEPTED = 2
        const val PICKUP_PHOTOS_REJECTED = 3
        const val UPLOAD_DROP_OFF_PHOTOS = 4
        const val DROP_OFF_PHOTOS_ACCEPTED = 5
        const val DROP_OFF_PHOTOS_REJECTED = 6
        const val UPLOAD_ACCIDENT_PHOTOS = 7
        const val BOOKING_CREATED = 8
        const val SEND_MESSAGE = 9
        const val ACCEPT_BOOKING = 10
        const val REJECT_BOOKING = 11
        const val CANCEL_BOOKING = 13
        const val CHANGE_REQUEST = 14
        const val EXTENSION_REQUEST = 17
        const val CAR_DOCS = 18
        const val USER_DOCS = 35
        const val CAR_TYPE = 32

        const val CAR_PHOTOS = 19
        const val PROMO = 20
        const val IMAGE_REMOVAL = 21
        const val VEHICLE_PROTECTION = 27
    }

    class ViewHolder(val itemLayoutBinding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(itemLayoutBinding.root)

    lateinit var binding: ItemNotificationBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.item_notification, parent, false
        )
        binding.adapter = this
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.cardDetail -> {
                setClickLink(notificationList, position, context)
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.itemLayoutBinding.position = viewHolder.adapterPosition
        if (notificationList[position].not_type == PROMO && notificationList[position].profile_image != null) {
            uiHelper.glideLoadImage(
                context,
                BuildConfig.IMAGE_BASE_URL + notificationList[position].profile_image,
                viewHolder.itemLayoutBinding.image
            )
        } else {
            uiHelper.glideLoadImage(
                context,
                BuildConfig.IMAGE_BASE_URL + notificationList[position].sender_profile_image,
                viewHolder.itemLayoutBinding.image
            )
        }
        viewHolder.itemLayoutBinding.model = notificationList[viewHolder.adapterPosition]
        binding.dateTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateTimeWithYearFormat,
            notificationList[position].time
        )
        binding.notificationDetail.text =
            notificationList[viewHolder.itemLayoutBinding.position].notification_detail


        /*  //-------------------For Read More and Read Less TextView Functionality
          try {
              var readmore = ""
              var readless = ""
              var length = 0
              if (notificationList[viewHolder.itemLayoutBinding.position].notification_detail.length > 100) {
                  readmore = context.getString(R.string.read_more)
                  readless = context.getString(R.string.read_less)
                  length =
                      (notificationList[viewHolder.itemLayoutBinding.position].notification_detail.length / 2.5).toInt()
              } else {
                  readmore = ""
                  readless = ""
                  length =
                      notificationList[viewHolder.itemLayoutBinding.position].notification_detail.length
              }
              val readMoreOption: ReadMoreOption = ReadMoreOption.Builder(context)
  //                .textLength(3, ReadMoreOption.TYPE_LINE) // OR
                  .textLength(length, ReadMoreOption.TYPE_CHARACTER)
                  .moreLabel(readmore)
                  .lessLabel(readless)
                  .moreLabelColor(context.getColor(R.color.btn_color))
                  .lessLabelColor(context.getColor(R.color.btn_color))
                  .build()
              readMoreOption.addReadMoreTo(
                  binding.notificationDetail,
                  notificationList[viewHolder.itemLayoutBinding.position].notification_detail
              )
          } catch (e: Exception) {
              e.printStackTrace()
          }*/

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun setClickLink(
        notificationList: List<NotificationModel>,
        position: Int,
        context: Activity
    ) {
        if (notificationList[position].not_type in (UPLOAD_PICKUP_PHOTOS..UPLOAD_ACCIDENT_PHOTOS) || notificationList[position].not_type in listOf(
                BOOKING_CREATED,
                ACCEPT_BOOKING,
                REJECT_BOOKING,
                CANCEL_BOOKING,
                CHANGE_REQUEST,
                EXTENSION_REQUEST
            )
        ) {
            val viewType = notificationList[position].view_type
            val bundle = Bundle()
            bundle.putInt(
                Constants.DataParsing.VIEW_TYPE,
                viewType
            )
            bundle.putInt(
                Constants.DataParsing.ID,
                notificationList[position].notification_type_id
            )
            uiHelper.openActivityForResult(
                context,
                TripDetailsActivity::class.java,
                true,
                Constants.RequestCodes.ACTIVITY_TRIPS, bundle
            )

        } else if (notificationList[position].not_type == SEND_MESSAGE) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.bnv_message)
        } else if (notificationList[position].not_type in listOf(
                CAR_DOCS,
                USER_DOCS
            ) && notificationList[position].car_id != 0
        ) {
            val bundle = Bundle()
            bundle.putInt(Constants.DataParsing.CAR_ID, notificationList[position].car_id)
            uiHelper.openActivity(
                context,
                ActivityCarDocuments::class.java,
                bundle
            )
        } else if (notificationList[position].not_type == CAR_PHOTOS && notificationList[position].car_id != 0) {
            val bundle = Bundle()
            bundle.putInt(Constants.DataParsing.CAR_ID, notificationList[position].car_id)
            uiHelper.openActivity(
                context,
                CarPhotosActivity::class.java,
                true, bundle
            )
        } else if (notificationList[position].not_type == IMAGE_REMOVAL) {
            context.findNavController(R.id.navHostFragment).navigate(R.id.bnv_profile)
        } else if (notificationList[position].not_type == VEHICLE_PROTECTION) {
            uiHelper.openActivity(context, VehicleProtectionActivity::class.java, Bundle().apply {
                this.putInt(Constants.DataParsing.CAR_ID, notificationList[position].car_id)
            })
        }
        else if (notificationList[position].not_type == CAR_TYPE){
            val direction = TripsFragmentDirections.actionBnvTripsToVehicleSettingFragment()
            direction.carId = notificationList[position].car_id
            context.findNavController(R.id.navHostFragment).navigate(direction)
        }
    }
}