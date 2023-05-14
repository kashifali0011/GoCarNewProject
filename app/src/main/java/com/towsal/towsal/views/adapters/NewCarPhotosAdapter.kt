package com.towsal.towsal.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.NewCarPhotosItemBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import java.util.*
import kotlin.collections.ArrayList

class NewCarPhotosAdapter(val context: Context, private val showStatus: Boolean = true, private val callBack: (index: Int) -> Unit = {} ) : BaseAdapter() {

    private var mArrayList = ArrayList<CarPhotoListModel>()

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<CarPhotoListModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    /**
     *  Functions for getting attached list
     * */
    fun getList() = mArrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = NewCarPhotosItemBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.new_car_photos_item, parent, false)
        )

        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder

        viewHolder.binding.tvStatus.isInvisible = mArrayList[position].status == -1
        viewHolder.binding.tvStatus.text = getStatus(
            mArrayList[position].status
        )
        viewHolder.binding.carImage.loadImage(
            BuildConfig.IMAGE_BASE_URL + mArrayList[position].url
        )
        viewHolder.binding.tvStatus.isInvisible = !showStatus

        viewHolder.binding.ivRemoveImage.setOnClickListener {
            mArrayList[position].url = ""
            notifyItemChanged(position)
            callBack(position)
        }
    }

    /**
     *  Functions for getting status
     * */
    private fun getStatus(status: Int) =
        when (status) {
            0 -> {
                "Pending"
            }
            1 -> {
                "Accepted"
            }
            2 -> {
                "Upload Again"
            }
            else -> {
                ""
            }
        }


    override fun getItemCount() = mArrayList.size
    fun moveItem(from: Int, to: Int) {
        val list = ArrayList(mArrayList.toMutableList())
        Collections.swap(list, from, to)
        var i = -1
        list.forEach {
            i += 1
            it.sequence = i
        }
        mArrayList = list
        notifyItemMoved(from, to)
        Log.e("moveItem: ", "$to")
        Log.e("moveItem: ", Gson().toJson(list))
    }

    class ViewHolder(var binding: NewCarPhotosItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}