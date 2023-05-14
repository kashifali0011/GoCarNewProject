package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.towsal.towsal.R
import com.towsal.towsal.databinding.AdapterInfoWindowMapBinding
import com.towsal.towsal.helper.uiHelperModule
import com.towsal.towsal.network.serializer.home.SearchCarDetailModel


class MapMarkerInfoAdapter(
    val context: Context,
) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {
        val binding = AdapterInfoWindowMapBinding.bind(
            LayoutInflater.from(context).inflate(
                R.layout.adapter_info_window_map,
                null
            )
        )
        renderWindow(binding, p0)
        return binding.root
    }

    override fun getInfoWindow(p0: Marker): View {

        val binding = AdapterInfoWindowMapBinding.bind(
            LayoutInflater.from(context).inflate(
                R.layout.adapter_info_window_map,
                null
            )
        )
        renderWindow(binding, p0)
        return binding.root
    }

    private fun renderWindow(binding: AdapterInfoWindowMapBinding, p0:Marker) {
//        binding.tvCarModel.text =  searchCarDetailModel.model

    }
}