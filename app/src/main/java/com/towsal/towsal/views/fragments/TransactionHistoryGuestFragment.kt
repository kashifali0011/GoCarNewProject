package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentTransactionHistoryGuestBinding
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.trips.TripsModel
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.adapters.TransactionHistoryListAdapter

/**
 * Fragment class for transaction history details
 * */
class TransactionHistoryGuestFragment :
    BaseFragment(), RecyclerViewItemClick {
    lateinit var binding: FragmentTransactionHistoryGuestBinding
    lateinit var adapter: TransactionHistoryListAdapter
    val homeViewModel: MainScreenViewModel by activityViewModels()
    var guestList: ArrayList<TripsModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_transaction_history_guest,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        homeViewModel.transactionModel.observe(viewLifecycleOwner) {
            guestList = it.guestList
            binding.noDataLayout.isVisible = guestList.isEmpty()
            setData()
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        adapter = TransactionHistoryListAdapter(
            requireActivity(),
            uiHelper,
            false
        )
        binding.recyclerview.adapter = adapter
        adapter.setList(guestList)
    }


}