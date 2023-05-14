package com.towsal.towsal.views.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityCarAvailabilitySettingBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.CarAvailabilityModel
import com.towsal.towsal.utils.Availability
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.FromDateAdapter
import com.towsal.towsal.views.adapters.ToDateAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


/**
 * Activity class for car availability settings
 * */
class CarAvailabilitySettingActivity : BaseActivity(), OnNetworkResponse {
    lateinit var binding: ActivityCarAvailabilitySettingBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var day = ""
    var toTime = ""
    var fromTime = ""
    var carId = 0
    var isUnavailable = Constants.Availability.AVAILABLE
    var selectPosition = 1
    var selectFromTime = ""
    var selectToTime = ""
    var hoursList = mutableListOf<String>()

    val snapHelper1 = LinearSnapHelper()
    val snapHelper = LinearSnapHelper()

    lateinit var adapter: FromDateAdapter
    lateinit var toDateAdapter: ToDateAdapter
    var isDataFirstTime = true
    var selectedFromPosition = 0
    var selectedToPosition = 0
    val layoutManager1 = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

    val scrollerForToTime = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            layoutManager1.findViewByPosition(layoutManager1.findFirstVisibleItemPosition())!!.scaleX =
                0.5f
            layoutManager1.findViewByPosition(layoutManager1.findLastVisibleItemPosition())!!.scaleX =
                0.5f
            layoutManager1.findViewByPosition(layoutManager1.findFirstVisibleItemPosition() + 1)!!.scaleX =
                0.5f
            val contentView1 = snapHelper1.findSnapView(layoutManager1)
            val fromDatePosition = contentView1?.let { layoutManager1.getPosition(it) }
            selectToTime = toDateAdapter.getLists()[fromDatePosition?.plus(0)!!]
            contentView1.scaleX = 0.8f
            contentView1.scaleY = 0.8f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_availability_setting)
        hoursList = ArrayList(resources.getStringArray(R.array.hourses).toMutableList())
        binding.activity = this
        setData()
        binding.layoutToolBar.setAsHostToolBar(R.string.pickup_and_return_hours, supportActionBar)
        snapHelper1.attachToRecyclerView(binding.rvFromTime)
        snapHelper.attachToRecyclerView(binding.rvToTime)
        setTimeOnAdapter()
        setSpinnerListeners()
    }

    private fun setTimeOnAdapter() {

        binding.rvFromTime.apply {

            val linearLayoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            layoutManager = linearLayoutManager
            adapter =
                FromDateAdapter(activity, hoursList, object : FromDateAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {}
                })
            scrollToPosition(selectedFromPosition)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstVisibleItemPosition())!!.scaleX =
                        0.5f
                    linearLayoutManager.findViewByPosition(linearLayoutManager.findLastVisibleItemPosition())!!.scaleX =
                        0.5f
                    linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstVisibleItemPosition() + 1)!!.scaleX =
                        0.5f

                    val contentView = snapHelper.findSnapView(layoutManager)
                    val centerPosition = contentView?.let { linearLayoutManager.getPosition(it) }
                    if (centerPosition != null) {
                        selectFromTime = hoursList[centerPosition]
                        selectPosition = centerPosition - 1
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.rvToTime.removeOnScrollListener(scrollerForToTime)
                            setToDate()
                        }, 200)
                    }
                    contentView?.scaleX = 0.8f
                    contentView?.scaleY = 0.8f

                }
            }
            )

        }
    }

    private fun setToDate() {

        var hourList = ArrayList(resources.getStringArray(R.array.secondListHour).toMutableList())
        hourList = ArrayList(hourList.subList(selectPosition, hourList.size))
        binding.rvToTime.apply {
            layoutManager = layoutManager1
            toDateAdapter = ToDateAdapter(activity, hourList)
            adapter = toDateAdapter
            scrollToPosition(if (selectToTime.isNotEmpty()) (if (hourList.indexOfFirst {
                    it.equals(
                        selectToTime,
                        true
                    )
                } >= 0) hourList.indexOfFirst { it.equals(selectToTime, true) } - 1 else 0) else 0)
            addOnScrollListener(
                scrollerForToTime
            )
        }

    }

    /**
     * Function for setting up spinners listeners
     * */

    private fun setSpinnerListeners() {
        binding.spAvailability.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    spinner: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == Constants.Availability.CUSTOM_AVAILABILITY) {
                        setSpinnersVisibility(true)
                    } else {
                        setSpinnersVisibility(false)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        isDataFirstTime = false
        if (intent.extras != null) {
            day = intent.extras!!.getString("day", "")
            carId = intent.extras!!.getInt("carId", 0)
            toTime = intent.extras!!.getString("toTime", "")
            fromTime = intent.extras!!.getString("fromTime", "")

            isUnavailable =
                intent.extras!!.getInt("is_unavailable", Constants.Availability.AVAILABLE)
        }
        binding.spAvailability.setSelection(isUnavailable)

        binding.tvDayName.text = day

        when (isUnavailable) {
            Availability.ALWAYS_AVAILABLE.value -> {
                setSpinnersVisibility(false)
            }
            Availability.UNAVAILABLE.value -> {
                setSpinnersVisibility(false)
            }
            Availability.CUSTOM_HOURS.value -> {
                setSpinnersVisibility(true)

                if (fromTime.isNotEmpty() && toTime.isNotEmpty()) {
                    fromTime = DateUtil.changeDateFormat(
                        "hh:mm a",
                        "hh : mm a",
                        fromTime
                    )
                    val fromTimeIndex = hoursList.indexOfFirst {
                        it.lowercase(Locale.getDefault()) == fromTime.lowercase(Locale.getDefault())
                    }
                    selectedFromPosition = if (fromTimeIndex == -1) 1 else fromTimeIndex - 1

                    toTime = DateUtil.changeDateFormat(
                        "HH:mm",
                        "hh : mm a",
                        toTime
                    )

                    selectToTime = toTime
                    val toTimeIndex = hoursList.indexOfFirst {
                        it.lowercase(Locale.getDefault()) == toTime.lowercase(Locale.getDefault())
                    }
                    selectedToPosition =
                        if (toTimeIndex == -1) fromTimeIndex + 2 else toTimeIndex - 1
                }
            }
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.btnSave -> {
                setCarAvailability()
            }
        }
    }

    /**
     * Function for setting car availability
     * */
    private fun setCarAvailability() {
        val model = CarAvailabilityModel()
        model.day = day
        model.car_id = carId
        if (binding.spAvailability.selectedItemPosition == 0) {
            model.isUnavailable = Constants.Availability.AVAILABLE
        } else if (binding.spAvailability.selectedItemPosition == 1) {
            model.isUnavailable = Constants.Availability.UNAVAILABLE
        } else {

            if (selectFromTime.isNotEmpty() && selectToTime.isNotEmpty()) {
                model.from = DateUtil.changeDateFormat(
                    "hh : mm a",
                    "HH:mm",
                    selectFromTime
                )
                model.to = DateUtil.changeDateFormat(
                    "hh : mm a",
                    "HH:mm",
                    selectToTime
                )
                model.isUnavailable = Constants.Availability.CUSTOM_AVAILABILITY
            } else {
                uiHelper.showLongToastInCenter(
                    this,
                    resources.getString(R.string.select_to_from_time)
                )
                return
            }
        }

        //changing button transition
        /*   uiHelper.changeButtonTransition(
               this,
               binding.btnSave,
               R.color.colorWhite,
               R.drawable.button_green_bg
           )*/

        settingsViewModel.setCarAvailability(
            model,
            true,
            Constants.API.SET_CAR_UNAVAILABILITY,
            this
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SET_CAR_UNAVAILABILITY -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    /**
     * Function for setting spinners visibility
     * */
    private fun setSpinnersVisibility(isVisible: Boolean) {
        binding.llFromDate.isVisible = isVisible
        binding.llToTime.isVisible = isVisible
        binding.tvFrom.isVisible = isVisible
        binding.tvTo.isVisible = isVisible
    }
}