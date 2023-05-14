package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCarDocsBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel

class CarDocumentsAdapter(
    val context: Context,
    val callBack: (Int, Int) -> Unit
) : BaseAdapter() {
    var mArrayList = ArrayList<CarPhotoListModel>()

    companion object {
        const val UPDATE = 1
        const val VIEW = 0
    }

    fun setList(mArrayList: ArrayList<CarPhotoListModel>) {
        this.mArrayList = mArrayList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemCarDocsBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.item_car_docs, parent, false)
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        viewHolder.binding.apply {
            tvStatus.text = getStatus(
                mArrayList[position].status
            )

            tvImageTitle.text = mArrayList[position].name
            ivIcon.loadImage(
                mArrayList[position].iconUrl
            )
            tvUpdate.setOnClickListener {
                callBack(UPDATE, position)
            }
            tvView.setOnClickListener {
                callBack(VIEW, position)
            }
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
                "Rejected"
            }
            else -> {
                ""
            }
        }


    override fun getItemCount() = mArrayList.size


    class ViewHolder(var binding: ItemCarDocsBinding) :
        RecyclerView.ViewHolder(binding.root)

}