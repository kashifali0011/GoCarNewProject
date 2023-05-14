package com.towsal.towsal.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentSearchCarBinding
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.home.CarSearchResponseModel
import com.towsal.towsal.network.serializer.home.SearchCarDetailModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.activities.CarDetailsActivity
import com.towsal.towsal.views.activities.SearchCarActivity
import com.towsal.towsal.views.adapters.SearchCarAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchCarFragment : BaseFragment(), RecyclerViewItemClick {
    lateinit var binding: FragmentSearchCarBinding
    lateinit var carDetailsList: ArrayList<SearchCarDetailModel>
    val homeViewModel: MainScreenViewModel by activityViewModels()
    val carDetailViewModel: CarDetailViewModel by viewModel()
    var responseModel = CarSearchResponseModel()
    var calendarDateTimeModel = CalendarDateTimeModel()
    lateinit var adapter: SearchCarAdapter

    var itemAt = -1
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            carDetailsList = ArrayList()
            carDetailsList.addAll(adapter.getList())
            Log.e(SearchCarAdapter::javaClass.name, carDetailsList[itemAt].fav_flag.toString())
            if (carDetailsList[itemAt].fav_flag !=
                it!!.data?.extras?.getInt(Constants.DataParsing.FAVORITE_FLAG, 0)
            ) {
                carDetailsList[itemAt].fav_flag =
                    it.data?.extras!!.getInt(Constants.DataParsing.FAVORITE_FLAG, 0)
                Log.e(
                    SearchCarAdapter::javaClass.name,
                    carDetailsList[itemAt].fav_flag.toString()
                )

                adapter.notifyItemChanged(itemAt)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_car, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        homeViewModel.carSearchResponseModel.observe(
            viewLifecycleOwner
        ) { carSearchResponseModel ->
            responseModel = carSearchResponseModel
            populateData()
        }
    }

    private fun populateData() {
        carDetailsList = responseModel.car_details
        adapter =
            SearchCarAdapter(requireActivity(), uiHelper, carDetailsList, this, carDetailViewModel)
        binding.recyclerView.adapter = adapter
        binding.noDataLayout.isVisible = responseModel.car_details.isEmpty()
        binding.nsvDataLayout.isVisible = responseModel.car_details.isNotEmpty()
    }

    override fun onItemClick(position: Int) {
        itemAt = position
        val cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)

        val bundle = Bundle()
        bundle.putString(Constants.DataParsing.ACTIVITY_NAME, SearchCarActivity::javaClass.name)
        bundle.putDouble(Constants.LAT, SearchCarActivity.carSearchModel.lat)
        bundle.putDouble(Constants.LNG, SearchCarActivity.carSearchModel.lng)
        bundle.putInt(Constants.CITY_ID, cityId)
        bundle.putInt(Constants.CAR_ID, responseModel.car_details[itemAt].id)
        bundle.putSerializable(Constants.DataParsing.DATE_TIME_MODEL, calendarDateTimeModel)

        val intent = Intent(
            requireActivity(),
            CarDetailsActivity::class.java
        )
        intent.putExtras(bundle)

        launcher.launch(intent)
    }
}