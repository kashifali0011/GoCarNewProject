package com.towsal.towsal.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.databinding.PopupChangeDateTimePickerBinding
import com.towsal.towsal.databinding.PopupDateTimePickerBinding
import com.towsal.towsal.extensions.showToastPopup
import com.towsal.towsal.extensions.stringToDate
import com.towsal.towsal.interfaces.CalendarCallback
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.interfaces.PopupReasonDetaisCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.cardetails.CarCustomAvailability
import com.towsal.towsal.network.serializer.cardetails.ReasonrResponse
import com.towsal.towsal.network.serializer.cardetails.ReportReasonDetails
import com.towsal.towsal.network.serializer.checkoutcarbooking.CarAvailabilityModel
import com.towsal.towsal.network.serializer.settings.DeleteReasonListModel
import com.towsal.towsal.network.serializer.trips.CalendarValidateResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.utils.DrawableUtils
import com.towsal.towsal.utils.ThumbTextSeekBar
import com.towsal.towsal.views.adapters.DeleteReasonListingAdapter
import com.towsal.towsal.views.adapters.SelectReasonOfReportAdapter
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * View model for car details process
 * */
class CarDetailViewModel : BaseViewModel() {

    var unavailableDateFound = false

    /**
     * Function for getting car details
     * */
    fun getCarDetail(
        lat: Double,
        lng: Double,
        cityId: Int,
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val map: HashMap<String, Any> = HashMap()
        map[DataKeyValues.CarInfo.CAR_ID] = carId
        if (lat != 0.0 && lng != 0.0) {
            map[DataKeyValues.LAT] = lat
            map[DataKeyValues.LNG] = lng
        } else {
            map[DataKeyValues.Home.CITY_ID] = cityId
        }
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarDetail(map),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for adding car to favourite's
     * */
    fun addFavorite(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.addToFavCar(carId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for removing car from favourite's
     * */
    fun deleteFavorite(
        carId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.deleteFavCar(carId),
            tag,
            callback,
            showLoader
        )
    }


    /**
     * Function for getting favourite cars
     * */
    fun getFavList(
        userId: Int,
        cityID: Int, lat: Double, lng: Double,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val map: HashMap<String, Any> = HashMap()
        if (cityID != 0) {
            map[DataKeyValues.Home.CITY_ID] = cityID
        } else if (lat != 0.0) {
            map[DataKeyValues.LAT] = lat
            map[DataKeyValues.LNG] = lng
        }

        map[DataKeyValues.USER_ID] = userId
        dataRepository.callApi(
            dataRepository.network.apis()!!.getFavoritesCarList(map),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for showing calendar popup
     * */
    fun showDateTimePicker(
        activity: Activity,
        model: CalendarDateTimeModel,
        callback: CalendarCallback,
        maxDate: Calendar? = null,
        bookedDates: ArrayList<String>,
        carAvailability: CarAvailabilityModel?,
        hideStartAndEndTimeLayout: Boolean
    ) {

        Log.d("hideStartAndEndTime" , hideStartAndEndTimeLayout.toString())

        var calendarDaysList = mutableListOf<Calendar>()
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime[Calendar.HOUR_OF_DAY]
        val advanceNoticeDateCheck = Calendar.getInstance()
        var noticeTime = 0
        val binding = PopupDateTimePickerBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.popup_date_time_picker, null)
        )
        val calendarPopup = Dialog(activity, R.style.full_screen_dialog)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.setContentView(binding.root)
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val save: Button = calendarPopup.findViewById(R.id.save)
        val calendarView: CalendarView = calendarPopup.findViewById(R.id.calendarView)

        val dropOffDate: TextView = calendarPopup.findViewById(R.id.dropOff_date)
        val pickUpDate: TextView = calendarPopup.findViewById(R.id.pickup_date)
        var minimumTimeAchieved = true
//        val reset: TextView = calendarPopup.findViewById(R.id.reset)
        val seekBarStart: ThumbTextSeekBar = calendarPopup.findViewById(R.id.startSeekBar)
        val seekBarEnd: ThumbTextSeekBar = calendarPopup.findViewById(R.id.endSeekbar)
        val tvPickUp: TextView = calendarPopup.findViewById(R.id.tvStartTime)
        val tvDropOff: TextView = calendarPopup.findViewById(R.id.tvEndTime)

        val llStarTime: LinearLayout = calendarPopup.findViewById(R.id.llStarTime)
        val view: View = calendarPopup.findViewById(R.id.view)
        val llEndTime: LinearLayout = calendarPopup.findViewById(R.id.llEndTime)

        // jab search car Activity se aea ga tab pick up and drop off time hide krna h
        if (hideStartAndEndTimeLayout){
            llStarTime.isVisible = false
            view.isVisible = false
            llEndTime.isVisible = false
        }else{
            llStarTime.isVisible = true
            view.isVisible = true
            llEndTime.isVisible = true
        }


        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val sdfServer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.layoutToolBar.toolbarTitle.text = "TRIP DATES"
        binding.layoutToolBar.ivArrowBack.setOnClickListener {
            calendarPopup.dismiss()
        }

        val min = Calendar.getInstance()
        min.add(Calendar.DAY_OF_MONTH, -1)
        if (model.pickUpDateServer.isNotEmpty()) {
            if (calendarView.selectedDates.isEmpty()) {
                Log.e("setDefaultDateTime", model.pickUpDateServer)
                setDefaultDateTime(
                    model,
                    pickUpDate,
                    dropOffDate,
                    calendarView,
                    tvPickUp,
                    tvDropOff,
                    seekBarStart,
                    seekBarEnd
                )

            }
        }
        calendarView.setSelectionBetweenMonthsEnabled(true)
        if (calendarView.selectedDates.isNotEmpty()) {
            pickUpDate.text =
                sdf.format(calendarView.selectedDates[0].time) + ", " + tvPickUp.text.toString()
            dropOffDate.text =
                sdf.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + ", " + tvDropOff.text.toString()
        }
        calendarView.setMinimumDate(min)
        if (maxDate != null)
            calendarView.setMaximumDate(maxDate)
        val unAvailableDaysList =
            carAvailability?.daysCustomAvailability
                ?.filter {
                    it.isUnavailable == Constants.Availability.UNAVAILABLE
                }
                ?.map {
                    it.dayIndex
                } ?: emptyList()
        calendarView.setDisabledWeekDays(unAvailableDaysList)
        val update = Handler(Looper.getMainLooper())

        val list: MutableList<CalendarDay> = ArrayList()
        if (bookedDates.isNotEmpty()) {
            for (i in bookedDates.indices) {
                val calendar = Calendar.getInstance()
                var calendarDayBooked = CalendarDay(calendar)
                Log.e("boobkdatescompiler:", " ${Gson().toJson(bookedDates[i])}")
                val date: Date = sdfServer.parse(bookedDates[i])!!
                calendar.time = date
                calendarDayBooked = CalendarDay(calendar)
                calendarDayBooked.backgroundDrawable = DrawableUtils.getDayCircle(
                    activity,
                    R.color.colorUnavailableDays,
                    R.color.colorUnavailableDays
                )
                calendarDayBooked.selectedBackgroundDrawable = DrawableUtils.getDayCircle(
                    activity,
                    R.color.colorUnavailableDays,
                    R.color.colorUnavailableDays
                )
                list.add(calendarDayBooked)
            }
        }
        if (bookedDates.contains(model.pickUpDateComplete) || bookedDates.contains(
                model.dropOffDateComplete
            )
        ) {
            unavailableDateFound = true
        }
        calendarView.setCalendarDays(list)

        save.setOnClickListener { v ->
            val carCustomAvailabilities = carAvailability?.daysCustomAvailability
                ?.filter {
                    it.isUnavailable == Constants.Availability.CUSTOM_AVAILABILITY
                }
                ?.sortedBy {
                    it.dayIndex
                } ?: emptyList()

            if (calendarView.selectedDates.isNotEmpty()) {
                if (unavailableDateFound) {
                    activity.showToastPopup(activity.getString(R.string.dates_not_available_for_booking_err_msg))
                } else {
                    val timeFormat = SimpleDateFormat("hh:mm a")
                    val timeFormatConversion = SimpleDateFormat("HH:mm:ss")
                    model.pickupDate = pickUpDate.text.toString()
                    model.dropOffDate = dropOffDate.text.toString()
                    model.pickupTime = tvPickUp.text.toString()
                    model.dropOffTime = tvDropOff.text.toString()
                    model.pickUpDateComplete = sdfServer.format(calendarView.selectedDates[0].time)
                    model.dropOffDateComplete =
                        sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time)

                    model.pickUpDateServer =
                        sdfServer.format(calendarView.selectedDates[0].time) + " " + timeFormatConversion.format(
                            timeFormat.parse(tvPickUp.text.toString())!!
                        )
                    model.dropOffDateServer =
                        sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + " " + timeFormatConversion.format(
                            timeFormat.parse(tvDropOff.text.toString())!!
                        )
                    model.dropOffTimeServer = timeFormatConversion.format(
                        timeFormat.parse(tvDropOff.text.toString())!!
                    )
                    model.pickupTimeServer = timeFormatConversion.format(
                        timeFormat.parse(tvPickUp.text.toString())!!
                    )
                    model.comparePickupDate =
                        sdfServer.format(calendarView.selectedDates[0].time) + "T" + timeFormatConversion.format(
                            timeFormat.parse(tvPickUp.text.toString())!!
                        ) + "-0700"
                    model.compareDropOffDate =
                        sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + "T" + timeFormatConversion.format(
                            timeFormat.parse(tvDropOff.text.toString())!!
                        ) + "-0700"
                    if (carAvailability != null) {


                        val dates: MutableList<Date> = java.util.ArrayList()
                        val str_date = model.pickUpDateServer
                        val end_date = model.dropOffDateServer
                        val formatter: DateFormat
                        formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val interval = (24 * 1000 * 60 * 60).toLong()
                        val endTime = formatter.parse(end_date).time

                        var curTime = formatter.parse(str_date).time
                        while (curTime <= endTime) {
                            dates.add(Date(curTime))
                            curTime += interval
                        }

                        Log.d("calendarView" ,dates.size.toString() )

                        var pickUpTime = model.pickUpDateServer.subSequence(11 , 19)
                        var dropOfTime = model.dropOffDateServer.subSequence(11 , 19)

                        if (pickUpTime == dropOfTime){
                            dates.removeAt(dates.size -1)
                        }else{}



                        //calendarView.selectedDates.size >
                        if (dates.size >  carAvailability.max_trip_dur.toInt()) {
                            activity.showToastPopup(activity.getString(R.string.this_car_can_be_booked_for) +" " + carAvailability.max_trip_dur.toInt() +" "+ activity.getString(R.string.days_at_most) )
                            return@setOnClickListener
                        }

                        if (uiHelper.currentDate == model.pickUpDateComplete) {

                            val timeFormatNew = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            if (timeFormatNew.parse(model.pickUpDateServer)!!
                                    .before(advanceNoticeDateCheck.time)
                            ) {
                                activity.showToastPopup("The host has selected $noticeTime hours advance notice for booking this car. Please go and change the start time with at least $noticeTime hours of difference")
                                return@setOnClickListener
                            }
                            val compare =
                                seekBarEnd.seekBar?.progress!! - seekBarStart.seekBar!!.progress
                            val minDuration = carAvailability.min_trip_dur.toInt() * 2
                            minimumTimeAchieved = if (calendarView.selectedDates.size == 1) {
                                compare >= minDuration
                            } else {
                                true
                            }
                            if (!minimumTimeAchieved) {
                                activity.showToastPopup("This car is available for rent for at least " + carAvailability.min_trip_dur + " hours which are not available today. So select another day or time to continue")
                                return@setOnClickListener
                            }
                        }
                    }



                    if (model.pickUpDateServer == model.dropOffDateServer) {
                        activity.showToastPopup(activity.getString(R.string.same_time_err_msg))
                        return@setOnClickListener
                    }
                    val dateFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                    if (dateFormat.parse(model.comparePickupDate)!!
                            .after(dateFormat.parse(model.compareDropOffDate))
                    ) {
                        activity.showToastPopup(activity.getString(R.string.same_day_err_msg_2))
                        return@setOnClickListener
                    }
                    var isHostAvailable = true
                    var customAvailability: CarCustomAvailability? = null
                    if (carAvailability != null && carCustomAvailabilities.isNotEmpty()) {
                        val pickUpCustomAvailability =
                            carCustomAvailabilities.find { it.dayIndex == calendarView.selectedDates[0][Calendar.DAY_OF_WEEK] }
                        val dropOffCustomAvailability =
                            carCustomAvailabilities.find { it.dayIndex == calendarView.selectedDates[calendarView.selectedDates.size - 1][Calendar.DAY_OF_WEEK] }
                        if (pickUpCustomAvailability != null || dropOffCustomAvailability != null) {
                            if (pickUpCustomAvailability != null) {
                                isHostAvailable = DateUtil.checkTimeIsBetweenCustomHours(
                                    calendarView.selectedDates[0].time,
                                    pickUpCustomAvailability,
                                    model.pickUpDateServer
                                )
                                if (!isHostAvailable)
                                    customAvailability = pickUpCustomAvailability
                            }
                            if (dropOffCustomAvailability != null && isHostAvailable) {
                                isHostAvailable = DateUtil.checkTimeIsBetweenCustomHours(
                                    calendarView.selectedDates[calendarView.selectedDates.size - 1].time,
                                    dropOffCustomAvailability,
                                    model.dropOffDateServer
                                )
                                if (!isHostAvailable)
                                    customAvailability = dropOffCustomAvailability
                            }
                        }
                    }
                    if (isHostAvailable) {
                        callback.onCalendarDateTimeSelected(model)
                        calendarPopup.dismiss()
                    } else {
                        val sdfTimeIn24Format =
                            SimpleDateFormat(Constants.TIME_FORMAT_24_HOURS, Locale.getDefault())
                        val sdfAmPmFormat =
                            SimpleDateFormat(Constants.AM_PM_TIME_FORMAT, Locale.getDefault())
                        val from =
                            sdfAmPmFormat.format(sdfTimeIn24Format.parse(customAvailability?.from))
                        val to =
                            sdfAmPmFormat.format(sdfTimeIn24Format.parse(customAvailability?.to))
                        activity.showToastPopup("The host is available for pickup and dropoff from '${from}' to '${to}' on '${customAvailability?.day}")
                    }

                }
            } else {
                activity.showToastPopup(activity.getString(R.string.select_pickup_dropoff_date))
            }
        }
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                if (calendarView.selectedDates.isEmpty()) {
                    pickUpDate?.text =
                        sdf.format(eventDay.calendar.time) + ", " + tvPickUp?.text.toString()
                    dropOffDate?.text =
                        sdf.format(eventDay.calendar.time) + ", " + tvDropOff?.text.toString()
                } else {
                    dropOffDate?.text =
                        sdf.format(eventDay.calendar.time) + ", " + tvDropOff?.text.toString()
                }
                update.postDelayed(
                    {
                        unavailableDateFound = false
                        if (!bookedDates.isNullOrEmpty()) {
                            outerLoop@ for (i in bookedDates.indices) {
                                Log.e("DateLoopLog4", "DateLoopLog4")
                                for (j in calendarView.selectedDates.indices) {
                                    Log.e(
                                        "DateLoopLog5",
                                        "DateLoopLog5 " + calendarView.selectedDates[j].time
                                    )
                                    if (bookedDates[i] == sdfServer.format(calendarView.selectedDates[j].time)) {
                                        unavailableDateFound = true
                                        break@outerLoop
                                    }
                                }
                            }
                        }
                        if (unavailableDateFound) {
                            activity.showToastPopup(activity.getString(R.string.dates_not_available_for_booking_err_msg))
                        }

                        if (calendarView.selectedDates.isNotEmpty()) {
                            pickUpDate.text =
                                sdf.format(calendarView.selectedDates[0].time) + " " + binding.tvStartTime.text.toString()
                            dropOffDate.text =
                                sdf.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + " " + binding.tvEndTime.text.toString()

                        }
                        if (calendarView.selectedDates.size == 1) {
                            if (sdfServer.format(calendarView.selectedDates[0].time) == uiHelper.currentDate) {
                                if (currentHour + 1 + noticeTime < 24) {
                                    seekBarStart.seekBar?.progress =
                                        (currentHour + 1 + noticeTime) * 2
//                                            seekBarStart.setThumbText((currentHour + 1 + noticeTime) * 2)
                                } else {
                                    seekBarStart.seekBar?.progress = (currentHour + 1) * 2
//                                            seekBarStart.setThumbText((currentHour + 1 + noticeTime) * 2)
                                }
                            }
                            if (sdfServer.format(calendarView.selectedDates[0].time) != uiHelper.currentDate) {
                                if (carAvailability != null) {
                                    seekBarStart.seekBar?.progress = 0
//                                            seekBarStart.setThumbText(0)
                                }
                            }
                            if (calendarView.selectedDates.size > 1 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) == uiHelper.currentDate
                            ) {
                                if (currentHour + 1 + noticeTime < 24) {
                                    seekBarStart.seekBar?.progress =
                                        (currentHour + 1 + noticeTime) * 2
//                                            seekBarStart.setThumbText((currentHour + 1 + noticeTime) * 2)
                                } else {
                                    seekBarStart.seekBar?.progress = (currentHour + 1) * 2
//                                            seekBarStart.setThumbText((currentHour + 1) * 2)
                                }
                            }
                            if (carAvailability != null) {
                                if (sdfServer.format(calendarView.selectedDates[0].time) == model.minimumDateOnlyDefault) {
                                    setCarBasicLimits(
                                        calendarView = calendarView,
                                        progress = seekBarStart.seekBar.progress,
                                        sdfServer = sdfServer,
                                        seekBarStart = seekBarStart,
                                        model = model,
                                        seekBarEnd = seekBarEnd,
                                        carAvailability = carAvailability,
                                        bookedDates = bookedDates,
                                        tvDropOff = tvDropOff,
                                        tvPickUp = tvPickUp,
                                        dropOffDate = dropOffDate,
                                        sdf = sdf,
                                        activity = activity
                                    )
                                } else if (seekBarEnd.seekBar.progress < seekBarStart.seekBar.progress + (carAvailability.min_trip_dur.toInt() * 2)) {
                                    seekBarEnd.seekBar.progress =
                                        seekBarStart.seekBar.progress + (carAvailability.min_trip_dur.toInt() * 2)
                                }
                            }
                        }
                        if (calendarView.selectedDates.size > 1) {
                            if (calendarView.selectedDates.size > 1 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) == uiHelper.currentDate
                            ) {
                                if (currentHour + 1 + noticeTime < 24) {
                                    seekBarStart.seekBar?.progress =
                                        (currentHour + 1 + noticeTime) * 2
//                                            seekBarStart.setThumbText((currentHour + 1 + noticeTime) * 2)

                                } else {
                                    seekBarStart.seekBar?.progress =
                                        (currentHour + 1) * 2
//                                            seekBarStart.setThumbText((currentHour + 1) * 2)
                                }
                            }
                            if (carAvailability != null) {
                                if (seekBarEnd.seekBar.progress < seekBarStart.seekBar.progress + (carAvailability.min_trip_dur.toInt() * 2)
                                ) {
                                    if (sdfServer.format(
                                            calendarView.selectedDates[calendarView.selectedDates.size - 1].time
                                        ) == model.dropOffDateComplete
                                    ) {
                                        val timeFormatOther =
                                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                        val dateMinDuration =
                                            timeFormatOther.parse(model.minimumDurationDateTime)
                                        val minCalendar = Calendar.getInstance()
                                        minCalendar.time = dateMinDuration!!
                                        val minDurationHour =
                                            minCalendar[Calendar.HOUR_OF_DAY] * 2
                                        seekBarEnd.seekBar.progress = minDurationHour
//                                                seekBarEnd.setThumbText(minDurationHour)

                                    }
                                    //                                        else
                                    //                                            seekBarEnd?.seekBar.progress  =seekBarStart!!.seekBar.progress + carAvailability.min_trip_dur.name.toInt())
                                }
                            }
                        }
                        /* var progress = seekBarStart!!.seekBar.progress
                                     var progressEnd = seekBarEnd!!.seekBar.progress
                                     seekBarStart!!.seekBar.progress  = 0
                                     seekBarEnd!!.seekBar.progress  = 0
                                     seekBarStart.seekBar.progress  = progress
                                     seekBarEnd.seekBar.progress  = progressEnd*/
                        Log.e("DatesSelected", calendarView.selectedDates.map {
                            it.get(Calendar.DAY_OF_WEEK)
                        }.toString())


                    }, 500
                )

            }
        })
        seekBarStart.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                pickupTime.text = calculateTime(progress)
                //                tvThumb.attachToSeekBar(seekBar);
                performVibration(activity)
                tvPickUp.text = calculateTime(progress)
                pickUpDate.text =
                    sdf.format(calendarView.selectedDates[0].time) + ", " + tvPickUp.text.toString()
