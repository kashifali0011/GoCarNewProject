package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityBookingListBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.*
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DrawableUtils
import com.towsal.towsal.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity class for changing calendar's dates status
 * */
class BookingListActivity : BaseActivity(), OnNetworkResponse {
    lateinit var binding: ActivityBookingListBinding
    var model = CalendarResponseModel()
    var mArrayList = ArrayList<DaySelectionModel>()
    val settingsViewModel: SettingsViewModel by viewModel()

    private val unavailableDaysList: MutableList<String> = ArrayList()

    var carId = 0


    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("yyyy-MM-dd")

    companion object {
        const val BOOKED = 1
        const val UNAVAILABLE = 2
        const val AVAILABLE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_booking_list)
        binding.activity = this

        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {

            }

        })
        setData()

    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("SimpleDateFormat")
    private fun setData() {
        actionBarSetting()
        val min = Calendar.getInstance()
        min.add(Calendar.DATE, -1)
        binding.calendarView.setMinimumDate(min)
        settingsViewModel.getCarCalendar(
            true,
            carId,
            Constants.API.GET_CAR_CALENDAR,
            this
        )

    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.calendar,
            supportActionBar
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_CALENDAR -> {
                binding.mainLayout.visibility = View.VISIBLE
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CalendarResponseModel::class.java
                )
                getBookedDates(model.car_calender)

//                getUnAvailableDates(model.car_calender)

            }
            Constants.API.SEND_CALENDAR_DATE -> {
                uiHelper.showLongToastInCenter(applicationContext, response?.message)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RequestCodes.ACTIVTIY_BOOK_DATE -> {
                    settingsViewModel.getCarCalendar(
                        true,
                        carId,
                        Constants.API.GET_CAR_CALENDAR,
                        this
                    )
                }
            }
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.save -> {
                val unAvailableDatesList = binding.calendarView.selectedDates
                val list: ArrayList<DatesList> = ArrayList()
                val model = UnAvailableDates()

                if (unAvailableDatesList.isNotEmpty()) {
                    val sdfDate = SimpleDateFormat("yyyy-MM-dd")
                    val sdfDay = SimpleDateFormat("EEEE")
                    for (i in unAvailableDatesList.indices) {
                        val datesList = DatesList()
                        val date = sdfDate.format(unAvailableDatesList[i].time)
//                        datesList.date = unAvailableDatesList[i].time.date.toString()
                        datesList.date = date
                        val day = sdfDay.format(unAvailableDatesList[i].time)
//                        datesList.day = unAvailableDatesList[i].time.day.toString()
                        datesList.day = day
                        list.add(datesList)
                    }
                    model.dates = list

                    settingsViewModel.markUnavailable(
                        carId,
                        model,
                        true,
                        Constants.API.SEND_CALENDAR_DATE,
                        this
                    )
                } else {
                    uiHelper.showLongToastInCenter(
                        this,
                        getString(R.string.mark_unavailable_date_err_msg)
                    )
                }
            }
        }
    }

    /**
     * Function for unavailable dates
     * */
    private fun getUnAvailableDates(carCalender: ArrayList<CalendarModel>) {
        if (!carCalender.isNullOrEmpty())
            for (i in carCalender.indices) {
                if (carCalender[i].status == UNAVAILABLE) {
                    val calendar = Calendar.getInstance()
                    val sdfDate = SimpleDateFormat("yyyy-MM-dd")
                    var calendarDay = CalendarDay(calendar)
                    val date = carCalender[i].date
                    unavailableDaysList.add(date)
                }
            }

    }

    /**
     * Function for booked dates
     * */
    private fun getBookedDates(carCalender: ArrayList<CalendarModel>) {

        val calendarDaysList: MutableList<CalendarDay> = java.util.ArrayList()
//        val calendarDay2 = CalendarDay(Calendar.getInstance())
//        calendarDay.backgroundDrawable = DrawableUtils.getDayCircle(
//            this,
//            R.color.colorCurrentDay,
//            R.color.colorCurrentDay
//        )
//        calendarDay.selectedBackgroundDrawable = DrawableUtils.getDayCircle(
//            this,
//            R.color.colorCurrentDay,
//            R.color.colorCurrentDay
//        )

        if (carCalender.isNotEmpty())
            for (i in carCalender.indices) {
                Log.e("CalenderDayStatus ${carCalender[i].date}", carCalender[i].status.toString())
                if (carCalender[i].status == BOOKED) {
                    Log.d("selectCalenderDay", carCalender[i].date)
                    Log.d("selectCalenderDay", carCalender[i].status.toString())
                    val calendar = Calendar.getInstance()
                    val date: Date = sdf.parse(carCalender[i].date)!!
                    calendar.time = date
                    val calendarDay = CalendarDay(calendar)
                    calendarDay.isSelectable = false
                    calendarDay.backgroundDrawable = DrawableUtils.getDayCircle(
                        applicationContext,
                        R.color.bg_chat_bottom,
                        R.color.bg_chat_bottom
                    )
                    calendarDay.selectedBackgroundDrawable = DrawableUtils.getDayCircle(
                        applicationContext,
                        R.color.bg_chat_bottom,
                        R.color.bg_chat_bottom
                    )
                    calendarDaysList.add(calendarDay)

                } else
                    if (carCalender[i].status == UNAVAILABLE) {
                        val calendar = Calendar.getInstance()

                        val date: Date = sdf.parse(carCalender[i].date)!!
                        calendar.time = date
                        val calendarDay = CalendarDay(calendar)
                        calendarDay.backgroundDrawable = DrawableUtils.getDayCircle(
                            applicationContext,
                            R.color.colorUnavailableDays,
                            R.color.colorUnavailableDays
                        )

                        calendarDay.selectedBackgroundDrawable = DrawableUtils.getDayCircle(
                            applicationContext,
                            R.color.bg_chat_bottom,
                            R.color.bg_chat_bottom
                        )
                        calendarDaysList.add(calendarDay)
                    }
            }
        binding.calendarView.setCalendarDays(calendarDaysList)
    }
}