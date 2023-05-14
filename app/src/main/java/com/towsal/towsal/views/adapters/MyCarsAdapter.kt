package com.towsal.towsal.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemMyCarBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.settings.VehicleInfoSettingModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil

class MyCarsAdapter(
    private var myCarsList: MutableList<VehicleInfoSettingModel>,
    private val uiHelper: UIHelper,
    private val callBack: (Int, Int, Int, Int) -> Unit,
) : BaseAdapter() {
    companion object {
        const val DELETE_FLOW = 1
        const val OPEN_DETAILS_FLOW = 2
        const val STATUS_CHANGE_FLOW = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(
            ItemMyCarBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.item_my_car, parent, false)
            )
        )

    fun setList(
        list: List<VehicleInfoSettingModel>
    ) {
        myCarsList = list.toMutableList()
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder

        viewHolder.binding.apply {
            ivCarImage.loadImage(
                BuildConfig.IMAGE_BASE_URL + myCarsList[position].images
            )

            if (myCarsList[position].rating == "0.0"){
                tvRating.isVisible = false
                tvNoRating.isVisible = true

            }else{
                tvRating.isVisible = true
                tvNoRating.isVisible = false
                tvRating.text = myCarsList[position].rating
            }

            tvLicenseNumber.text = myCarsList[position].plateNumber
            tvLastTrip.text =
                if (myCarsList[position].pickUp.isNullOrEmpty() && myCarsList[position].dropOff.isNullOrEmpty()) "No trips" else "Last trip: ${
                    DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.MonthDayFormat,
                        myCarsList[position].pickUp
                    )
                } - ${
                    DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.MonthDayFormat,
                        myCarsList[position].dropOff
                    )
                }"
            tvCarName.text = "${myCarsList[position].make} - ${myCarsList[position].model}"
            active.isChecked = myCarsList[position].status == Constants.CarStatus.LISTED
            snoozed.isChecked = myCarsList[position].status == Constants.CarStatus.SNOOZED

            if (myCarsList[position].status == Constants.CarStatus.SNOOZED) {
                clMainLayout.alpha = 0.5f
            } else {
                clMainLayout.alpha = 1f
            }
            clMainLayout.alpha = if (myCarsList[position].status == Constants.CarStatus.SNOOZED) 0.5f else 1f

            active.setOnClickListener {
                Log.e("onBindViewHolder: ", "${myCarsList[position].status}")
                if (myCarsList[position].status != Constants.CarStatus.LISTED) {
                    snoozed.isChecked = myCarsList[position].status == Constants.CarStatus.SNOOZED
                    callBack(
                        Constants.CarStatus.LISTED,
                        myCarsList[position].id,
                        position,
                        STATUS_CHANGE_FLOW
                    )
                }
            }

            snoozed.setOnClickListener {
                if (myCarsList[position].status != Constants.CarStatus.SNOOZED) {
                    active.isChecked = myCarsList[position].status == Constants.CarStatus.LISTED
                    callBack(
                        Constants.CarStatus.SNOOZED,
                        myCarsList[position].id,
                        position,
                        STATUS_CHANGE_FLOW
                    )
                }
            }

            ivDeleteCar.setOnClickListener {
                callBack(Constants.CarStatus.LISTED, myCarsList[position].id, position, DELETE_FLOW)
            }
        }

        viewHolder.itemView.setOnClickListener {
            callBack(
                Constants.CarStatus.LISTED,
                myCarsList[position].id,
                position,
                OPEN_DETAILS_FLOW
            )
        }
        viewHolder.binding.view.isVisible = myCarsList.size - 1 == position
    }

    override fun getItemCount(): Int = myCarsList.size

    class ViewHolder(val binding: ItemMyCarBinding) :
        RecyclerView.ViewHolder(binding.root)

}