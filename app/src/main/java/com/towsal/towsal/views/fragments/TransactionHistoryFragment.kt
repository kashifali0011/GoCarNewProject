package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentTransactionHistoryBinding
import com.towsal.towsal.network.serializer.trips.TripsModel
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.adapters.TransactionHistoryListAdapter

/**
 * Fragment class for transaction history
 * */
class TransactionHistoryFragment(
) : BaseFragment() {

    lateinit var binding: FragmentTransactionHistoryBinding
    lateinit var adapter: TransactionHistoryListAdapter
    val homeViewModel: MainScreenViewModel by activityViewModels()
    private var hostList: ArrayList<TripsModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_transaction_history,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        homeViewModel.transactionModel.observe(viewLifecycleOwner) {
            hostList = it.hostList
            setData()
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        adapter = TransactionHistoryListAdapter(requireActivity(), uiHelper, true)
        binding.recyclerview.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.recyclerview.adapter = adapter
        adapter.setList(hostList)
        binding.noDataLayout.isVisible = hostList.isEmpty()
        binding.executePendingBindings()
    }

}