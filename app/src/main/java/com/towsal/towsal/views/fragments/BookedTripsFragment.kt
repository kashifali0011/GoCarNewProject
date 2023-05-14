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
import com.towsal.towsal.databinding.FragmentBookedHistoryTripsBinding
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
 * Fragment class for booked trips
 * */
class BookedTripsFragment : BaseFragment(), OnNetworkResponse, FilterStatusCallback {

    companion object {
        var isFilterCall = false
    }

    private val tripsViewModel: TripsViewModel by viewModel()
    private val homeViewModel: MainScreenViewModel by activityViewModels()
    private lateinit var binding: FragmentBookedHistoryTripsBinding
    private var mArrayList = ArrayList<TripsModel>()
    private lateinit var adapter: TripsMultiViewAdapter
    private var filterChange = false
    private var statusFilter: ArrayList<FilterTripsModel> = ArrayList()
    private var model = BookingsTripsResponseModel()
    private var isFragmentVisible = false
    private var isDataLoaded = false
    private var isListenersAttached = false
    private var page: Int = -1
    var filters: ArrayList<FilterTripsModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_booked_history_trips,
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
        isFragmentVisible = true
        if (!isListenersAttached) {
            setData()
        } else {
            callApi(1, true)
        }
        super.onResume()
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
                            isDataLoaded = false
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
     * Function fro preparing filter status data
     * */
    private fun addStatusFilters() {
        statusFilter.clear()
        var model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.all_)
        model.selected = true
        model.id = -1
        statusFilter.add(model)
        model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.pending)
        model.selected = false
        model.id = Constants.BookingStatus.PENDING
        statusFilter.add(model)
        model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.in_progress)
        model.selected = false
        model.id = Constants.BookingStatus.IN_PROGRESS
        statusFilter.add(model)
        model = FilterTripsModel()
        model.name = requireActivity().getString(R.string.future)
        model.selected = false
        model.id = Constants.BookingStatus.FUTURE
        statusFilter.add(model)
    }

    /**
     * Function for view click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.filterBooked -> {
                isFilterCall = true
                val filters = ArrayList(statusFilter.map {
                    val filter = FilterTripsModel()
                    filter.id = it.id
                    filter.selected = it.selected
                    filter.image_url = it.image_url
                    filter.name = it.name
                    filter
                })
                uiHelper.addFragmentWithBackStack(
                    Constants.Container.BOOKED_TRIPS_FRAGMENT_CONTAINER,
                    FilterTripStatusFragment.newInstance(filters, this),
                    childFragmentManager,
                    "FilterTripStatusFragment"
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
     * Function for calling api
     * */
    fun callApi(page: Int, showLoader: Boolean) {
        Log.d("filterId", "filterId" + getFilterId())
        Log.d("filterStatus", "filterStatus" + getFilterStatus())
        this.page = page
        if (isFragmentVisible) {
            tripsViewModel.getMyBookings(
                page, getFilterId(), getFilterStatus(),
                showLoader,
                Constants.API.GET_MY_BOOKINGS,
                this
            )
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_MY_BOOKINGS -> {
                if (this.isAdded) {
                    isDataLoaded = true
                    binding.pullToRefresh.isRefreshing = false
                    model = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        BookingsTripsResponseModel::class.java
                    )
                    if(!isListenersAttached)
                        setObserver()
                    filterChange = false
                    if ((model.pagination.currentPage == 1 || model.pagination.currentPage == 0) && (model.isDataExists || page == 1)) {
                        mArrayList.clear()
                    }
                    if (model.bookings.isNotEmpty()) {
                        mArrayList.addAll(model.bookings)
                    }
                    adapter.notifyItems(mArrayList)

                    binding.noDataLayout.isVisible = mArrayList.isEmpty()
                    binding.filterBooked.isVisible = mArrayList.isNotEmpty() || getFilterStatus() != -1
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

