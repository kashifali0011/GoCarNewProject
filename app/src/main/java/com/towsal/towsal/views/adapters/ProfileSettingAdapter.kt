package com.towsal.towsal.views.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemProfileSettingBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.ProfileRVItemClick
import com.towsal.towsal.network.serializer.profile.GetProfileResponseModel
import com.towsal.towsal.network.serializer.settings.ProfileModel
import java.util.*

/**
 * Adapter class for profile settings
 * */
class ProfileSettingAdapter(
    private val settingList: List<ProfileModel>,
    var context: Activity,
    val uiHelper: UIHelper,
    private val profileRVItemClick: ProfileRVItemClick
) : BaseAdapter() {
    var model: GetProfileResponseModel = GetProfileResponseModel()

    class ViewHolder2(val itemLayoutBinding: ItemProfileSettingBinding) :
        RecyclerView.ViewHolder(itemLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemProfileSettingBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.item_profile_setting, parent, false)
        )

        binding.adapter = this
        return ViewHolder2(binding)

    }

    override fun getItemCount(): Int {
        return settingList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.viewClick -> {
                profileRVItemClick.onItemClick(position)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val viewHolder: ViewHolder2 = holder as ViewHolder2
        viewHolder.itemLayoutBinding.position = viewHolder.adapterPosition
        viewHolder.itemLayoutBinding.imageView.setImageResource(settingList[viewHolder.itemLayoutBinding.position].image)
        viewHolder.itemLayoutBinding.name.text =
            if (position != settingList.size - 2) {
                settingList[viewHolder.itemLayoutBinding.position].name.split(' ')
                    .joinToString(" ") {
                        it.replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase(
                                Locale.getDefault()
                            ) else char.toString()
                        }
                    }
            }
            else {
                settingList[viewHolder.itemLayoutBinding.position].name
            }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setProfileData(model: GetProfileResponseModel) {
        this.model = model
        notifyDataSetChanged()
    }

}