//                seekBarStart.setThumbText(progress)
                if (carAvailability != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        setCarBasicLimits(
                            calendarView = calendarView,
                            progress = seekBarStart.seekBar.progress,
                            sdfServer = sdfServer,
                            seekBarStart = seekBarStart,
                            model = model,
                            seekBarEnd = seekBarEnd,
                            carAvailability = carAvailability,
                            bookedDates = bookedDates,
                            tvDropOff = tvDropOff,
                            tvPickUp = tvPickUp,
                            dropOffDate = dropOffDate,
                            sdf = sdf,
                            activity = activity
                        )
                    }, 200)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        seekBarEnd.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (calendarView.selectedDates.isNotEmpty()) {
                    tvDropOff.text = calculateTime(progress)
                    dropOffDate.text =
                        sdf.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + ", " + tvDropOff.text.toString()
                    performVibration(activity)
                    val timeFormatConversion = SimpleDateFormat("HH:mm:ss")
                    val timeFormatOther = SimpleDateFormat("hh:mm a")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    if (carAvailability != null) {
                        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        if (calendarView.selectedDates.isNotEmpty()) {
                            val calendar = Calendar.getInstance()
                            calendar.time = timeFormat.parse(model.minimumDurationDateTime)!!
                            val date =
                                sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time)
                            val dateMinDuration = timeFormat.parse(model.minimumDurationDateTime)
                            if (date == sdfServer.format(dateMinDuration!!)) {
                                val minCalendar = Calendar.getInstance()
                                minCalendar.time = dateMinDuration
                                val minDurationHour = minCalendar[Calendar.HOUR_OF_DAY] * 2
                                if (progress < minDurationHour) {
                                    seekBarEnd.seekBar.progress = minDurationHour
                                }
                            }
                            if (calendarView.selectedDates.size == 1 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) != model.pickUpDateComplete
                            ) {
                                val progressMin = carAvailability.min_trip_dur.toInt() * 2
                                val calculation = seekBarStart.seekBar.progress + progressMin
                                if (progress < calculation) {
                                    seekBarEnd.seekBar.progress = calculation
                                    return
                                }
                            }
                            if (calendarView.selectedDates.size == 2 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) != model.pickUpDateComplete
                            ) {
                                val progressMin = carAvailability.min_trip_dur.toInt()
                                val calendar = Calendar.getInstance()

                                val time = timeFormatConversion.format(
                                    timeFormatOther.parse(tvPickUp.text.toString())!!
                                )
                                calendar.time =
                                    timeFormat.parse(sdfServer.format(calendarView.selectedDates[0].time) + " " + time)!!
                                calendar.add(
                                    Calendar.HOUR,
                                    progressMin
                                )

                                val dropOffHour = calendar[Calendar.HOUR_OF_DAY] * 2
                                val dropOffTimeCheck = Calendar.getInstance()
                                val dropOffDateTime =
                                    dateFormat.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + " " + timeFormatConversion.format(
                                        timeFormatOther.parse(tvDropOff.text.toString())!!
                                    )
                                dropOffTimeCheck.time = timeFormat.parse(dropOffDateTime)!!
                                if (dropOffTimeCheck.time.before(calendar.time)) {
                                    seekBarEnd.seekBar.progress = dropOffHour
                                }
                            }
                        }

                    }

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        if (carAvailability != null) {
            if (calendarView.selectedDates.size == 1) {
                seekBarEnd.seekBar.progress = 0 + (carAvailability.min_trip_dur.toInt() * 2)
//                seekBarEnd.setThumbText(0 + (carAvailability.min_trip_dur.name.toInt() * 2))

            }
        }
