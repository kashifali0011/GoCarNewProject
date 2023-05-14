package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.rizlee.rangeseekbar.RangeSeekBar
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentFiltersBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.home.AdvanceSearchParamsModel
import com.towsal.towsal.network.serializer.home.CarSearchModel
import com.towsal.towsal.network.serializer.home.CarSearchResponseModel
import com.towsal.towsal.network.serializer.home.FilterDataName
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.adapters.VehicleSettingAdapter
import com.towsal.towsal.views.adapters.VehicleTypeAdapter
import kotlinx.android.synthetic.main.fragment_filters.*

/**
 * Fragment class for filters
 * */
class FiltersFragment : BaseFragment(), RangeSeekBar.OnRangeSeekBarRealTimeListener,
    RangeSeekBar.OnRangeSeekBarPostListener, PopupCallback, OnNetworkResponse , VehicleTypeAdapter.OnItemClickListener  {
    private var runFirstTime = false
    private val modelList = ArrayList<String>()
    private var yearSelectedIndex = -1
    private var makeSelectedIndex = -1
    private var selectedIndexSortBy = -1
    private var selectedModelIndex = -1
    private val makeList = ArrayList<String>()
    private val homeViewModel: MainScreenViewModel by activityViewModels()
    private var carSearchModel: CarSearchModel = CarSearchModel()
    private var carSearchResponseModel = CarSearchResponseModel()
    private var isDataLoaded = false

    private var vehicleTypeAdapter: VehicleTypeAdapter? = null
    var selectCarId = 0

    var list: ArrayList<String> = ArrayList()

    companion object {
        var advanceSearchParam = AdvanceSearchParamsModel()
        const val BUDGET = 0
        const val DISTANCE = 1
        const val SORT_BY = 2
    }

    lateinit var binding: FragmentFiltersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_filters,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        binding.tvClearFilter.setGradientTextColor(
            intArrayOf(
                requireActivity().getColor(
                    R.color.text_receive_msg
                ),
                requireActivity().getColor(
                    R.color.send_msg_bg
                )
            )
        )
        setObserver()

    }

    private fun setObserver() {
        homeViewModel.carSearchModel.observe(
            viewLifecycleOwner
        ) {
            carSearchModel = it

            if (advanceSearchParam.years.isEmpty()) {
                homeViewModel.getCarSearchAttributes(
                    Constants.API.CAR_ATTRIBUTES,
                    this,
                    true
                )
            } else {
                setSpinnersCarsRelated()
            }
        }

        homeViewModel.carSearchResponseModel.observe(
            viewLifecycleOwner
        ) {
            carSearchResponseModel = it
            advanceSearchParam.sort_by = it.advance_search_param.sort_by
            advanceSearchParam.distance = it.advance_search_param.distance
            if (!isDataLoaded)
                setDropDownSpinners()
        }
    }

    override fun onResume() {
        super.onResume()
        isDataLoaded = false
    }

    private fun setDropDownSpinners() {
        isDataLoaded = true
        binding.rangeSeekbarBudget.listenerRealTime = this
        binding.rangeSeekbarBudget.listenerPost = this
        if (carSearchModel.price.isNotEmpty()) {
            val separated: List<String> = carSearchModel.price.split("-")
            binding.rangeSeekbarBudget.setCurrentValues(separated[0].toInt(), separated[1].toInt())
            binding.tvBudgetPrice.text =
                "${
                    separated[0].toInt()
                } - ${
                    separated[1].toInt()
                }  ${
                    requireActivity().getString(
                        R.string.sar_day
                    )
                }"

            carSearchModel.price = "${separated[0].toInt()}-${separated[1].toInt()}"
        } else {
            binding.tvBudgetPrice.text =
                "${
                    0
                } - ${
                    2500
                }  ${
                    requireActivity().getString(
                        R.string.sar_day
                    )
                }"

            binding.rangeSeekbarBudget.setCurrentValues(0, 2500)
            carSearchModel.price = ""
        }

        binding.bookInstantly.isChecked = carSearchModel.bookInstantly == 1
        binding.deliveryLocation.isChecked = carSearchModel.car_location == 1
        val sortByList = ArrayList<String>()
        advanceSearchParam.sort_by.mapTo(
            sortByList
        ) {
            it.name
        }
        selectedIndexSortBy = advanceSearchParam.sort_by.indexOfFirst {
            carSearchModel.sort_by == it.id
        }

        uiHelper.setSpinnerWithNewText(requireActivity(), sortByList, binding.spSortedByList)

        binding.spSortedByList.setSelection(if (selectedIndexSortBy == -1) 0 else selectedIndexSortBy)

        //---------------Distance Spinner--------------
        val distanceList = ArrayList<String>()
        advanceSearchParam.distance.mapTo(
            distanceList
        ) {
            if (it.value.lowercase() == getString(R.string.any_).lowercase())
                it.value
            else
                it.value + " " + getString(R.string.km_day)
        }
        val distanceSpinnerSelectedIndex: Int = advanceSearchParam.distance.indexOfFirst {
            it.id == carSearchModel.distance_id
        }
        uiHelper.setSpinnerWithNewText(
            requireActivity(),
            distanceList,
            binding.distanceIncludedList
        )
        binding.distanceIncludedList.setSelection(if (distanceSpinnerSelectedIndex == -1) 0 else distanceSpinnerSelectedIndex)
    }


    /**
     * Function for setting up data in viws
     * */
    private fun setSpinnersCarsRelated() {

     /*   val vehicleType = ArrayList<String>()
        advanceSearchParam.types.mapTo(vehicleType) {
            it.name
        }*/

        val notificationLayoutManager = LinearLayoutManager(requireActivity())
        binding.rvVehicleType.layoutManager = notificationLayoutManager
        selectCarId = advanceSearchParam.types[0].id
        vehicleTypeAdapter = VehicleTypeAdapter( advanceSearchParam.types, requireActivity(), uiHelper ,this)
        binding.rvVehicleType.adapter = vehicleTypeAdapter



        val yearList = ArrayList<String>()
        advanceSearchParam.years.mapTo(yearList) {
            it.year
        }
        yearSelectedIndex = advanceSearchParam.years.indexOfFirst {
            it.id == carSearchModel.year
        }
        uiHelper.setSpinner(requireActivity(), yearList, binding.spYearList)

        advanceSearchParam.models.mapTo(modelList) {
            it.name
        }
        advanceSearchParam.makes.mapTo(makeList) {
            it.name
        }
        selectedModelIndex = advanceSearchParam.models.indexOfFirst {
            it.id == carSearchModel.model
        }

        makeSelectedIndex = advanceSearchParam.makes.indexOfFirst {
            it.id == carSearchModel.make
        }
        uiHelper.setSpinner(requireActivity(), modelList, binding.spModelList)
        binding.spModelList.setSelection(if (selectedModelIndex == -1) 0 else selectedModelIndex)
        binding.makeList.text = makeList[if (makeSelectedIndex == -1) 0 else makeSelectedIndex]
        makeSelectedIndex = 0

        binding.spModelList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectedModelIndex = position
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        binding.spYearList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {

                yearSelectedIndex = position
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        runFirstTime = true
        binding.spYearList.setSelection(if (yearSelectedIndex == -1) 0 else yearSelectedIndex)
        //  binding.odometerList.setSelection(if (odometerSelectedIndex == -1) 0 else odometerSelectedIndex)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            in listOf(
                R.id.ivSortByDropDown,
                R.id.ivSortByArrowUpward
            ) -> {
                setExpandableViewsVisibilities(SORT_BY)
            }
            in listOf(
                R.id.ivBudgetDropDown,
                R.id.ivBudgetArrowUpWard
            ) -> {
                setExpandableViewsVisibilities(BUDGET)
            }
            in listOf(
                R.id.ivDistanceDropDown,
                R.id.ivDistanceArrowUpWard
            ) -> {
                setExpandableViewsVisibilities(DISTANCE)
            }
            R.id.btnApplyFilter -> {
                if (binding.rangeSeekbarBudget.getCurrentValues().leftValue.toInt() == 10 && binding.rangeSeekbarBudget.getCurrentValues().rightValue.toInt() == 10) {
                    carSearchModel.price = ""
                } else {
                    carSearchModel.price =
                        "${binding.rangeSeekbarBudget.getCurrentValues().leftValue.toInt()}-${binding.rangeSeekbarBudget.getCurrentValues().rightValue.toInt()}"
                }

                carSearchModel.sort_by =
                    advanceSearchParam.sort_by[binding.spSortedByList.selectedItemPosition].id
                carSearchModel.distance_id =
                    advanceSearchParam.distance[binding.distanceIncludedList.selectedItemPosition].id
                // car it
              //  carSearchModel.carTypeId = advanceSearchParam.types[binding.vehicleTypeList.selectedItemPosition].id
                carSearchModel.carTypeId = selectCarId

                // end change
                carSearchModel.year =
                    advanceSearchParam.years[binding.spYearList.selectedItemPosition].id
                carSearchModel.bookInstantly = if (binding.bookInstantly.isChecked) 1 else 0
                carSearchModel.car_location = if (binding.deliveryLocation.isChecked) 1 else 0
                homeViewModel.setCarSearchModel(carSearchModel)
                requireActivity().onBackPressed()

            }
            R.id.tvClearFilter -> {
                clearFilterData()
            }
            binding.rlMakeSpinner.id -> {
                uiHelper.showMakeSearchPopUp(requireActivity(), this, makeList)
            }
            R.id.spinnerLL -> {
                binding.clFilter.isVisible = true
                binding.clSelectMulValues.isVisible = true
            }
            R.id.clFilter -> {
                binding.clFilter.isVisible = false
                binding.clSelectMulValues.isVisible = false
            }
            R.id.clSelectMulValues -> {
                binding.clFilter.isVisible = false
                binding.clSelectMulValues.isVisible = false
            }
        }
    }

    private fun setExpandableViewsVisibilities(
        viewToExpandOrCollapse: Int
    ) {

        binding.rlSortedBySpinner.isVisible =
            viewToExpandOrCollapse == SORT_BY && binding.ivSortByDropDown.isVisible

        binding.ivSortByArrowUpward.isVisible =
            viewToExpandOrCollapse == SORT_BY && binding.rlSortedBySpinner.isVisible

        binding.ivSortByDropDown.isVisible = !binding.rlSortedBySpinner.isVisible

        binding.rlDistanceSpinner.isVisible =
            viewToExpandOrCollapse == DISTANCE && binding.ivDistanceDropDown.isVisible

        binding.ivDistanceArrowUpWard.isVisible =
            viewToExpandOrCollapse == DISTANCE && binding.rlDistanceSpinner.isVisible

        binding.ivDistanceDropDown.isVisible = !binding.rlDistanceSpinner.isVisible

        binding.clBudgetSeekbar.isVisible =
            viewToExpandOrCollapse == BUDGET && binding.ivBudgetDropDown.isVisible

        binding.ivBudgetArrowUpWard.isVisible =
            viewToExpandOrCollapse == BUDGET && binding.clBudgetSeekbar.isVisible

        binding.ivBudgetDropDown.isVisible = !binding.clBudgetSeekbar.isVisible

    }

    override fun onValuesChanging(minValue: Float, maxValue: Float) {

    }

    @SuppressLint("SetTextI18n")
    override fun onValuesChanging(minValue: Int, maxValue: Int) {
        binding.tvBudgetPrice.text =
            "${
                if (minValue == maxValue && minValue != 0)
                    maxValue - 250
                else
                    minValue
            } - ${
                if (maxValue == 0 && minValue == 0)
                    250
                else
                    maxValue
            }  ${
                requireActivity().getString(
                    R.string.sar_day
                )
            }"

        if (minValue == 0 && maxValue == 2500) {
            carSearchModel.price = ""
        } else {
            carSearchModel.price = "$minValue-$maxValue"
        }

    }

    override fun onValuesChanged(minValue: Float, maxValue: Float) {

    }

    override fun onValuesChanged(minValue: Int, maxValue: Int) {

        binding.tvBudgetPrice.text =
            "${
                if (minValue == maxValue && minValue != 0)
                    maxValue - 250
                else
                    minValue
            } - ${
                if (maxValue == 0 && minValue == 0)
                    250
                else
                    maxValue
            }  ${
                requireActivity().getString(
                    R.string.sar_day
                )
            }"

        if (maxValue < 250) {
            binding.rangeSeekbarBudget.setCurrentValues(minValue, 250)
            if (minValue == 10 && maxValue == 10) {
                carSearchModel.price = ""
            } else {
                carSearchModel.price = "$minValue-$maxValue"
            }
        }
        if (minValue == maxValue) {
            if (minValue != 0 ) {

               /* try {*/
                    var checkPrice = maxValue - 250
                    Log.d("minValuess" , "$checkPrice")
                    if (checkPrice >= 0){

                        binding.rangeSeekbarBudget.setCurrentValues(maxValue - 250, maxValue)

                        if (minValue == 10 && maxValue == 10) {
                            carSearchModel.price = ""
                        } else {
                            carSearchModel.price = "$minValue-$maxValue"
                        }

                    }else{
                        Log.d("minValuess" , "Error come")
                        binding.rangeSeekbarBudget.setCurrentValues(0,  250)

                        binding.tvBudgetPrice.text =
                            "${
                                0
                            } - ${
                                250
                            }  ${
                                requireActivity().getString(
                                    R.string.sar_day
                                )
                            }"

                        if (minValue == 10 && maxValue == 10) {
                            carSearchModel.price = ""
                        } else {
                            carSearchModel.price = "$minValue-$maxValue"
                        }
                    }


            } else {
                binding.rangeSeekbarBudget.setCurrentValues(0, minValue + 250)

                if (minValue == 10 && maxValue == 10) {
                    carSearchModel.price = ""
                } else {
                    carSearchModel.price = "$minValue-$maxValue"
                }
            }

        }
    }

    override fun popupButtonClick(value: Int) {
        makeSelectedIndex = value
        binding.makeList.text = makeList[makeSelectedIndex]
    }

    override fun popupButtonClick(value: Int, text_id_model: Any) {
        val input = text_id_model as String
        makeSelectedIndex = makeList.indexOfFirst {
            it == input
        }
        binding.makeList.text = makeList[makeSelectedIndex]
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        if (isAdded) {
            when (tag) {
                Constants.API.CAR_ATTRIBUTES -> {
                    val responseModel = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        CarListDataModel::class.java
                    )

                    advanceSearchParam.years = responseModel.car_year
                    advanceSearchParam.types = responseModel.carTypes
                    advanceSearchParam.makes = responseModel.car_make
                    advanceSearchParam.models = responseModel.car_model
                    advanceSearchParam.odometers = responseModel.odometer
                    carSearchResponseModel.advance_search_param = advanceSearchParam
                    homeViewModel.setSearchResponseModel(carSearchResponseModel)
                    setSpinnersCarsRelated()

                }
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        if (isAdded)
            uiHelper.showLongToastInCenter(requireActivity(), response?.message)
        //pbModel.isVisible = false
    }

     fun clearFilterData(){
         binding.tvVecleType.text = "Any"
        carSearchModel.sort_by = 0
        carSearchModel.distance_id = 0
        carSearchModel.




        carTypeId = 0
        carSearchModel.year = 0
        carSearchModel.make = 0
        carSearchModel.model = 0
        carSearchModel.model = 0
        carSearchModel.bookInstantly = 0
        carSearchModel.car_location = 0
        carSearchModel.price = ""
        carSearchModel.delivery_fee = ""
        setSpinnersCarsRelated()
        setDropDownSpinners()

         carSearchModel.price = ""
         carSearchModel.sort_by = 0
         carSearchModel.distance_id = 0
         carSearchModel.carTypeId = 0
         carSearchModel.year = 0
         carSearchModel.bookInstantly =  0
         carSearchModel.car_location =  0
         homeViewModel.setCarSearchModel(carSearchModel)
         requireActivity().onBackPressed()


    }

    override fun onItemClick(position: Int, carId: Int, carName: String, isCheck: Boolean) {

        Log.d("positionposition" ,position.toString())

        if (position == 0){
            val notificationLayoutManager = LinearLayoutManager(requireActivity())
            binding.rvVehicleType.layoutManager = notificationLayoutManager
            selectCarId = advanceSearchParam.types[0].id
            vehicleTypeAdapter = VehicleTypeAdapter( advanceSearchParam.types, requireActivity(), uiHelper ,this)
            binding.rvVehicleType.adapter = vehicleTypeAdapter

            binding.tvVecleType.text = "Any"
            selectCarId = advanceSearchParam.types[0].id
            list.clear()
        }else{

        selectCarId = id
        if (isCheck) {
            list.add("$carName")
            if (list.isNotEmpty()) {
                binding.tvVecleType.text = list.toString()
                var textPreplace = binding.tvVecleType.text.toString()
                var replace = textPreplace.replace("[" , "")
                var finalReplace = replace.replace("]" , "")
                binding.tvVecleType.text = finalReplace
            }else{
                binding.tvVecleType.text = "Any"
                selectCarId = advanceSearchParam.types[0].id
            }
        }else{
            list.remove("$carName")
            if (list.isNotEmpty()) {
                binding.tvVecleType.text = list.toString()
                var textReplace = binding.tvVecleType.text.toString()
                Log.d("tvVecleType" ,textReplace)
                var replace = textReplace.replace("[" , "")
                var finalReplace = replace.replace("]" , "")
                binding.tvVecleType.text = finalReplace
            }else{
                binding.tvVecleType.text = "Any"
                selectCarId = advanceSearchParam.types[0].id
            }
        }
    }}}

