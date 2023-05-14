package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentHistoryBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.interfaces.FilterStatusCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.trips.BookingsTripsResponseModel
import com.towsal.towsal.network.serializer.trips.FilterTripsModel
import com.towsal.towsal.network.serializer.trips.TripsModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.TripsMultiViewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for trips history
 * */
class HistoryTripsFragment : BaseFragment(), OnNetworkResponse, FilterStatusCallback {

    companion object {
        var isFilterCall = false
    }

    private lateinit var binding: FragmentHistoryBinding
    private val tripsViewModel: TripsViewModel by viewModel()
    private val homeViewModel: MainScreenViewModel by activityViewModels()
    private var mArrayList = ArrayList<TripsModel>()
    private lateinit var adapter: TripsMultiViewAdapter
    private var filterChange = false
    private var statusFilter: ArrayList<FilterTripsModel> = ArrayList()
    private var model = BookingsTripsResponseModel()
    private var isFragmentVisible = false
    private var isDataLoaded = false
    private var isListenersAttached = false
    var filters: ArrayList<FilterTripsModel> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_history,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

    }

    override fun onResume() {
        super.onResume()
        isFragmentVisible = true
        if (!isListenersAttached) {
            setData()
        } else {
            callApi(1, true)
        }
    }

    override fun onPause() {
        super.onPause()
        isFragmentVisible = false
    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("RestrictedApi")
    private fun setData() {
        addStatusFilters()
        adapter = TripsMultiViewAdapter(requireActivity(), uiHelper)
        binding.recyclerview.adapter = adapter
        binding.pullToRefresh.setOnRefreshListener {
            callApi(1, false)
        }
        callApi(1, true)

        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (model.pagination.currentPage < model.pagination.totalPages && model.pagination.totalPages > 0) {
                        if (isDataLoaded) {
                            callApi(
                                model.pagination.nextPage,
                                true
                            )
                        }
                    }
                }
            }
        })

    }

    private fun setObserver() {
        homeViewModel.filters.observe(viewLifecycleOwner) {
            filters = it
            if(isListenersAttached)
                callApi(1, true)
            isListenersAttached = it.isNotEmpty()

        }
    }
    /**
     * Function for preparing status filter
     * */
    private fun addStatusFilters() {
        statusFilter.clear()
        var model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.all_)
        model.selected = true
        model.id = -1
        statusFilter.add(model)
        model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.cancelled)
        model.selected = false
        model.id = Constants.BookingStatus.CANCELLED
        statusFilter.add(model)
        model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.completed)
        model.selected = false
        model.id = Constants.BookingStatus.COMPLETED
        statusFilter.add(model)
        model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.conflicted)
        model.selected = false
        model.id = Constants.BookingStatus.CONFLICTED
        statusFilter.add(model)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.filterHistory -> {
                isFilterCall = true
                val filters = ArrayList(statusFilter.map {
                    val filter = FilterTripsModel()
                    filter.id = it.id
                    filter.selected = it.selected
                    filter.image_url = it.image_url
                    filter.name = it.name
                    filter
                })
                uiHelper.addFragment(
                    Constants.Container.HISTORY_TRIPS_FRAGMENT_CONTAINER,
                    FilterTripStatusFragment.newInstance(filters, this),
                    childFragmentManager
                )
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            try {
                childFragmentManager.popBackStackImmediate()
                smoothScroller.targetPosition = 0
                if (mArrayList.isNotEmpty())
                    binding.recyclerview.layoutManager?.startSmoothScroll(smoothScroller)
                if (mArrayList.isEmpty() || filterChange) {
                    callApi(1, true)
                }
            } catch (e: Exception) {
                Log.e(this.javaClass.simpleName, "" + e.localizedMessage)
            }

        }
    }

    /**
     * Function for api calling
     * */
    fun callApi(page: Int, showLoader: Boolean) {
        if (isFragmentVisible) {
            tripsViewModel.getMyHistoryBookings(
                page, getFilterId(), getFilterStatus(),
                showLoader,
                Constants.API.GET_MY_PREVIOUS_BOOKINGS,
                this
            )
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        if (this.isAdded) {
            when (tag) {
                Constants.API.GET_MY_PREVIOUS_BOOKINGS -> {
                    isDataLoaded = true
                    binding.pullToRefresh.isRefreshing = false
                    try {
                        model = Gson().fromJson(
                            uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                            BookingsTripsResponseModel::class.java
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    if(!isListenersAttached)
                        setObserver()

                    filterChange = false
                    if (model.pagination.currentPage == 1 || model.pagination.currentPage == 0) {
                        mArrayList.clear()
                    }
                    if (model.bookinghistory.isNotEmpty()) {
                        mArrayList.addAll(model.bookinghistory)
                    }
                    adapter.notifyItems(mArrayList)
                    binding.noDataLayout.isVisible = mArrayList.isEmpty()
                    binding.filterHistory.isVisible = mArrayList.isNotEmpty() || getFilterStatus() != -1
                }
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireContext(), response?.message)
    }

    /**
     * Function for getting selected filter id
     * */
    private fun getFilterId() = (filters.find { it.selected } ?: FilterTripsModel()).id

    /**
     * Function for getting selected filter
     * */
    private fun getFilterStatus() = (statusFilter.find { it.selected } ?: FilterTripsModel()).id


    override fun statusSelected(filterStatusList: ArrayList<FilterTripsModel>) {
        statusFilter = filterStatusList
        callApi(1, true)
    }


    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RequestCodes.ACTIVITY_TRIPS -> {
                    if (this.isMenuVisible) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            callApi(1, true)
                        }, NotificationsFragment.delayTimeMilliSeconds)
                    } else {
                        filterChange = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.filters.removeObservers(viewLifecycleOwner)
        isListenersAttached = false
    }
}