//        reset.setOnClickListener {
//            model.minimumDateTime = model.minimumDateTimeDefault
//            model.minimumDurationDateTime = model.minimumDurationDateTimeDefault
//            model.minimumDateOnly = model.minimumDateOnlyDefault
//            setDefaultDateTime(
//                model,
//                pickUpDate,
//                dropOffDate,
//                calendarView,
//                tvPickUp,
//                tvDropOff,
//                seekBarStart,
//                seekBarEnd
//            )
//        }
        if (carAvailability != null) {
            noticeTime = (NumberFormat.getInstance()
                .parse(carAvailability.adv_notice_trip_start) as Number).toInt()
            advanceNoticeDateCheck.add(Calendar.HOUR, noticeTime)
        }
        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * Function for showing calendar popup for in progress booking
     * */
    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility", "SetTextI18n")
    fun showDateTimePicker(
        pickUpDateComing: String,
        activity: Activity,
        model: CalendarDateTimeModel,
        callback: CalendarCallback,
        bookedDate: String,
        pickupDateTime: String,
        dropOffDateTime: String,
        carAvailability: CarAvailabilityModel?
    ) {
        val currentTime = Calendar.getInstance()

        val sdfServer = SimpleDateFormat("yyyy-MM-dd")

        val binding = PopupChangeDateTimePickerBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.popup_change_date_time_picker, null)
        )
        val calendarPopup: Dialog = Dialog(activity, R.style.full_screen_dialog)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.setContentView(binding.root)
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        binding.layoutToolBar.toolbarTitle.text = "TRIP DATES"
        binding.layoutToolBar.ivArrowBack.setOnClickListener {
            calendarPopup.dismiss()
        }
        val save: Button? = calendarPopup.findViewById(R.id.save)
        val calendarView: CalendarView? = calendarPopup.findViewById(R.id.calendarView)

        val dropOffDate: TextView? = calendarPopup.findViewById(R.id.dropOff_date)
        val pickUpDate: TextView? = calendarPopup.findViewById(R.id.pickup_date)
        val tvPickup: TextView? = calendarPopup.findViewById(R.id.tvStartTime)
        val tvDropOff: TextView? = calendarPopup.findViewById(R.id.tvEndTime)
        val seekBarEnd: ThumbTextSeekBar? = calendarPopup.findViewById(R.id.endSeekbar)
        val sdf = SimpleDateFormat("EEE, MMM dd")
        val currentHour = currentTime[Calendar.HOUR_OF_DAY]
        seekBarEnd?.seekBar?.progress =
            (currentHour + 1) * 2
        tvPickup?.text = calculateTime(seekBarEnd?.seekBar?.progress!!)
        tvDropOff?.text = calculateTime(seekBarEnd.seekBar?.progress!!)
        pickUpDate?.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateTime1Format,
            pickupDateTime
        )

        dropOffDate?.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateTime1Format,
            dropOffDateTime
        )

        if (model.pickUpDateServer.isNotEmpty()) {
            if (calendarView?.selectedDates?.isEmpty() == true) {
            val update = Handler(Looper.getMainLooper())
            update.postDelayed(
                {
                    Log.e("setDefaultDateTime", model.pickUpDateServer)
                    setDefaultDateTime(
                        model = model,
                        dropOffDate = dropOffDate,
                        calendarView = calendarView,
                        tvDropOff = tvDropOff,
                        seekBarEnd = seekBarEnd
                    )
                }, 700
            )
            }
        }

        val min = Calendar.getInstance()
        min.time = SimpleDateFormat(Constants.ServerDateFormat).parse(
            DateUtil.changeDateFormat(
                Constants.ServerDateTimeFormat,
                Constants.ServerDateFormat,
                pickupDateTime
            )
        ) as Date
        val max = Calendar.getInstance()
        if (bookedDate.isNotEmpty()) {
            max.time = SimpleDateFormat(Constants.ServerDateFormat).parse(bookedDate) as Date
            calendarView?.setMaximumDate(max)
        }
        calendarView?.setMinimumDate(min)
        calendarView?.setSelectionBetweenMonthsEnabled(true)
        calendarView?.setDate(min)

        val update = Handler(Looper.getMainLooper())

        val calendarDay = CalendarDay(Calendar.getInstance())
        calendarDay.backgroundDrawable =
            DrawableUtils.getDayCircle(activity, R.color.colorCurrentDay, R.color.colorCurrentDay)
        val list: MutableList<CalendarDay> = ArrayList()
        list.add(calendarDay)
        save?.setOnClickListener { v ->
            if (calendarView?.firstSelectedDate?.time != null) {
                val timeFormat = SimpleDateFormat("hh:mm a")
                val timeFormatConversion = SimpleDateFormat("HH:mm:ss")
                val pickUpDateComingDate =
                    stringToDate(pickUpDateComing, Constants.ServerDateFormat)!!
                val selectedDropOff =
                    sdfServer.format(calendarView.firstSelectedDate.time) + " " + timeFormatConversion.format(
                        timeFormat.parse(tvDropOff?.text.toString())!!
                    )
                val selectedPickUp =
                    stringToDate(pickupDateTime, Constants.ServerDateTimeFormat)!!
                val minDropOffCalendar = Calendar.getInstance()
                val minTripDurationCalendar = Calendar.getInstance()
                minTripDurationCalendar.time = selectedPickUp
                if (carAvailability != null)
                    minTripDurationCalendar[Calendar.HOUR_OF_DAY] += carAvailability.min_trip_dur.toInt()
                minDropOffCalendar.time =
                    if (pickUpDateComingDate > minTripDurationCalendar.time) {
                        if (pickUpDateComingDate > minDropOffCalendar.time) {
                            pickUpDateComingDate
                        } else {
                            minDropOffCalendar.time
                        }
                    } else {
                        minTripDurationCalendar.time
                    }
                if (stringToDate(
                        selectedDropOff,
                        Constants.ServerDateTimeFormat
                    )!! <= minDropOffCalendar.time
                ) {
                    activity.showToastPopup("You have to book a car for atleast ${carAvailability?.min_trip_dur} hours")
                } else {
                    val dates: MutableList<Date> = java.util.ArrayList()
                    val str_date = model.pickUpDateServer
                    val end_date = model.dropOffDateServer
                    val formatter: DateFormat
                    formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val interval = (24 * 1000 * 60 * 60).toLong()
                    val endTime = formatter.parse(end_date).time

                    var curTime = formatter.parse(str_date).time
                    while (curTime <= endTime) {
                        dates.add(Date(curTime))
                        curTime += interval
                    }

                    Log.d("calendarView" ,dates.size.toString() )
                    var pickUpTime = model.pickUpDateServer.subSequence(11 , 19)
                    var dropOfTime = model.dropOffDateServer.subSequence(11 , 19)

                    if (pickUpTime == dropOfTime){
                        dates.removeAt(dates.size -1)
                    }else{}
                    Log.d("calendarView" ,dates.size.toString() )
                    Log.d("calendarView " , calendarView.selectedDates.size.toString())
                    Log.d("calendarView" , carAvailability!!.max_trip_dur)

                    if (dates.size > carAvailability.max_trip_dur.toInt()) {
                        activity.showToastPopup(activity.getString(R.string.booking_day_exceeds_limit_err_msg) +" " + carAvailability.max_trip_dur.toInt() +" "+ activity.getString(R.string.days_at_most) )
                        return@setOnClickListener
                    }

                    val perfectDropOff =
                        stringToDate(selectedDropOff, Constants.ServerDateTimeFormat)
                    model.dropOffDate = dropOffDate?.text.toString()
                    model.dropOffTime = tvDropOff?.text.toString()
                    model.dropOffDateComplete =
                        selectedDropOff.split(" ")[0]
                    model.dropOffDateServer =
                        selectedDropOff
                    model.dropOffTimeServer = timeFormatConversion.format(
                        perfectDropOff ?: Date()
                    )



                    callback.onCalendarDateTimeSelected(model)
                    calendarPopup.dismiss()
                }
            } else {
                activity.showToastPopup(activity.getString(R.string.select_any_time))
            }
        }

        calendarView?.setOnDayClickListener(object :
            OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                if (eventDay.calendar.time < min.time) {
                    activity.showToastPopup("Notice period out of bound")
                    return
                }
                dropOffDate?.text =
                    sdf.format(eventDay.calendar.time) + ", " + calculateTime(seekBarEnd.seekBar.progress)
                update.postDelayed(
                    {
                        if (calendarView.selectedDates.isEmpty()) {
                            setDefaultDateTime(
                                model = model,
                                dropOffDate = dropOffDate,
                                calendarView = calendarView,
                                tvDropOff = tvDropOff,
                                seekBarEnd = seekBarEnd
                            )
                        }
                    }, 500
                )
            }
        })
        seekBarEnd.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                tvDropOff?.text = calculateTime(progress)
