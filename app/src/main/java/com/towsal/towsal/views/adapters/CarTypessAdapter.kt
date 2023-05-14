package com.towsal.towsal.views.adapters

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.CarTypesItemLayoutBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.home.CarHomeModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.GPSTracker
import com.towsal.towsal.views.activities.SearchCarActivity

/**
 * Adapter class for car types
 * */
class CarTypessAdapter(
    private val photosList: ArrayList<CarHomeModel>,
    var context: Activity,
    val uiHelper: UIHelper,
    val gpsTracker: GPSTracker?
) : BaseAdapter() {

    class ViewHolder(val itemLayoutBinding: CarTypesItemLayoutBinding) :
        RecyclerView.ViewHolder(itemLayoutBinding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CarTypesItemLayoutBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.car_types_item_layout, parent, false)
        )
        binding.adapter = this
        return ViewHolder(binding)
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.image -> {

                val bundle = Bundle()
                var lat = 0.0
                var lng = 0.0
                var cityId =
                    PreferenceHelper().getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
                if (uiHelper.isPermissionAllowed(context, PERMISSION)) {
                    if (!gpsTracker!!.checkGPsEnabled()) {
                        uiHelper.showEnableLocationSetting(context)
                        return
                    }
                    lat = gpsTracker.getLatitude()
                    lng = gpsTracker.getLongitude()
                    cityId = 0
                }
                bundle.putDouble(Constants.LAT, lat)
                bundle.putDouble(Constants.LNG, lng)
                bundle.putInt(Constants.CITY_ID, cityId)
                var id = photosList[position].id
                Log.d("dd",id.toString())
                bundle.putInt(Constants.CAR_TYPE_ID, photosList[position].id)
                bundle.putString(Constants.ADDRESS, context.getString(R.string.current_location))
                uiHelper.openActivity(context, SearchCarActivity::class.java, true, bundle)

            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewholder: ViewHolder = holder as ViewHolder
        viewholder.itemLayoutBinding.photo = photosList[position]
        viewholder.itemLayoutBinding.position = viewholder.adapterPosition
        uiHelper.glideLoadImage(
            context,
            photosList[position].url,
            viewholder.itemLayoutBinding.image
        )
    }

    override fun getItemViewType(position: Int) = position


    override fun getItemCount() = photosList.size


    override fun getItemId(position: Int) = position.toLong()
}