package com.towsal.towsal.views.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.towsal.towsal.R
import com.towsal.towsal.network.serializer.profile.TransactionResponseModel
import com.towsal.towsal.views.fragments.TransactionHistoryFragment
import com.towsal.towsal.views.fragments.TransactionHistoryGuestFragment

/**
 * Adapter class for transaction history pager
 * */
class TransactionHistoryAdapter(
    fm: FragmentManager,
    val context: Context,
    val isHost: Boolean,
    val responseModel: TransactionResponseModel
) :
    FragmentStatePagerAdapter(fm) {

    override fun getCount() =
        if (isHost) {
            2
        } else {
            1
        }


    override fun getItem(i: Int) =
        if (isHost) {
            when (i) {
                0 -> {
                    TransactionHistoryGuestFragment()
                }
                1 -> {
                    TransactionHistoryFragment()
                }
                else -> {
                    TransactionHistoryGuestFragment()
                }
            }
        } else {
            when (i) {
                0 -> {
                    TransactionHistoryGuestFragment()
                }
                else -> {
                    TransactionHistoryGuestFragment()
                }
            }
        }


    override fun getPageTitle(position: Int) =
        if (isHost) {
            when (position) {
                0 -> {
                    context.getString(R.string.guest)
                }
                1 -> {
                    context.getString(R.string.host)
                }
                else -> {
                    context.getString(R.string.host)
                }
            }
        } else {
            when (position) {
                0 -> {
                    context.getString(R.string.guest)
                }

                else -> {
                    context.getString(R.string.guest)
                }
            }
        }


}