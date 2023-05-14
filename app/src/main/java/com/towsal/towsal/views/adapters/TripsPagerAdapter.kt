package com.towsal.towsal.views.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.towsal.towsal.views.fragments.BookedTripsFragment
import com.towsal.towsal.views.fragments.HistoryTripsFragment
import com.towsal.towsal.views.fragments.NotificationsFragment

/**
 * Adapter class for trips view pager
 * */
class TripsPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {


    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                NotificationsFragment()
            }
            1 -> {
                BookedTripsFragment()
            }
            2 -> {
                HistoryTripsFragment()
            }
            else -> {
                NotificationsFragment()
            }
        }
    }


}