package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentFilterTripsBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.interfaces.ParentToChild
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.trips.FilterTripsModel
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.adapters.FilterTripsAdapter

/**
 * Fragment class for filtering trips
 * */
class FilterTripsFragment : BaseFragment(), RecyclerViewItemClick, View.OnClickListener {

    private val homeViewModel: MainScreenViewModel by activityViewModels()

    companion object {
        var filters: ArrayList<FilterTripsModel> = ArrayList()
        var callback: ParentToChild? = null
        fun newInstance(
            filters: ArrayList<FilterTripsModel>,
            callback: ParentToChild
        ): FilterTripsFragment {
            this.filters = filters
            this.callback = callback
            return FilterTripsFragment()
        }
    }

    lateinit var binding: FragmentFilterTripsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_filter_trips,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        setData()
        onBackPressed()
    }

    /**
     * Function setting up data in views
     * */
    private fun setData() {
        val adapter = FilterTripsAdapter(requireContext(), uiHelper, this, false)
        binding.filtersTripsRecyclerView.adapter = adapter
        adapter.setList(filters)
    }

    override fun onItemClick(position: Int) {
        try {
            for (i in filters.indices) {
                filters[i].selected = false
            }
            filters[position].selected = true
            (binding.filtersTripsRecyclerView.adapter as FilterTripsAdapter).setList(filters)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Function for back to parent fragment
     * */
    fun onBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        if (callback != null) {
                            callback?.sendCallbackFromPTC()
                            if (isEnabled) {
                                isEnabled = false
                                requireActivity().onBackPressed()
                            }
                            callback = null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            )
    }

    override fun onClick(p0: View?) {
        p0?.preventDoubleClick()
        when (p0) {
            binding.btnDone -> {
                homeViewModel.setFilters(filters)
                requireActivity().onBackPressed()
            }
        }
    }
}