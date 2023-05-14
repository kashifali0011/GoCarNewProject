package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.towsal.towsal.databinding.FragmentNotificationsBinding
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.trips.FilterTripsModel
import com.towsal.towsal.network.serializer.trips.NotificationModel
import com.towsal.towsal.network.serializer.trips.NotificationResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.activities.LoginActivity
import com.towsal.towsal.views.adapters.NotificationAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for notifications
 * */
class NotificationsFragment : BaseFragment(), OnNetworkResponse, PopupCallback {
    private val tripsViewModel: TripsViewModel by viewModel()
    private val homeViewModel: MainScreenViewModel by activityViewModels()
    private var notificationList: ArrayList<NotificationModel> = null ?: ArrayList()
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var adapter: NotificationAdapter
    private var filterList: ArrayList<FilterTripsModel> = ArrayList()
    private var isFragmentVisible = false
    private var isDataLoaded = false
    private var isListenersAttached = false

    companion object {
        var model = NotificationResponseModel()

        /* ....trying to set the notifications on Resume ....*/
        var delayTimeMilliSeconds: Long = 200
    }

    private var filterChange = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
    }

    /*....trying to set notifications on Resume ......*/
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
        model = NotificationResponseModel()
        adapter = NotificationAdapter(
            requireActivity(),
            uiHelper,
            requireActivity().supportFragmentManager
        )
        binding.recyclerview.adapter = adapter
        callApi(1, true)

        binding.pullToRefresh.setOnRefreshListener {
            callApi(1, false)
        }

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

        homeViewModel.filters.observe(viewLifecycleOwner) {
            model.filters = it
            if (isListenersAttached)
                callApi(1, true)
            isListenersAttached = it.isNotEmpty()
        }
    }

    /**
     * Function for calling api's
     * */
    fun callApi(page: Int, showLoader: Boolean) {
        if (isFragmentVisible) {
            tripsViewModel.getAllNotifications(
                page, getFilterId(),
                showLoader,
                Constants.API.GET_ALL_NOTIFICATIONS,
                this
            )
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_ALL_NOTIFICATIONS -> {
                isDataLoaded = true
                binding.pullToRefresh.isRefreshing = false
                filterChange = false
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    NotificationResponseModel::class.java
                )
                if (model.pagination.currentPage == 1 || model.pagination.currentPage == 0) {
                    if (!isListenersAttached) {
                        homeViewModel.setFilters(model.filters)
                    }
                    notificationList.clear()
                }
                if (model.notificationList.isNotEmpty()) {
                    notificationList.addAll(model.notificationList)
                }
                if (model.filters.isNotEmpty())
                    model.filters[0].selected = true
                if (filterList.isNotEmpty()) {
                    model.filters = filterList
                }
                adapter.notifyItems(notificationList)
                if (notificationList.size == 0){
                   binding.recyclerview.isVisible  = false
                    binding.tvNoData.isVisible  = true
                }
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        binding.pullToRefresh.isRefreshing = false
        uiHelper.showLongToastInCenter(requireContext(), response?.message)

    }

    override fun setMenuVisibility(visible: Boolean) {
        if (visible) {
            smoothScroller.targetPosition = 0
            if (notificationList.isNotEmpty())
                binding.recyclerview.layoutManager?.startSmoothScroll(smoothScroller)
            //trying to set the notifications onResume
            if (notificationList.isEmpty() || filterChange) {
                //trying to set the session expire issue without login
                if (uiHelper.userLoggedIn(preferenceHelper)) {
                    callApi(1, true)
                    filterChange = false
                } else {
                    //trying to set the crash on it
                    if (activity != null) {
                        uiHelper.showLongToastInCenter(
                            requireActivity(),
                            getString(R.string.login_list_car_err_msg)
                        )

//                        uiHelper.showPopup(
//                            requireActivity(),
//                            this,
//                            R.string.login,
//                            R.string.cancel_,
//                            R.string.login,
//                            R.string.would_login,
//                            false
//                        )
                        uiHelper.openActivityForResult(
                            requireActivity(),
                            LoginActivity::class.java,
                            Constants.RequestCodes.ACTIVITY_LOGIN
                        )
                    }

                }

            }
        }
        super.setMenuVisibility(visible)
    }

    /**
     * Function for getting selected filter id
     * */
    private fun getFilterId() = (model.filters.find { it.selected } ?: FilterTripsModel()).id

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RequestCodes.ACTIVITY_TRIPS -> {
                    if (this.isMenuVisible) {
                        //trying to set the notifications onResume
                        callApi(1, true)
                    } else {
                        filterChange = true
                    }
                }
            }
        }
    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            uiHelper.openActivityForResult(
                requireActivity(),
                LoginActivity::class.java,
                Constants.RequestCodes.ACTIVITY_LOGIN
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.filters.removeObservers(viewLifecycleOwner)
        isListenersAttached = false
    }
}