//                seekBarEnd.setThumbText(progress)
                binding.dropOffDate.text =
                    sdf.format(binding.calendarView.firstSelectedDate.time) + ", " + tvDropOff?.text.toString()
                performVibration(activity)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    /**
     * Function for showing calendar popup when user click's on change in snackbar
     * */
    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility", "SetTextI18n")
    fun showDateTimePickerWithTime(
        activity: Activity,
        model: CalendarDateTimeModel,
        callback: CalendarCallback,
        bookedDates: ArrayList<String>,
        maxDate: Calendar? = null,
        dropoffDateAndTime: String,
        carAvailability: CarAvailabilityModel?
    ) {

        val currentTime = Calendar.getInstance()

        val currentHour = currentTime[Calendar.HOUR_OF_DAY]
        val advanceNoticeDateCheck = Calendar.getInstance()
        var noticeTime = 0
        val calendarPopup = Dialog(activity, R.style.full_screen_dialog)
        val binding = PopupDateTimePickerBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.popup_date_time_picker, null)
        )

        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.setContentView(binding.root)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val crossBtn: ImageButton? = calendarPopup.findViewById(R.id.cross)
        val save: Button? = calendarPopup.findViewById(R.id.save)
        val calendarView: CalendarView = calendarPopup.findViewById(R.id.calendarView)

        val dropOffDate: TextView? = calendarPopup.findViewById(R.id.dropOff_date)
        val pickUpDate: TextView? = calendarPopup.findViewById(R.id.pickup_date)
        val tvPickUp: TextView? = calendarPopup.findViewById(R.id.tvStartTime)
        val tvDropOff: TextView? = calendarPopup.findViewById(R.id.tvEndTime)
        binding.layoutToolBar.toolbarTitle.text = "TRIP DATES"
        binding.layoutToolBar.ivArrowBack.setOnClickListener {
            calendarPopup.dismiss()
        }
        var minimumTimeAchieved = true
        val seekBarStart: ThumbTextSeekBar? = calendarPopup.findViewById(R.id.startSeekBar)
        val seekBarEnd: ThumbTextSeekBar? = calendarPopup.findViewById(R.id.endSeekbar)
        val sdf = SimpleDateFormat("EEE, MMM dd")
        val sdfTime = SimpleDateFormat(Constants.UIDateTime1Format)
        val sdfServer = SimpleDateFormat("yyyy-MM-dd")
        crossBtn?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val min = Calendar.getInstance()
        min.add(Calendar.DAY_OF_MONTH, -1)

        calendarView.setSelectionBetweenMonthsEnabled(true)
        pickUpDate?.text =
            sdfTime.format(SimpleDateFormat(Constants.ServerDateTimeFormat).parse(model.pickUpDateServer))
        dropOffDate?.text =
            sdfTime.format(SimpleDateFormat(Constants.ServerDateTimeFormat).parse(model.dropOffDateServer))


        calendarView.setMinimumDate(min)
        if (maxDate != null) {
            calendarView.setMaximumDate(maxDate)
        }
        val unAvailableDaysList =
            carAvailability?.daysCustomAvailability
                ?.filter {
                    it.isUnavailable == Constants.Availability.UNAVAILABLE
                }
                ?.map {
                    it.dayIndex
                } ?: emptyList()
        binding.calendarView.setDisabledWeekDays(unAvailableDaysList)
        val update = Handler(Looper.getMainLooper())

        val list: MutableList<CalendarDay> = ArrayList()
        if (bookedDates.isNotEmpty()) {
            for (i in bookedDates.indices) {
                val calendar = Calendar.getInstance()
                val date: Date = sdfServer.parse(bookedDates[i])!!
                calendar.time = date
                val calendarDayBooked = CalendarDay(calendar)
                calendarDayBooked.backgroundDrawable = DrawableUtils.getDayCircle(
                    activity,
                    R.color.colorUnavailableDays,
                    R.color.colorUnavailableDays
                )
                calendarDayBooked.selectedBackgroundDrawable = DrawableUtils.getDayCircle(
                    activity,
                    R.color.colorUnavailableDays,
                    R.color.colorUnavailableDays
                )
                list.add(calendarDayBooked)
            }
        }
        Log.d("carAvailableDate", "dropoffTime = " + model.dropOffTime)
        Log.d("carAvailableDate", "dropoffTimeDate = " + dropoffDateAndTime)
        if (bookedDates.contains(model.pickUpDateComplete) || bookedDates.contains(
                model.dropOffDateComplete
            )
        ) {
            unavailableDateFound = true
        }
        calendarView.setCalendarDays(list)


        if (model.pickUpDateServer.isNotEmpty()) {

            if (calendarView.selectedDates.isEmpty()) {
                setDefaultDateTime(
                    model,
                    pickUpDate,
                    dropOffDate,
                    calendarView,
                    tvPickUp,
                    tvDropOff,
                    seekBarStart,
                    seekBarEnd
                )

            }
        }
        save?.setOnClickListener { v ->

            val carCustomAvailabilities = carAvailability?.daysCustomAvailability
                ?.filter {
                    it.isUnavailable == Constants.Availability.CUSTOM_AVAILABILITY
                }
                ?.sortedBy {
                    it.dayIndex
                } ?: emptyList()

            if (calendarView.selectedDates.isNotEmpty()) {
                if (unavailableDateFound) {
                    activity.showToastPopup(activity.getString(R.string.dates_not_available_for_booking_err_msg))
                } else {
                    val timeFormat = SimpleDateFormat("hh:mm a")
                    val timeFormatConversion = SimpleDateFormat("HH:mm:ss")
                    model.pickupDate = pickUpDate?.text.toString()
                    model.dropOffDate = dropOffDate?.text.toString()
                    model.pickupTime = tvPickUp?.text.toString()
                    model.dropOffTime = tvDropOff?.text.toString()
                    model.pickUpDateComplete =
                        sdfServer.format(calendarView.selectedDates[0].time)
                    model.dropOffDateComplete =
                        sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time)

                    model.pickUpDateServer =
                        sdfServer.format(calendarView.selectedDates[0].time) + " " + timeFormatConversion.format(
                            timeFormat.parse(tvPickUp?.text.toString())!!
                        )
                    model.dropOffDateServer =
                        sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + " " + timeFormatConversion.format(
                            timeFormat.parse(tvDropOff?.text.toString())!!
                        )
                    model.dropOffTimeServer = timeFormatConversion.format(
                        timeFormat.parse(tvDropOff?.text.toString())!!
                    )
                    model.pickupTimeServer = timeFormatConversion.format(
                        timeFormat.parse(tvPickUp?.text.toString())!!
                    )
                    model.comparePickupDate =
                        sdfServer.format(calendarView.selectedDates[0].time) + "T" + timeFormatConversion.format(
                            timeFormat.parse(tvPickUp?.text.toString())!!
                        ) + "-0700"
                    model.compareDropOffDate =
                        sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + "T" + timeFormatConversion.format(
                            timeFormat.parse(tvDropOff?.text.toString())!!
                        ) + "-0700"
                    if (carAvailability != null) {
                        Log.d("calendarView " , model.pickUpDateServer)
                        Log.d("calendarView" , model.dropOffDateServer)

                        val dates: MutableList<Date> = java.util.ArrayList()
                        val str_date = model.pickUpDateServer
                        val end_date = model.dropOffDateServer
                        val formatter: DateFormat
                        formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val interval = (24 * 1000 * 60 * 60).toLong()
                        val endTime = formatter.parse(end_date).time

                        var curTime = formatter.parse(str_date).time
                        while (curTime <= endTime) {
                            dates.add(Date(curTime))
                            curTime += interval
                        }

                        Log.d("calendarView" ,dates.size.toString() )

                        var pickUpTime = model.pickUpDateServer.subSequence(11 , 19)
                        var dropOfTime = model.dropOffDateServer.subSequence(11 , 19)

                        if (pickUpTime == dropOfTime){
                            dates.removeAt(dates.size -1)
                        }else{}

                        Log.d("calendarView" ,dates.size.toString() )

                        Log.d("calendarView " , calendarView.selectedDates.size.toString())
                        Log.d("calendarView" , carAvailability.max_trip_dur)

                        if (dates.size > carAvailability.max_trip_dur.toInt()) {
                            activity.showToastPopup(activity.getString(R.string.booking_day_exceeds_limit_err_msg) +" " + carAvailability.max_trip_dur.toInt() +" "+ activity.getString(R.string.days_at_most) )
                          //  activity.showToastPopup(activity.getString(R.string.booking_day_exceeds_limit_err_msg))

                            return@setOnClickListener
                        }

                        if (uiHelper.currentDate == model.pickUpDateComplete) {
                            val timeFormatNew = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            if (timeFormatNew.parse(model.pickUpDateServer)!!
                                    .before(advanceNoticeDateCheck.time)
                            ) {
                                activity.showToastPopup("The host has selected $noticeTime hours advance notice for booking this car. Please go and change the start time with at least $noticeTime hours of difference")
                                return@setOnClickListener
                            }
                        }
                        val compare =
                            seekBarEnd!!.seekBar?.progress!! - seekBarStart!!.seekBar!!.progress
                        val minDuration = carAvailability.min_trip_dur.toInt() * 2
                        if (calendarView.selectedDates.size == 1) {
                            minimumTimeAchieved = compare >= minDuration
                        } else {
                            minimumTimeAchieved = true
                        }
                        if (!minimumTimeAchieved) {
                            activity.showToastPopup("This car is available for rent for at least " + carAvailability.min_trip_dur.toInt() + " hours which are not available today. So select another day or time to continue")
                            return@setOnClickListener
                        }
                    }



                    if (model.pickUpDateServer == model.dropOffDateServer) {
                        activity.showToastPopup(activity.getString(R.string.same_time_err_msg))
                        return@setOnClickListener
                    }
                    val dateFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                    if (dateFormat.parse(model.comparePickupDate)!!
                            .after(dateFormat.parse(model.compareDropOffDate))
                    ) {
                        activity.showToastPopup(activity.getString(R.string.same_time_err_msg))
                        return@setOnClickListener
                    }
                    var isHostAvailable = true
                    var customAvailability: CarCustomAvailability? = null
                    if (carAvailability != null && carCustomAvailabilities.isNotEmpty()) {
                        val pickUpCustomAvailability =
                            carCustomAvailabilities.find {
                                it.dayIndex == calendarView.selectedDates[0][Calendar.DAY_OF_WEEK]
                            }
                        val dropOffCustomAvailability =
                            carCustomAvailabilities.find {
                                it.dayIndex == calendarView.selectedDates[calendarView.selectedDates.size - 1][Calendar.DAY_OF_WEEK]
                            }
                        if (pickUpCustomAvailability != null || dropOffCustomAvailability != null) {
                            if (pickUpCustomAvailability != null) {
                                isHostAvailable = DateUtil.checkTimeIsBetweenCustomHours(
                                    calendarView.selectedDates[0].time,
                                    pickUpCustomAvailability,
                                    model.pickUpDateServer
                                )
                                if (!isHostAvailable)
                                    customAvailability = pickUpCustomAvailability
                            }
                            if (dropOffCustomAvailability != null && isHostAvailable) {
                                isHostAvailable = DateUtil.checkTimeIsBetweenCustomHours(
                                    calendarView.selectedDates[calendarView.selectedDates.size - 1].time,
                                    dropOffCustomAvailability,
                                    model.dropOffDateServer
                                )
                                if (!isHostAvailable)
                                    customAvailability = dropOffCustomAvailability
                            }
                        }
                    }
//                    checkDatesAvailability(popUp = calendarPopup, callback, model, activity)
                    if (isHostAvailable) {
                        callback.onCalendarDateTimeSelected(model)
                        calendarPopup.dismiss()
                    } else {
                        val sdfTimeIn24Format =
                            SimpleDateFormat(
                                Constants.TIME_FORMAT_24_HOURS,
                                Locale.getDefault()
                            )
                        val sdfAmPmFormat =
                            SimpleDateFormat(Constants.AM_PM_TIME_FORMAT, Locale.getDefault())
                        val from = sdfAmPmFormat.format(
                            customAvailability?.from?.let {
                                sdfTimeIn24Format.parse(
                                    customAvailability.from
                                )
                            } ?: Date()
                        )
                        val to =
                            sdfAmPmFormat.format(
                                customAvailability?.to?.let {
                                    sdfTimeIn24Format.parse(
                                        customAvailability.from
                                    )
                                } ?: Date())
                        activity.showToastPopup("The host is available for pickup and dropoff from '${from}' to '${to}' on '${customAvailability?.day}")
                    }
                }
            } else {
                activity.showToastPopup(activity.getString(R.string.select_pickup_dropoff_date))

            }

        }

        calendarView.setOnDayClickListener(object :
            OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {

                if (calendarView.selectedDates.isEmpty()) {
                    pickUpDate?.text =
                        sdf.format(eventDay.calendar.time) + ", " + tvPickUp?.text.toString()
                    dropOffDate?.text =
                        sdf.format(eventDay.calendar.time) + ", " + tvDropOff?.text.toString()
                } else {
                    dropOffDate?.text =
                        sdf.format(eventDay.calendar.time) + ", " + tvDropOff?.text.toString()
                }
                update.postDelayed(
                    {

                        unavailableDateFound = false
                        if (!bookedDates.isNullOrEmpty()) {
                            for (i in bookedDates.indices) {
                                for (j in calendarView.selectedDates.indices) {
                                    if (bookedDates[i] == sdfServer.format(calendarView.selectedDates[j].time)) {
                                        unavailableDateFound = true
                                        break
                                    }
                                    if (unavailableDateFound) {
                                        break
                                    }
                                }
                            }
                        }
                        if (unavailableDateFound) {
                            activity.showToastPopup(
                                activity.getString(R.string.dates_not_available_for_booking_err_msg)
                            )
                        }

                        if (calendarView.selectedDates.isNotEmpty()) {
                            pickUpDate?.text =
                                sdf.format(calendarView.selectedDates[0].time) + ", " + tvPickUp?.text.toString()
                            dropOffDate?.text =
                                sdf.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + ", " + tvDropOff?.text.toString()
                        }
                        if (calendarView.selectedDates.size == 1) {
                            if (sdfServer.format(calendarView.selectedDates[0].time) == uiHelper.currentDate) {
                                if (currentHour + 1 + noticeTime < 24) {
                                    seekBarStart?.seekBar?.progress =
                                        (currentHour + 1 + noticeTime) * 2
//                                            seekBarStart?.setThumbText((currentHour + 1 + noticeTime) * 2)
                                } else {
                                    seekBarStart?.seekBar?.progress =
                                        (currentHour + 1) * 2
//                                            seekBarStart?.setThumbText((currentHour + 1) * 2)
                                }
                            }
                            if (sdfServer.format(calendarView.selectedDates[0].time) != uiHelper.currentDate) {
                                if (carAvailability != null) {
                                    seekBarStart?.seekBar?.progress =
                                        (currentHour + 1) * 2
//                                            seekBarStart?.setThumbText((currentHour + 1) * 2)
                                }
                            }
                            if (calendarView.selectedDates.size > 1 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) == uiHelper.currentDate
                            ) {
                                if (currentHour + 1 + noticeTime < 24) {
                                    seekBarStart?.seekBar?.progress =
                                        (currentHour + 1) * 2
//                                            seekBarStart?.setThumbText((currentHour + 1) * 2)
                                } else {
                                    seekBarStart?.seekBar?.progress =
                                        (currentHour + 1) * 2
//                                            seekBarStart?.setThumbText((currentHour + 1) * 2)
                                }
                            }
                            if (carAvailability != null) {
                                if (sdfServer.format(calendarView.selectedDates[0].time) == model.minimumDateOnlyDefault) {
                                    seekBarStart?.let {
                                        seekBarStart.seekBar?.progress?.let { it1 ->
                                            setCarBasicLimits(
                                                calendarView = calendarView,
                                                progress = it1,
                                                sdfServer = sdfServer,
                                                seekBarStart = it,
                                                model = model,
                                                seekBarEnd = seekBarEnd,
                                                carAvailability = carAvailability,
                                                bookedDates = bookedDates,
                                                tvDropOff = tvDropOff,
                                                tvPickUp = tvPickUp,
                                                dropOffDate = dropOffDate,
                                                sdf = sdf,
                                                activity = activity
                                            )
                                        }
                                    }

                                } else if (seekBarEnd!!.seekBar.progress < seekBarStart!!.seekBar.progress + (carAvailability.min_trip_dur.toInt() * 2)) {
                                    seekBarEnd.seekBar.progress =
                                        seekBarStart.seekBar.progress + (carAvailability.min_trip_dur.toInt() * 2)
//                                            seekBarEnd.setThumbText(seekBarStart.seekBar.progress + (carAvailability.min_trip_dur.name.toInt() * 2))
                                }
                            }
                        }
                        if (calendarView.selectedDates.size > 1) {
                            if (calendarView.selectedDates.size > 1 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) == uiHelper.currentDate
                            ) {
                                if (currentHour + 1 + noticeTime < 24) {
                                    seekBarStart?.seekBar?.progress =
                                        (currentHour + 1) * 2
//                                            seekBarStart?.setThumbText((currentHour + 1) * 2)
                                } else {
                                    seekBarStart?.seekBar?.progress =
                                        (currentHour + 1) * 2
//                                            seekBarStart?.setThumbText((currentHour + 1) * 2)
                                }
                            }
                            if (carAvailability != null) {
                                if (seekBarEnd!!.seekBar.progress < seekBarStart!!.seekBar.progress + (carAvailability.min_trip_dur.toInt() * 2)
                                ) {
                                    if (sdfServer.format(
                                            calendarView.selectedDates[calendarView.selectedDates.size - 1].time
                                        ) == model.dropOffDateComplete
                                    ) {
                                        val timeFormatOther =
                                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                        val dateMinDuration =
                                            timeFormatOther.parse(model.minimumDurationDateTime)
                                        val minCalendar = Calendar.getInstance()
                                        minCalendar.time = dateMinDuration!!
                                        val minDurationHour =
                                            minCalendar[Calendar.HOUR_OF_DAY] * 2
                                        seekBarEnd.seekBar.progress = minDurationHour
//                                                seekBarEnd.setThumbText(minDurationHour)

                                    }
                                }
                            }
                        }
                    }, 500
                )

            }
        })

        seekBarEnd?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (calendarView.selectedDates.isNotEmpty()) {
                    tvDropOff?.text = calculateTime(progress)
//                dropOffTime?.text = calculateTime(progress)
                    performVibration(activity)
                    dropOffDate?.text =
                        sdf.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + ", " + tvDropOff?.text.toString()
//                seekBarEnd.setThumbText(progress)
                    val timeFormatConversion = SimpleDateFormat("HH:mm:ss")
                    val timeFormatOther = SimpleDateFormat("hh:mm a")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    if (carAvailability != null) {
                        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        if (calendarView.selectedDates.isNotEmpty()) {
                            val calendar = Calendar.getInstance()
                            calendar.time = timeFormat.parse(model.minimumDurationDateTime)!!
                            val date =
                                sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time)
                            val dateMinDuration = timeFormat.parse(model.minimumDurationDateTime)
                            if (date == sdfServer.format(dateMinDuration!!)) {
                                val minCalendar = Calendar.getInstance()
                                minCalendar.time = dateMinDuration
                                val minDurationHour = minCalendar[Calendar.HOUR_OF_DAY] * 2
                                if (progress < minDurationHour) {
                                    seekBarEnd.seekBar.progress = minDurationHour
                                    tvDropOff?.text = calculateTime(minDurationHour)
                                    dropOffDate?.text =
                                        sdf.format(minCalendar.time) + ", " + calculateTime(
                                            minDurationHour
                                        )
//                                seekBarEnd.setThumbText(minDurationHour)
                                }
                            }
                            if (calendarView.selectedDates.size == 1 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) != model.pickUpDateComplete
                            ) {
                                val progressMin = carAvailability.min_trip_dur.toInt() * 2
                                val calculation = seekBarStart!!.seekBar.progress + progressMin
                                if (progress < calculation) {
                                    seekBarEnd.seekBar.progress = calculation
                                    dropOffDate?.text = sdf.format(
                                        calendarView.selectedDates[calendarView.selectedDates.size - 1].time
                                            ?: ""
                                    ) + ", " + calculateTime(calculation)
//                                seekBarEnd.setThumbText(calculation)
                                    return
                                }
                            }
                            if (calendarView.selectedDates.size == 2 && sdfServer.format(
                                    calendarView.selectedDates[0].time
                                ) != model.pickUpDateComplete
                            ) {
                                val progressMin = carAvailability.min_trip_dur.toInt()
                                val calendar = Calendar.getInstance()

                                val time = timeFormatConversion.format(
                                    timeFormatOther.parse(tvPickUp?.text.toString())!!
                                )
                                calendar.time =
                                    timeFormat.parse("" + sdfServer.format(calendarView.selectedDates[0].time) + " " + time)!!
                                calendar.add(
                                    Calendar.HOUR,
                                    progressMin
                                )

                                val dropOffHour = calendar[Calendar.HOUR_OF_DAY] * 2
                                val dropOffTimeCheck = Calendar.getInstance()
                                val dropOffDateTime =
                                    dateFormat.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + " " + timeFormatConversion.format(
                                        timeFormatOther.parse(tvDropOff?.text.toString())!!
                                    )
                                dropOffTimeCheck.time = timeFormat.parse(dropOffDateTime)!!
                                if (dropOffTimeCheck.time.before(calendar.time)) {
                                    seekBarEnd.seekBar.progress = dropOffHour
//                                seekBarEnd.setThumbText(dropOffHour)
                                }
                            }
                        }

                    }

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        seekBarStart?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                tvPickUp?.text = calculateTime(progress)
                pickUpDate?.text =
                    pickUpDate?.text.toString()
                        .split(",")[0].trim() + ", " + pickUpDate?.text.toString()
                        .split(",")[1].trim() + ", " + tvPickUp?.text.toString()
                performVibration(activity)
                if (carAvailability != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        setCarBasicLimits(
                            calendarView = calendarView,
                            progress = seekBarStart.seekBar.progress,
                            sdfServer = sdfServer,
                            seekBarStart = seekBarStart,
                            model = model,
                            seekBarEnd = seekBarEnd,
                            carAvailability = carAvailability,
                            bookedDates = bookedDates,
                            tvDropOff = tvDropOff,
                            tvPickUp = tvPickUp,
                            dropOffDate = dropOffDate,
                            sdf = sdf,
                            activity = activity
                        )
                    }, 200)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        if (carAvailability != null) {
            if (calendarView.selectedDates.size == 1) {
                seekBarEnd?.seekBar?.progress = 0 + carAvailability.min_trip_dur.toInt() * 2
//                seekBarStart?.setThumbText(carAvailability.min_trip_dur.name.toInt() * 2)

            }

            noticeTime = (NumberFormat.getInstance()
                .parse(carAvailability.adv_notice_trip_start) as Number).toInt()
            advanceNoticeDateCheck.add(Calendar.HOUR, noticeTime)
        }
