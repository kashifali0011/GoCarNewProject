package com.towsal.towsal.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentTripsBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.interfaces.ParentToChild
import com.towsal.towsal.network.serializer.trips.FilterTripsModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.activities.MainActivity

/**
 * Fragment class for trips
 * */
class TripsFragment : BaseFragment(), ParentToChild {

    val homeViewModel: MainScreenViewModel by activityViewModels()
    private var pos = 0
    lateinit var binding: FragmentTripsBinding
    var filters = ArrayList<FilterTripsModel>()

    companion object {
        const val NotificationKey = "Notifications"
        const val BookedKey = "Booked"
        const val HistoryKey = "History"
        const val FilterKey = "Filter"
        lateinit var navController: NavController
        var flow = ""
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_trips,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.trips,
            actionBar = null,
            arrowVisible = false,
            filterIconVisible = true
        )
        binding.layoutToolBar.filter.setOnClickListener(this::onClick)
        setData()
        setObserver()
    }

    private fun setObserver() {
        homeViewModel.filters.observe(viewLifecycleOwner) {
            filters = it
        }
    }


    override fun onResume() {
        super.onResume()
        setData()
        (activity as MainActivity?)?.setBadgeIcon()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        setNavController()
        setClickListeners()
    }
    private fun setClickListeners() {
        binding.clActivity.setOnClickListener {
            it.preventDoubleClick()
            if (binding.viewActivity.isInvisible) {
                navController.navigate(
                    R.id.notificationsFragment
                )
            }
        }
        binding.clBooked.setOnClickListener {
            it.preventDoubleClick()
            if (binding.viewBooked.isInvisible) {
                navController.navigate(
                    R.id.bookedTripsFragment
                )
            }
        }
        binding.clHistory.setOnClickListener {
            it.preventDoubleClick()
            if (binding.viewHistory.isInvisible) {
                navController.navigate(
                    R.id.historyTripsFragment
                )
            }
        }
    }


    private fun setNavController() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph_trips)
        setDestinationListener()
    }

    private fun setDestinationListener() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            setVisibility(destination.id)
        }
    }

    private fun setVisibility(destination: Int) {
        binding.viewActivity.isInvisible = destination != R.id.notificationsFragment
        binding.viewBooked.isInvisible = destination != R.id.bookedTripsFragment
        binding.viewHistory.isInvisible = destination != R.id.historyTripsFragment
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {
            binding.layoutToolBar.filter -> {
                val filters = filters.map {
                    val filter = FilterTripsModel()
                    filter.id = it.id
                    filter.image_url = it.image_url
                    filter.name = it.name
                    filter.selected = it.selected
                    filter
                }
                uiHelper.addFragment(
                    Constants.Container.TRIPS_FRAGMENT_CONTAINER,
                    FilterTripsFragment.newInstance(
                        ArrayList(filters),
                        this
                    ),
                    childFragmentManager
                )
                binding.layoutToolBar.filter.isVisible = false
            }
        }
    }

    override fun sendCallbackFromPTC() {
        binding.layoutToolBar.filter.isVisible = true
        when (navController.currentDestination?.id) {
            R.id.bookedTripsFragment, R.id.historyTripsFragment -> {
                if (BookedTripsFragment.isFilterCall || HistoryTripsFragment.isFilterCall) {
                    BookedTripsFragment.isFilterCall = false
                    HistoryTripsFragment.isFilterCall = false
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RequestCodes.ACTIVITY_TRIPS -> {
                    childFragmentManager.fragments.forEach { fragment ->
                        fragment.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.setFilters(ArrayList())
    }

}