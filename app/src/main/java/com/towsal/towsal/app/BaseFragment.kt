package com.towsal.towsal.app

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.anggrayudi.storage.SimpleStorageHelper
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.utils.GPSTracker
import org.koin.android.ext.android.inject

/**
 * Every fragment should extend from base fragment which contains some generic functions like action bar setting
 */
open class BaseFragment : Fragment() {
    val preferenceHelper: PreferenceHelper by inject()
    val uiHelper: UIHelper by inject()
    var setActionBar: SetActionBar? = null
    var gpsTracker: GPSTracker? = null
    val storageHelper: SimpleStorageHelper by lazy {
        SimpleStorageHelper(requireActivity())
    }

    val PERMISSION =
        Manifest.permission.ACCESS_FINE_LOCATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsTracker = GPSTracker(requireActivity())
    }

    var smoothScroller: RecyclerView.SmoothScroller =
        object : LinearSmoothScroller(MainApplication.applicationContext()) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
}