package com.towsal.towsal.views.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.towsal.towsal.network.serializer.settings.VehicleInfoSettingModel
import com.towsal.towsal.views.fragments.EarningSettingFragment
import com.towsal.towsal.views.fragments.MyVehicleListingFragment
import com.towsal.towsal.views.fragments.PerformanceSettingFragment
import com.towsal.towsal.views.fragments.ReviewSettingFragment

/**
 * Adapter class for host screen view pager
 * */
class HostSettingsPagerAdapter(fm: FragmentManager, val context: FragmentActivity, val callBack: (Int, Int) -> Unit) :
    FragmentStateAdapter(context) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return MyVehicleListingFragment()
            }
            1 -> {
                return EarningSettingFragment()
            }
            2 -> {
                return PerformanceSettingFragment()
            }

            3 -> {
                return ReviewSettingFragment()
            }
            else -> {
                return MyVehicleListingFragment()
            }
        }
    }
}