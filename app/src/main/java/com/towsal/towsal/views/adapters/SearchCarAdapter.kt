package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemSearchCarListBinding
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.home.SearchCarDetailModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarDetailViewModel

/**
 * Adapter class for search car
 * */
class SearchCarAdapter(
    var context: Context,
    val uiHelper: UIHelper,
    var mArrayList: ArrayList<SearchCarDetailModel>,
    val callBack: RecyclerViewItemClick
) : BaseAdapter(), OnNetworkResponse {
    lateinit var carDetailViewModel: CarDetailViewModel
    var position = 0
    var removeItem = false
    var mListner: EmptyItem? = null


    constructor(
        context: Context,
        uiHelper: UIHelper,
        mArrayList: ArrayList<SearchCarDetailModel>,
        callBack: RecyclerViewItemClick,
        carDetailViewModel: CarDetailViewModel
    ) : this(context, uiHelper, mArrayList, callBack) {
        this.carDetailViewModel = carDetailViewModel
        this.mArrayList = mArrayList
    }

    constructor(
        context: Context,
        uiHelper: UIHelper,
        mArrayList: ArrayList<SearchCarDetailModel>,
        callBack: RecyclerViewItemClick,
        carDetailViewModel: CarDetailViewModel,
        removeItem: Boolean,
        mlistener: EmptyItem
    ) : this(context, uiHelper, mArrayList, callBack) {
        this.carDetailViewModel = carDetailViewModel
        this.removeItem = removeItem
        this.mListner = mlistener

    }

    lateinit var binding: ItemSearchCarListBinding

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<SearchCarDetailModel>) {
        mArrayList.clear()
        mArrayList.addAll(arrayList)
//        mArrayList = arrayList
        Log.e(SearchCarAdapter::javaClass.name, arrayList.size.toString())
        notifyDataSetChanged()
    }

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<SearchCarDetailModel> {
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
            R.layout.item_search_car_list, parent, false
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
            R.id.cardDetails -> {
                callBack.onItemClick(position)
            }
            R.id.favourite -> {
                Log.d("favFlagaaaa", "carId" + mArrayList[position].id)
                Log.d("", "")
                this.position = position
                if (uiHelper.userLoggedIn(PreferenceHelper())) {
                    if (mArrayList[position].fav_flag == 1) {
                        carDetailViewModel.deleteFavorite(
                            mArrayList[position].id,
                            true,
                            Constants.API.DELETE_FAV_CAR,
                            this
                        )
                    } else {
                        carDetailViewModel.addFavorite(
                            mArrayList[position].id,
                            true,
                            Constants.API.ADD_FAV_CAR,
                            this
                        )
                    }
                } else {
                    uiHelper.showLongToastInCenter(
                        context,
                        context.getString(R.string.log_in_to_add_fav_car_err_msg)
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        Log.e(SearchCarAdapter::javaClass.name, mArrayList[position_].fav_flag.toString())
        val viewHolder: ViewHolder = holder as ViewHolder
        val position = viewHolder.adapterPosition
        viewHolder.binding.position = position
        viewHolder.binding.carName.text =
            mArrayList[position].name + " " + mArrayList[position].model

        if (mArrayList[position].rating == 0.0){
            viewHolder.binding.rating.visibility = View.INVISIBLE
        }else{
            viewHolder.binding.rating.visibility = View.VISIBLE
        }

        viewHolder.binding.rating.rating = mArrayList[position].rating.toFloat()
        viewHolder.binding.cardDetails.setOnClickListener {
            onClick(position, it)
        }
        viewHolder.binding.favourite.setOnClickListener {
            onClick(position, it)
        }
        viewHolder.binding.price.text =
            "" + mArrayList[position].price
        viewHolder.binding.distance.text = "" + mArrayList[position].distance
        if (mArrayList[position].fav_flag == 1) {
            binding.favourite.setImageResource(R.drawable.ic_new_favourite)
        } else if (mArrayList[position].fav_flag == 0) {
            binding.favourite.setImageResource(R.drawable.ic_new_un_favorite)
        }
        uiHelper.glideLoadImage(
            context,
            BuildConfig.IMAGE_BASE_URL + mArrayList[position].url,
            viewHolder.binding.image
        )
        if (mArrayList[position].delivery_status == 0) {
            binding.pickupOnly.text = context.getString(R.string.pickup_only)
        } else {
            binding.pickupOnly.text = context.getString(R.string.available_for_delivery)
        }
        viewHolder.binding.executePendingBindings()

    }

    class ViewHolder(var binding: ItemSearchCarListBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     *  Functions for handling api's success response
     * */
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(context, response?.message)
        when (tag) {
            Constants.API.DELETE_FAV_CAR -> {
                mArrayList[position].fav_flag = 0
            }
            Constants.API.ADD_FAV_CAR -> {
                mArrayList[position].fav_flag = 1
            }
        }
        notifyItemChanged(position)
        if (removeItem) {
            Log.d("favoriteDelete", "favoriteDeleted")
            mArrayList.remove(mArrayList[position])
            notifyDataSetChanged()

            /*......favorite no data found visibility .......*/
            if (mArrayList.isEmpty()) {
                Log.d("favoriteDelete", "favoriteDeletedListEmpty")
                if (mListner != null)
                    mListner!!.onEmptyList()
                /*              context.startActivity(Intent(context,FavoriteListActivity::class.java))
                              (context as Activity).finish()*/
            }
        }
    }

    /**
     *  Functions for handling api's failure response
     * */
    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(context, response?.message)
    }


    interface EmptyItem {
        fun onEmptyList()
    }
}