//        reset?.setOnClickListener {
//            model.minimumDateTime = model.minimumDateTimeDefault
//            model.minimumDurationDateTime = model.minimumDurationDateTimeDefault
//            model.minimumDateOnly = model.minimumDateOnlyDefault
//            setDefaultDateTime(
//                model,
//                pickUpDate,
//                dropOffDate,
//                calendarView,
//                tvPickUp,
//                tvDropOff,
//                seekBarStart,
//                seekBarEnd
//            )
//        }
        try {
            calendarPopup.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun performVibration(activity: Activity) {
// Vibrate for 500 milliseconds
        // Vibrate for 500 milliseconds
        val v: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(50)
        }
    }

    /**
     * Function for setting calendar popup screen default date and time
     * */
    @SuppressLint("SimpleDateFormat")
    private fun setDefaultDateTime(
        model: CalendarDateTimeModel,
        pickUpDate: TextView?,
        dropOffDate: TextView?,
        calendarView: CalendarView,
        tvPickUp: TextView?,
        tvDropOff: TextView?,
        seekBarStart: ThumbTextSeekBar?,
        seekBarEnd: ThumbTextSeekBar?
    ) {
        Log.e("setDefaultDateTime: ", "${calendarView.selectedDates.size}")
        val sdfServer = SimpleDateFormat("yyyy-MM-dd")
        val sdf = SimpleDateFormat("EEE, MMM dd")
        val calendars: MutableList<Calendar> = ArrayList()
        val date = sdfServer.parse(model.pickUpDateServer)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        val date2 = sdfServer.parse(model.dropOffDateServer)
        val cal2 = Calendar.getInstance()
        cal2.time = date2!!
//            calendarView.scrollToDate(date)
        Log.e("setDefaultDateTime", "${cal2.time}")
        while (calendar.time.before(date2)) {
            val calendarToAdd = Calendar.getInstance()
            calendarToAdd.time = calendar.time
            calendars.add(calendarToAdd)
            calendar.add(Calendar.DATE, 1)
        }
        val calendarToAdd = Calendar.getInstance()
        calendarToAdd.time = cal2.time
        calendars.add(calendarToAdd)
        calendarView.selectedDates = ArrayList()
        calendarView.selectedDates = calendars
        seekBarStart?.seekBar?.progress = 0
        seekBarEnd?.seekBar?.progress = 0
        pickUpDate?.text = sdf.format(calendar.time) + model.pickupTime
        dropOffDate?.text =
            sdf.format(cal2.time) + model.dropOffTime
        val pickUpHour = Calendar.getInstance()
        val DropOffHour = Calendar.getInstance()
        val timeFormatNew = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        pickUpHour.time = timeFormatNew.parse(model.pickUpDateServer)!!
        DropOffHour.time = timeFormatNew.parse(model.dropOffDateServer)!!
        var pickupHour = pickUpHour[Calendar.HOUR_OF_DAY] * 2
        var dropOffHour = DropOffHour[Calendar.HOUR_OF_DAY] * 2
        if (pickUpHour[Calendar.MINUTE] == 30) {
            pickupHour += 1
        }
        if (DropOffHour[Calendar.MINUTE] == 30) {
            dropOffHour += 1
        }

        seekBarStart?.seekBar?.progress = pickupHour
//            seekBarStart?.setThumbText(pickupHour)
        seekBarEnd?.seekBar?.progress = dropOffHour
//            seekBarEnd?.setThumbText(dropOffHour)


        pickUpDate?.text =
            sdf.format(calendar.time) + ", " + calculateTime(pickupHour)
        dropOffDate?.text =
            sdf.format(cal2.time) + ", " + calculateTime(
                dropOffHour
            )
        tvPickUp?.text = calculateTime(pickupHour)
        tvDropOff?.text = calculateTime(dropOffHour)


    }

    /**
     * Function for setting car basic limits
     * */
    @SuppressLint("SimpleDateFormat")
    private fun setCarBasicLimits(
        calendarView: CalendarView,
        progress: Int,
        sdfServer: SimpleDateFormat,
        seekBarStart: ThumbTextSeekBar,
        model: CalendarDateTimeModel,
        seekBarEnd: ThumbTextSeekBar?,
        carAvailability: CarAvailabilityModel,
        bookedDates: ArrayList<String>,
        tvDropOff: TextView?,
        tvPickUp: TextView?,
        dropOffDate: TextView?,
        sdf: SimpleDateFormat,
        activity: Activity
    ) {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timeFormatOther = SimpleDateFormat("hh:mm a")
        val timeFormatConversion = SimpleDateFormat("HH:mm:ss")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        if (calendarView.selectedDates.isNotEmpty()) {
            val date = sdfServer.format(calendarView.selectedDates[0].time)
            val dateMinDuration = timeFormat.parse(model.minimumDateTime)
            if (date == sdfServer.format(dateMinDuration!!)) {
                val minCalendar = Calendar.getInstance()
                minCalendar.time = dateMinDuration
                val minDurationHour = minCalendar[Calendar.HOUR_OF_DAY] * 2
                if (progress < minDurationHour) {
                    seekBarStart.seekBar.progress = minDurationHour
//                    seekBarStart.setThumbText(minDurationHour)
                } else if (progress >= minDurationHour && (sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) == model.dropOffDateComplete || sdfServer.parse(
                        sdfServer.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time)
                    )!!.after(sdfServer.parse(model.dropOffDateServer)) || sdfServer.format(
                        calendarView.selectedDates[calendarView.selectedDates.size - 1].time
                    ) == model.minimumDateOnlyDefault)
                ) {
                    //--------------------Add minimum Duration Period in Date (Calculated By Pickup date + Pickup Time)
                    val calendar = Calendar.getInstance()
                    val time = timeFormatConversion.format(
                        timeFormatOther.parse(tvPickUp?.text.toString())!!
                    )
                    calendar.time =
                        timeFormat.parse("" + sdfServer.format(calendarView.selectedDates[0].time) + " " + time)!!
                    calendar.add(
                        Calendar.HOUR,
                        carAvailability.min_trip_dur.toInt()
                    )
                    val dropOffHour = calendar[Calendar.HOUR_OF_DAY] * 2
                    model.minimumDurationDateTime = timeFormat.format(calendar.time)
                    val calendarSet = Calendar.getInstance()
                    calendarSet.time = timeFormat.parse(model.minimumDurationDateTime)!!
                    val dropOffTimeCheck = Calendar.getInstance()
                    val dropOffDateTime =
                        dateFormat.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + " " + timeFormatConversion.format(
                            timeFormatOther.parse(tvDropOff?.text.toString())!!
                        )
                    dropOffTimeCheck.time = timeFormat.parse(dropOffDateTime)!!
                    if (calendarView.selectedDates[calendarView.selectedDates.size - 1].time.before(
                            calendarSet.time
                        )
                    ) {
                        val dateMinDurationOther =
                            timeFormat.parse(model.minimumDurationDateTime)
                        val minCalendarCheck = Calendar.getInstance()
                        minCalendarCheck.time = dateMinDurationOther!!

                        if (dropOffTimeCheck.time.after(minCalendarCheck.time)) {
                            Log.e("Seek bar End", "After Time")
                            return
                        }
                        tvDropOff?.text = calculateTime(dropOffHour)

                        seekBarEnd?.seekBar?.progress = dropOffHour
//                        seekBarEnd?.setThumbText(dropOffHour)

                    }

                    //-------------------------Check If Calendar does not have nex date added because of minimum duration then add it
                    var dateFound = false
                    val formatDate = sdfServer.format(calendar.time)
                    for (i in calendarView.selectedDates.indices) {
                        if (formatDate == sdfServer.format(calendarView.selectedDates[i].time)) {
                            dateFound = true
                            break
                        }
                    }
                    if (!dateFound && sdfServer.format(
                            calendar.time
                        ) != sdfServer.format(calendarView.selectedDates[0].time)
                    ) {
                        val listOther: MutableList<Calendar> =
                            calendarView.selectedDates.toMutableList()
                        calendarView.selectedDates = ArrayList()
                        listOther.add(calendar)
                        calendarView.selectedDates = listOther
                        Handler(Looper.getMainLooper()).postDelayed({
                            dropOffDate?.text =
                                sdf.format(calendar.time) + ", " + tvDropOff?.text.toString()
                            unavailableDateFound = false
                            if (bookedDates.isNotEmpty()) {
                                for (i in bookedDates.indices) {
                                    for (j in calendarView.selectedDates.indices) {
                                        if (bookedDates[i] == sdfServer.format(
                                                calendarView.selectedDates[j].time
                                            )
                                        ) {
                                            unavailableDateFound = true
                                            break
                                        }
                                        if (unavailableDateFound) {
                                            break
                                        }
                                    }
                                }
                            }
                            if (unavailableDateFound) {
                                activity.showToastPopup(
                                    activity.getString(R.string.dates_not_available_for_booking_err_msg)
                                )
                            }

                        }, 500)
                    }
                }

            }
            if (calendarView.selectedDates.isNotEmpty() && sdfServer.format(calendarView.selectedDates[0].time) != model.pickUpDateComplete) {
                val progressMin = carAvailability.min_trip_dur.toInt()
                val calendar = Calendar.getInstance()

                val time = timeFormatConversion.format(
                    timeFormatOther.parse(tvPickUp?.text.toString())!!
                )
                calendar.time =
                    timeFormat.parse("" + sdfServer.format(calendarView.selectedDates[0].time) + " " + time)!!
                calendar.add(
                    Calendar.HOUR,
                    progressMin
                )

                Log.e(
                    "setCarBasicLimits:", "${
                        dateFormat.parse(
                            dateFormat.format(calendar.time)
                        )
                    }"
                )

                val dropOffHour = calendar[Calendar.HOUR_OF_DAY] * 2
                val dropOffTimeCheck = Calendar.getInstance()
                val dropOffDateTime =
                    dateFormat.format(calendarView.selectedDates[calendarView.selectedDates.size - 1].time) + " " + timeFormatConversion.format(
                        timeFormatOther.parse(tvDropOff?.text.toString())!!
                    )
                dropOffTimeCheck.time = timeFormat.parse(dropOffDateTime)!!

                if (dropOffTimeCheck.time.before(calendar.time)) {
                    tvDropOff?.text = calculateTime(dropOffHour)
                    seekBarEnd?.seekBar?.progress = dropOffHour
//                    seekBarEnd?.setThumbText(dropOffHour)
                }

                //-------------------------Check If Calendar does not have nex date added because of minimum duration then add it
                var dateFound = false
                val formatDate = sdfServer.format(calendar.time)
                for (i in calendarView.selectedDates.indices) {
                    if (formatDate == sdfServer.format(calendarView.selectedDates[i].time)) {
                        dateFound = true
                        break
                    }
                }

                if (!dateFound && sdfServer.format(
                        calendar.time
                    ) != sdfServer.format(calendarView.selectedDates[0].time)
                ) {
//                        nextDateAdded = 1
                    val listOther: MutableList<Calendar> =
                        calendarView.selectedDates.toMutableList()
                    calendarView.selectedDates = ArrayList()
                    listOther.add(calendar)
                    calendarView.selectedDates = listOther
                    Handler(Looper.getMainLooper()).postDelayed({
                        dropOffDate?.text =
                            sdf.format(calendar.time) + ", " + tvDropOff?.text.toString()
                        unavailableDateFound = false
                        if (bookedDates.isNotEmpty()) {
                            for (i in bookedDates.indices) {
                                for (j in calendarView.selectedDates.indices) {
                                    if (bookedDates[i] == sdfServer.format(
                                            calendarView.selectedDates[j].time
                                        )
                                    ) {
                                        unavailableDateFound = true
                                        break
                                    }
                                    if (unavailableDateFound) {
                                        break
                                    }
                                }
                            }
                        }
                        if (unavailableDateFound) {
                            activity.showToastPopup(
                                activity.getString(R.string.dates_not_available_for_booking_err_msg)
                            )
                        }

                    }, 500)

                }
