package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCarTypesBinding
import com.towsal.towsal.databinding.ItemSearchCarListBinding
import com.towsal.towsal.databinding.ItemTopCarsBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.home.CarHomeModel
import com.towsal.towsal.views.listeners.OnEvent

/**
 * Adapter class for cars around you
 * */
class CarTypesAdapter(
    private val photosList: ArrayList<CarHomeModel>,
    var context: Activity,
    val uiHelper: UIHelper,
    val listener: OnEvent,
    val customViewType: Int = 0,
) : BaseAdapter() {
    var position = -1

    class ViewHolder(val binding: ItemCarTypesBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ViewHolder2(val binding: ItemTopCarsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (customViewType == 0) {
            ViewHolder(
                ItemCarTypesBinding.bind(
                    LayoutInflater.from(context).inflate(R.layout.item_car_types, parent, false)
                )
            )
        } else {
            ViewHolder2(
                ItemTopCarsBinding.bind(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_top_cars, parent, false)
                )
            )
        }


    override fun getItemCount() = photosList.size


    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.cardDetails, R.id.image -> {
                listener.openDetailsActivity(photosList[position].id, position)
            }

            R.id.favourite -> {
                this.position = position
                if (uiHelper.userLoggedIn(PreferenceHelper())) {
                    listener.markFavourite(photosList[position].id, position)
                } else {
                    uiHelper.showLongToastInCenter(
                        context,
                        context.getString(R.string.log_in_to_add_fav_car_err_msg)
                    )
                }
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                setCarTypesData(holder, position)
            }
            is ViewHolder2 -> {
                setCarsData(holder, position)
            }
        }

    }

    private fun setCarsData(viewHolder: ViewHolder2, position: Int) {
        viewHolder.binding.position = position
        viewHolder.binding.carName.text =
            photosList[position].carDetailInfo.name + " " + photosList[position].carDetailInfo.model
        if (photosList[position].carDetailInfo.rating == 0.0){
            viewHolder.binding.rating.visibility = View.INVISIBLE
        }else{
            viewHolder.binding.rating.visibility = View.VISIBLE
        }
        viewHolder.binding.rating.rating = photosList[position].carDetailInfo.rating.toFloat()
        viewHolder.binding.price.text =
            "" + photosList[position].carDetailInfo.price
        viewHolder.binding.distance.text = "" + photosList[position].carDetailInfo.distance
        if (photosList[position].carDetailInfo.isFavourite == 1) {
            viewHolder.binding.favourite.setImageResource(R.drawable.ic_new_favourite)
        } else if (photosList[position].carDetailInfo.isFavourite == 0) {
            viewHolder.binding.favourite.setImageResource(R.drawable.ic_new_un_favorite)
        }
        uiHelper.glideLoadImage(
            context,
            BuildConfig.IMAGE_BASE_URL + photosList[position].url,
            viewHolder.binding.image
        )
        if (photosList[position].carDetailInfo.delivery_status == 0) {
            viewHolder.binding.pickupOnly.text = context.getString(R.string.pickup_only)
        } else {
            viewHolder.binding.pickupOnly.text = context.getString(R.string.available_for_delivery)
        }
        viewHolder.binding.cardDetails.setOnClickListener {
            onClick(position, it)
        }
        viewHolder.binding.favourite.setOnClickListener {
            onClick(position, it)
        }
        viewHolder.binding.executePendingBindings()
    }

    private fun setCarTypesData(viewHolder: ViewHolder, position: Int) {
        viewHolder.binding.photo = photosList[position]
        viewHolder.binding.position = viewHolder.adapterPosition
        uiHelper.glideLoadImage(
            context,
            BuildConfig.IMAGE_BASE_URL + photosList[position].url,
            viewHolder.binding.image
        )
        viewHolder.binding.constraintLayout.visibility = View.VISIBLE
        setCarInfoData(viewHolder.adapterPosition, photosList, viewHolder)
        if (photosList[position].carDetailInfo.delivery_status == 0) {
            viewHolder.binding.pickupOnly.text = context.getString(R.string.pickup_only)
        } else {
            viewHolder.binding.pickupOnly.text = context.getString(R.string.available_for_delivery)
        }
        viewHolder.binding.image.setOnClickListener {
            onClick(position, it)
        }
        viewHolder.binding.favourite.setOnClickListener {
            onClick(position, it)
        }
        viewHolder.binding.pickupOnly.visibility = View.VISIBLE
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     *  Functions for setting views data
     * */
    @SuppressLint("SetTextI18n")
    fun setCarInfoData(
        position: Int,
        carAroundYou: ArrayList<CarHomeModel>,
        viewHolder: ViewHolder
    ) {
        viewHolder.binding.carName.text =
            carAroundYou[position].carDetailInfo.name + " " + carAroundYou[position].carDetailInfo.model

        if (carAroundYou[position].carDetailInfo.rating == 0.0){
            viewHolder.binding.rating.visibility = View.INVISIBLE
        }else{
            viewHolder.binding.rating.visibility = View.VISIBLE
        }

        viewHolder.binding.rating.rating = carAroundYou[position].carDetailInfo.rating.toFloat()
        viewHolder.binding.price.text = "" + carAroundYou[position].carDetailInfo.price
        viewHolder.binding.distance.text =
            carAroundYou[position].carDetailInfo.distance + " " + context.getString(R.string.km)

        if (carAroundYou[position].carDetailInfo.isFavourite == 1) {
            viewHolder.binding.favourite.setImageResource(R.drawable.ic_new_favourite)
        } else {
            viewHolder.binding.favourite.setImageResource(R.drawable.ic_new_un_favorite)
        }
    }

}