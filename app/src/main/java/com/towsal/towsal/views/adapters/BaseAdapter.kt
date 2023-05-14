package com.towsal.towsal.views.adapters

import android.Manifest
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse

/**
 * Class which will be extended by all adapter classes used in the application
 * */
abstract class BaseAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), OnNetworkResponse {
    val PERMISSION =
        Manifest.permission.ACCESS_FINE_LOCATION

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
    }
}