//                if (progress >= 24 - progressMin) {
//                    seekBarStart.seekBar.progress  =24 - progressMin)
//                }
//                val newprog = progress + progressMin
//                seekBarEnd?.seekBar.progress  =newprog)
            }
        }
    }

    /**
     * Function for setting car default date time
     * */
    private fun setDefaultDateTime(
        model: CalendarDateTimeModel,
        dropOffDate: TextView?,
        calendarView: CalendarView,
        tvDropOff: TextView?,
        seekBarEnd: ThumbTextSeekBar?
    ) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val sdfServer =
                    SimpleDateFormat(Constants.ServerDateFormat, Locale.getDefault())
                val sdf = SimpleDateFormat(Constants.UIDateFormat, Locale.getDefault())
                val calendars: MutableList<Calendar> = ArrayList()
                val date = sdfServer.parse(model.pickUpDateServer)
                val calendar = Calendar.getInstance()
                calendar.time = date!!
                val date2 = sdfServer.parse(model.dropOffDateServer)
                val cal2 = Calendar.getInstance()
                cal2.time = date2!!
                while (calendar.time.before(date2)) {
                    val calendarToAdd = Calendar.getInstance()
                    calendarToAdd.time = calendar.time
                    calendars.add(calendarToAdd)
                    calendar.add(Calendar.DATE, 1)
                }
                val calendarToAdd = Calendar.getInstance()
                calendarToAdd.time = cal2.time
                calendars.add(calendarToAdd)
                calendarView.selectedDates = ArrayList()
                calendarView.selectedDates = calendars
                seekBarEnd?.seekBar?.progress = 0
                val pickUpHour = Calendar.getInstance()
                val DropOffHour = Calendar.getInstance()
                val timeFormatNew =
                    SimpleDateFormat(Constants.ServerDateTimeFormat, Locale.getDefault())
                pickUpHour.time = timeFormatNew.parse(model.pickUpDateServer)!!
                DropOffHour.time = timeFormatNew.parse(model.dropOffDateServer)!!
                var pickupHour = pickUpHour[Calendar.HOUR_OF_DAY] * 2
                var dropOffHour = DropOffHour[Calendar.HOUR_OF_DAY] * 2
                if (pickUpHour[Calendar.MINUTE] == 30) {
                    pickupHour += 1
                }
                if (DropOffHour[Calendar.MINUTE] == 30) {
                    dropOffHour += 1
                }
                seekBarEnd?.seekBar?.progress = pickupHour
