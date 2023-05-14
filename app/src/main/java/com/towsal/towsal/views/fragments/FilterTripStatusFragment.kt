package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentFilterTripStatusBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.interfaces.FilterStatusCallback
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.trips.FilterTripsModel
import com.towsal.towsal.views.adapters.FilterTripsAdapter

/**
 * Fragment class for filtering trips according to status
 * */
class FilterTripStatusFragment : BaseFragment(), RecyclerViewItemClick, View.OnClickListener {

    companion object {
        var filtersCopy: ArrayList<FilterTripsModel> = ArrayList()
        var callback: FilterStatusCallback? = null
        fun newInstance(
            filters: ArrayList<FilterTripsModel>,
            callback: FilterStatusCallback
        ): FilterTripStatusFragment {
            filtersCopy = filters
            this.callback = callback
            return FilterTripStatusFragment()
        }
    }

    lateinit var binding: FragmentFilterTripStatusBinding
    lateinit var adapter: FilterTripsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_filter_trip_status,
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
     * Function for setting up data in views
     * */
    private fun setData() {
        adapter = FilterTripsAdapter(requireActivity(), uiHelper, this, true)
        binding.filtersTripsStatusRecyclerView.apply {
            adapter = this@FilterTripStatusFragment.adapter
        }
        adapter.setList(filtersCopy)
    }

    override fun onItemClick(position: Int) {
        val previousPosition = filtersCopy.indexOfFirst {
            it.selected
        }
        filtersCopy[previousPosition].selected = false
        filtersCopy[position].selected = true
        adapter.setList(filtersCopy)
    }

    /**
     * Function for setting up back press
     * */
    fun onBackPressed() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (callback != null) {
                        if (isEnabled) {
                            isEnabled = false
                            try {
                                if (parentFragmentManager.fragments.isNotEmpty())
                                    parentFragmentManager.popBackStack()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                        callback = null
                    }

                }
            }
            )
    }

    override fun onClick(p0: View?) {
        p0?.preventDoubleClick()
        when (p0) {
            binding.btnDone -> {
                if (callback != null)
                    callback?.statusSelected(filtersCopy)
                requireActivity().onBackPressed()
            }
        }
    }


}