//                seekBarEnd?.setThumbText(pickupHour)
                tvDropOff?.text = calculateTime(pickupHour)
                dropOffDate?.text =
                    sdf.format(cal2.time) + ", " + tvDropOff?.text.toString()
            }, 200
        )
    }


    /**
     * Function for calculate time accoring to progress
     * */
    fun calculateTime(value_: Int): String? {
        val value = value_ * 0.5f
        var time: String = if (value.toString().contains(".0")) {
            "${value.toInt()}:00"
        } else {
            "${value.toString().split(".")[0]}:30"
        }
        if (value_ == 48) {
            val changedValue = 23
            time = "0$changedValue:30"
        }
        return try {
            val timeFormatMain = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateObj: Date? = timeFormatMain.parse(time)
            timeFormat.format(dateObj!!)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            time
        }

    }

    /**
     * Function for getting selected dates
     * */
    private fun getSelectedDays(): List<Calendar> {
        val calendars: MutableList<Calendar> =
            ArrayList()
        for (i in 0..1) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, i)
            calendars.add(calendar)
        }
        return calendars
    }

    /**
     * Function for getting disabled dates
     * */
    @SuppressLint("SimpleDateFormat")
    fun getDisabledDates(
        minimumDateOnlyDefault: Date,
        unAvailableDays: List<Int> = emptyList()
    ): List<Calendar> {
        val calendars: MutableList<Calendar> = ArrayList()
        val sdfServer = SimpleDateFormat("yyyy-MM-dd")
        for (i in 0..60) {
            val min = Calendar.getInstance()
            min.time = sdfServer.parse(sdfServer.format(minimumDateOnlyDefault))!!
            min.add(Calendar.DATE, i)
            if (unAvailableDays.isNotEmpty()) {
                for (j in unAvailableDays) {
                    Log.e("DateLoopLog", min.time.toString())
                    if (j == min.get(Calendar.DAY_OF_WEEK))
                        calendars.add(min)
                }
            }
        }
        return calendars
    }

    @SuppressLint("SimpleDateFormat")
    fun getPreviousDisabledDates(
        minimumDateOnlyDefault: Date,
    ): List<Calendar> {
        val calendars: MutableList<Calendar> = ArrayList()
        val sdfServer = SimpleDateFormat("yyyy-MM-dd")
        Log.e("getPreviousDisabledDates: ", "")
        for (i in -1 downTo -30) {
            val min = Calendar.getInstance()
            min.time = sdfServer.parse(sdfServer.format(minimumDateOnlyDefault))!!
            min.add(Calendar.DATE, i)
            calendars.add(min)
        }
        return calendars
    }

    /**
     * Function for showing guidlines by host popup
     * */
    @SuppressLint("SimpleDateFormat")
    fun showGuideLinesByHost(
        activity: Activity,
        model: String
    ) {
        val calendarPopup: Dialog = Dialog(activity, R.style.full_screen_dialog)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.setContentView(R.layout.popup_host_guidlines)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val crossBtn: ImageButton? = calendarPopup.findViewById(R.id.cross)
        val guideLines: TextView? = calendarPopup.findViewById(R.id.guidlines)
        guideLines?.text = model
        guideLines?.movementMethod = ScrollingMovementMethod()
        crossBtn?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            calendarPopup.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Function for showing report listing error
     * */
    @SuppressLint("SimpleDateFormat")
    fun showReportListingError(
        activity: Activity,
        callBack: PopupCallback,
        model: ArrayList<ReportReasonDetails>
    ) {
        var id = 0
        val calendarPopup = Dialog(activity, R.style.full_screen_dialog_2)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.setContentView(R.layout.popup_report_listing)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val crossBtn: ImageView? = calendarPopup.findViewById(R.id.ivArrowBack)
        val save: Button? = calendarPopup.findViewById(R.id.report)
        val etReason: EditText? = calendarPopup.findViewById(R.id.etReason)
        val llReason: RelativeLayout? = calendarPopup.findViewById(R.id.llReason)
        val recyclerView: RecyclerView? = calendarPopup.findViewById(R.id.recyclerView)
        val adapter = SelectReasonOfReportAdapter(calendarPopup.context,
            uiHelper,
            model
        ){ reasonId ->
            Log.d("reasonId" , reasonId.toString())
            etReason?.setText("")
            llReason?.isVisible = reasonId == 6
            id = reasonId
        }
        recyclerView?.adapter = adapter

        crossBtn?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        save?.setOnClickListener {
            try {
                if (id == 6){
                    if (etReason?.text.toString().isNotEmpty()) {
                        callBack.popupButtonClick(id, etReason?.text.toString())
                        calendarPopup.dismiss()
                    }else{
                        uiHelper.showLongToastInCenter(
                            activity,
                            activity.getString(R.string.select_reasons)
                        )
                    }
                }else{
                    callBack.popupButtonClick(id , etReason?.text.toString())
                    calendarPopup.dismiss()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * Function for showing cancellation policy popup
     * */
    @SuppressLint("SimpleDateFormat")
    fun showCancellationPolicy(
        activity: Activity
    ) {
        val calendarPopup: Dialog? = Dialog(activity, R.style.full_screen_dialog)
        calendarPopup?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup?.setCancelable(false)
        calendarPopup?.setContentView(R.layout.popup_cancellation_policy)
        calendarPopup?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val crossBtn: ImageButton? = calendarPopup?.findViewById(R.id.cross)
        val guideLines: TextView? = calendarPopup?.findViewById(R.id.guidlines)
        guideLines?.movementMethod = ScrollingMovementMethod()
        crossBtn?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * Function for getting user information
     * */
    fun getUserInfo(
        userId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getUserInfo(userId),
            tag,
            callback,
            showLoader
        )
    }

     fun getReportReason(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ){
        dataRepository.callApi(
            dataRepository.network.apis()!!.getReportReason(),
            tag,
            callback,
            showLoader
        )
    }


    /**
     * Function for cancelling booking
     * */
    fun saveReportReason(
        reportId: Int,
        carId: Int,
        cancelReason: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveReportReason(reportId, carId , cancelReason),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for checking dates availability
     * */
    private fun checkDatesAvailability(
        popUp: Dialog,
        callback: CalendarCallback,
        model: CalendarDateTimeModel,
        activity: Activity
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!
                .validateCalendar(model.carId, model.pickUpDateServer, model.dropOffDateServer),
            Constants.API.CALENDAR_VALIDATOR,
            object : OnNetworkResponse {
                override fun onSuccess(response: BaseResponse?, tag: Any?) {
                    val responseModel = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        CalendarValidateResponseModel::class.java
                    )
                    if (responseModel.availability) {
                        callback.onCalendarDateTimeSelected(model)
                        popUp.dismiss()
                    } else {
                        activity.showToastPopup(response?.message.toString())
                    }
                }

                override fun onFailure(response: BaseResponse?, tag: Any?) {
                    activity.showToastPopup(response?.message.toString())
                }

            },
            true
        )
    }
}