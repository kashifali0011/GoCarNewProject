package com.towsal.towsal.utils

import android.util.Log
import com.towsal.towsal.network.serializer.cardetails.CarCustomAvailability
import com.towsal.towsal.utils.Constants.ServerDateFormat
import com.towsal.towsal.utils.Constants.ServerDateTimeFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Class for handling all types of functions that we need in the project to perform on dates
 * */
object DateUtil {

    /**
     * Function for getting local date from utc
     * */
    fun dateFromUTC(date: Date): Date {
        return Date(date.time + Calendar.getInstance().timeZone.getOffset(date.time))
    }

    /**
     * Function for getting date in utc
     * */
    fun dateToUTC(date: Date): Date {
        return Date(date.time - Calendar.getInstance().timeZone.getOffset(date.time))
    }

    /**
     * Function for getting string date from stringDate which is in utc
     * */
    fun getDateInUtc(ourDate: String): String {
        var ourCopyDate = ourDate
        try {
            val formatter = SimpleDateFormat(ServerDateTimeFormat, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val value = formatter.parse(ourCopyDate)
            ourCopyDate = formatter.format(dateToUTC(value))
        } catch (e: Exception) {
            ourCopyDate = "00-00-0000 00:00"
        }

        return ourCopyDate
    }

    /**
     * Function for getting string date
     * */
    fun getDateInString(OurDate: Date): String {
        var stringDate = "0000-00-00T00:00:00"
        stringDate = try {
            val formatter = SimpleDateFormat("vyyy-MM-dd'T'HH:mm:ss")
            formatter.format(dateToUTC(OurDate))
        } catch (e: Exception) {
            "0000-00-00T00:00:00"
        }

        return stringDate
    }

    /**
     * Function for getting string date
     * */
    fun SendDateToDate(OurDate: String): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm a")
            val value = formatter.parse(OurDate)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") //this format changeable
            val formattedDate = dateFormatter.format(value)
            formattedDate
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Function for getting date
     * */
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss") // val format = SimpleDateFormat("HH:mm:ss")
        return format.format(date)
    }

    /**
     * Function for getting current date in time milliseconds
     * */
    fun currentTimeToLong(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Function for getting time milliseconds
     * */
    fun convertDateToLong(date: String): Long {
        var mDate = getLocalDate(date)
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        var startDate = df.parse(mDate)
        val strCurrentDate = df.format(Calendar.getInstance().time)
        var currentDate = df.parse(strCurrentDate)
        val diff = currentDate.time - startDate.time
        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Function for getting date in time milliseconds
     * */
    fun convertDateToLongForward(date: String): Long {
        var mDate = getLocalDate(date)
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        var startDate = df.parse(mDate)
        return startDate.time
    }

    /**
     * Function for getting year
     * */
    fun getYear(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2020)
        return cal.get(Calendar.YEAR)
    }

    /**
     * Function for converting date
     * */
    fun convertDate(startDate: String, endDate: String): String {
        var finalDate: String? = null
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val currentDate = df.format(Calendar.getInstance().time)
        val splitedStartDate =
            startDate.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val subSplitedStartDate =
            splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val splitedEndDate =
            endDate.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val subSplitedndDate =
            splitedEndDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val time1 = convertIn12HourFormat(startDate)
        val time2 = convertIn12HourFormat(endDate)
        if (splitedStartDate[0].equals(splitedEndDate[0], ignoreCase = true)) {
            finalDate = when {
                isToday(startDate) -> "Today $time1 to $time2"
                isYesterday(startDate) -> "Yesterday $time1 to $time2"
                isTomorrow(startDate) -> "Tomorrow $time1 to $time2"
                getDaysDifference(
                    startDate,
                    currentDate
                ) in 0..5 -> getDayName(startDate) + ", " + time1 + " to " + time2
                else -> getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0] + " - " + time1 + " Between " + time2
            }
        } else {
            when {
                isToday(startDate) -> {
                    finalDate = "Today $time1"
                    finalDate = when {
                        isTomorrow(endDate) -> "$finalDate - Tomorrow $time2"
                        getDaysDifference(
                            endDate,
                            currentDate
                        ) in 0..5 -> finalDate + " - " + getDayName(endDate) + " " + time2
                        else -> finalDate + " - " + getMonthNameByValue(
                            Integer.parseInt(
                                subSplitedndDate[1]
                            )
                        ) + " " + subSplitedndDate[2] + ", " + subSplitedndDate[0] + "  " + time1
                    }
                }
                isTomorrow(startDate) -> {
                    finalDate = "Tomorrow $time1"
                    finalDate = when {
                        isTomorrow(endDate) -> "$finalDate - Tomorrow $time2"
                        getDaysDifference(
                            endDate,
                            currentDate
                        ) in 0..5 -> finalDate + " - " + getDayName(endDate) + " " + time2
                        else -> finalDate + " - " + getMonthNameByValue(
                            Integer.parseInt(
                                subSplitedndDate[1]
                            )
                        ) + " " + subSplitedndDate[2] + ", " + subSplitedndDate[0] + "  " + time1
                    }
                }
                isYesterday(startDate) -> {
                    finalDate = "Yesterday $time1"
                    finalDate = when {
                        isToday(endDate) -> "$finalDate - Today $time2"
                        isTomorrow(endDate) -> "$finalDate - Tomorrow $time2"
                        getDaysDifference(
                            endDate,
                            currentDate
                        ) in 0..5 -> finalDate + " - " + getDayName(endDate) + " " + time2
                        else -> finalDate + " - " + getMonthNameByValue(
                            Integer.parseInt(
                                subSplitedndDate[1]
                            )
                        ) + " " + subSplitedndDate[2] + ", " + subSplitedndDate[0] + "  " + time1
                    }
                }
                getDaysDifference(startDate, endDate) in 0..5 -> finalDate =
                    getDayName(startDate) + " " + time1 + " - " + getDayName(endDate) + " " + time2
                else -> finalDate =
                    getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0] + "  " + time1 + " - " + getMonthNameByValue(
                        Integer.parseInt(subSplitedndDate[1])
                    ) + " " + subSplitedndDate[2] + ", " + subSplitedndDate[0] + " " + time2
            }
        }
        return finalDate
    }

    /**
     * Function for converting single date and time
     * */
    fun convertSingleDateAndTime(startDate: String?): String {
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)
            getMinutesDifference(getLocalDate(startDate), currentDate)
            val splitedStartDate =
                getLocalDate(startDate).split("T".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(getLocalDate(startDate))
            getDayName(startDate) + ", " + getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0] + " \u00B7 " + time1
        } else {
            " "
        }
    }

    /**
     * Function for converting notification date
     * */
    fun convertSingleDateNotification(startDate: String): String {
        val startDate = getLocalDate(startDate)
        var finalDate: String? = null
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val currentDate = df.format(Calendar.getInstance().time)
        getMinutesDifference(startDate, currentDate)
        val splitedStartDate =
            startDate.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val subSplitedStartDate =
            splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val time1 = convertIn12HourFormatWithMinute(startDate)
        finalDate = when {
            isToday(startDate) -> when {
                getMinutesDifference(startDate, currentDate) < 1 -> "Just Now"
                getMinutesDifference(startDate, currentDate) < 60 -> "${
                    getMinutesDifference(startDate, currentDate)
                }min ago"
                else -> "${getHoursDifference(startDate, currentDate)}h ago"
            }
            isYesterday(startDate) -> "Yesterday $time1"
            isTomorrow(startDate) -> "Tomorrow $time1"
            getDaysDifference(startDate, currentDate) in 0..7 -> getDaysDifference(
                startDate,
                currentDate
            ).toString() + " days ago"
            else -> getDayName(startDate) + ", " + getMonthNameByValue(
                Integer.parseInt(
                    subSplitedStartDate[1]
                )
            ) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0] + " \u00B7 " + time1
        }
        return finalDate
    }

    /**
     * Function for converting single date
     * */
    fun convertSingleDate(startDate: String?): String {
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)

            getMinutesDifference(startDate, currentDate)
            val splitedStartDate =
                startDate.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(startDate)
            getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])).lowercase(Locale.getDefault()) + " " + subSplitedStartDate[2].replaceFirst(
                "0",
                " "
            ) + ", " + subSplitedStartDate[0]
        } else {
            " "
        }
    }

    /**
     * Function for converting single date and time
     * */
    fun convertSingleDateTimePicker(startDate: String?): String {
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)

            //    getMinutesDifference(getLocalDate(startDate), currentDate)
            val splitedStartDate =
                startDate.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray() //    val time1 = convertIn12HourFormatWithMinute(startDate)
            getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0]
        } else {
            " "
        }
    }

    /**
     * Function for converting date to time
     * */
    fun convertSingleTimePicker(startDate: String?): String {
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)
            val splitedStartDate =
                startDate.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(startDate)
            time1
        } else {
            " "
        }
    }

    /**
     * Function for converting date without year
     * */
    fun convertSingleDateWithoutYear(startDate: String?): String {
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)

            getMinutesDifference(getLocalDate(startDate), currentDate)
            val splitedStartDate =
                getLocalDate(startDate).split("T".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(getLocalDate(startDate))
            getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2]
        } else {
            " "
        }
    }

    /**
     * Function for getting date
     * */
    fun convertSingleDateWithYear(startDate: String?): String {
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)
            getMinutesDifference(getLocalDate(startDate), currentDate)
            val splitedStartDate =
                getLocalDate(startDate).split("T".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(getLocalDate(startDate))
            getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0]
        } else {
            " "
        }
    }

    /**
     * Function for getting date without time
     * */
    fun convertDateWithoutTime(startDate: String?): String {

        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)
            getMinutesDifference(getLocalDate(startDate), currentDate)
            val splitedStartDate =
                getLocalDate(startDate).split("T".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(getLocalDate(startDate))
            getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0]
        } else {
            " "
        }
    }

    /**
     * Function for getting time
     * */
    fun convertSingleTime(startDate: String?): String {
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)
            getMinutesDifference(startDate, currentDate)
            val splitedStartDate =
                startDate.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(startDate)
            time1
        } else {
            " "
        }
    }

    /**
     * Function for getting month name
     * */
    private fun getMonthNameByValue(position: Int): String {
        return when (position - 1) {
            Calendar.JANUARY -> "Jan"
            Calendar.FEBRUARY -> "Feb"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Apr"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Aug"
            Calendar.SEPTEMBER -> "Sep"
            Calendar.OCTOBER -> "Oct"
            Calendar.NOVEMBER -> "Nov"
            Calendar.DECEMBER -> "Dec"
            else -> ""
        }
    }

    /**
     * Function for getting month full names
     * */
    private fun getMonthFullNameByValue(position: Int): String {
        return when (position - 1) {
            Calendar.JANUARY -> "JANUARY"
            Calendar.FEBRUARY -> "FEBRUARY"
            Calendar.MARCH -> "MARCH"
            Calendar.APRIL -> "APRIL"
            Calendar.MAY -> "MAY"
            Calendar.JUNE -> "JUNE"
            Calendar.JULY -> "JULY"
            Calendar.AUGUST -> "AUGUST"
            Calendar.SEPTEMBER -> "SEPTEMBER"
            Calendar.OCTOBER -> "OCTOBER"
            Calendar.NOVEMBER -> "NOVEMBER"
            Calendar.DECEMBER -> "DECEMBER"
            else -> ""
        }
    }

    /**
     * Function for converting single date
     * */
    fun convertSingleDateDoubleLine(startDate: String?): String {
        if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val currentDate = df.format(Calendar.getInstance().time)
            getMinutesDifference(getLocalDate(startDate), currentDate)
            val splitedStartDate =
                getLocalDate(startDate).split("T".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(getLocalDate(startDate))
            finalDate = when {
                isToday(getLocalDate(startDate)) -> when {
                    getMinutesDifference(
                        getLocalDate(startDate),
                        currentDate
                    ).toInt() == 0 -> "Just Now"
                    getMinutesDifference(getLocalDate(startDate), currentDate) < 60 -> "${
                        getMinutesDifference(getLocalDate(startDate), currentDate)
                    }min ago"
                    else -> "${getHoursDifference(getLocalDate(startDate), currentDate)}h ago"
                }
                isYesterday(getLocalDate(startDate)) -> "Yesterday $time1"
                isTomorrow(getLocalDate(startDate)) -> "Tomorrow $time1"/* else if (getDaysDifference(startDate, currentDate) in 0..5) {
                    getDayName(startDate) + ", " + time1
                }*/
                else -> time1 + "\n" + getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0]
            }
            return finalDate
        } else {
            return " "
        }

    }

    /**
     * Function for getting single date to schedule
     * */
    fun convertSingleDateSchedule(startDate: String?): String {
        var startDate = getLocalDate(startDate!!)
        return if (startDate != null) {
            var finalDate: String? = null
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val splitedStartDate =
                startDate.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val subSplitedStartDate =
                splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val time1 = convertIn12HourFormatWithMinute(startDate)
            finalDate =
                getMonthNameByValue(Integer.parseInt(subSplitedStartDate[1])) + " " + subSplitedStartDate[2] + ", " + subSplitedStartDate[0] + " - " + time1
            finalDate
        } else {
            " "
        }

    }

    /**
     * Function for converting notification date to time ago
     * */
    fun convertNotificationSingleDate(dataDate: String?): String? {
        var convTime: String? = null
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val pasTime = dateFormat.parse(dataDate)
            val nowTime = Date()
            val dateDiff = nowTime.time - pasTime.time
            val second = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day = TimeUnit.MILLISECONDS.toDays(dateDiff)
            if (second < 60) {
                convTime = "Just a moment ago"
            } else if (minute == 1.toLong()) {
                convTime = "1 minute ago"
            } else if (minute < 60) {
                convTime = "${minute} minutes ago"
            } else if (hour == 1.toLong()) {
                convTime = "1 hour ago"
            } else if (hour < 24) {
                convTime = "${hour} hours ago"
            } else if (day == 1.toLong()) {
                convTime = "1 day ago"
            } else if (day > 7) {
                convTime = if (day == 360.toLong()) {
                    "1 year ago"
                } else if (day > 360) {
                    (day / 360).toString() + " years ago"
                } else if (day == 30.toLong()) {
                    "1 month ago"
                } else if (day > 30) {
                    (day / 30).toString() + " months ago"
                } else {
                    (day / 7).toString() + " weeks ago"
                }
            } else if (day < 7) {
                convTime = "${day} days ago"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("ConvTimeE", e.message.toString())
        }
        return convTime
    }

    /**
     * Function for converting date in 12 hour format
     * */
    private fun convertIn12HourFormat(startTime: String): String {
        var startTime = startTime
        val splitedStartDate =
            startTime.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val date = splitedStartDate[0]
        val time = splitedStartDate[1]

        val sdf = SimpleDateFormat("HH:mm:ss")
        val sdfs = SimpleDateFormat("hh:mm a")
        val dt: Date
        try {
            dt = sdf.parse(time)
            startTime = sdfs.format(dt)
            println("Time Display: " + sdfs.format(dt)) // <-- I got result here
        } catch (e: ParseException) { // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return startTime
    }

    /**
     * Function for getting date in 12 hour format
     * */
    fun convertIn12HourFormatWithMinute(startTime: String): String {
        var startTime = startTime
        val splitedStartDate =
            startTime.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val date = splitedStartDate[0]
        val time = splitedStartDate[1]

        val sdf = SimpleDateFormat("HH:mm:ss")
        val sdfs = SimpleDateFormat("h:mm a")
        val dt: Date
        try {
            dt = sdf.parse(time)
            startTime = sdfs.format(dt)
            println("Time Display: " + sdfs.format(dt)) // <-- I got result here
        } catch (e: ParseException) { // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return startTime.replace("am", "AM").replace("pm", "PM")
    }


    /**
     * Function for getting days difference between two dates
     * */
    fun getDaysDifference(startDate: String, currentDate: String): Long {
        val diff = stringToDate(startDate)?.time!! - stringToDate(currentDate)?.time!!
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Function for getting months difference between date and current date
     * */
    fun getMonthDifference(startDate: String): Int {
        val dob = Calendar.getInstance()
        dob.time = stringToDate(startDate)!!
        val today = Calendar.getInstance()
        var monthsBetween = 0
        var dateDiff = today[Calendar.DAY_OF_MONTH] - dob[Calendar.DAY_OF_MONTH]
        if (dateDiff < 0) {
            val borrrow = today.getActualMaximum(Calendar.DAY_OF_MONTH)
            dateDiff = dob[Calendar.DAY_OF_MONTH] - today[Calendar.DAY_OF_MONTH] + borrrow
            monthsBetween--
            if (dateDiff > 0) {
                monthsBetween++
            }
        } else {
            monthsBetween++
        }
        monthsBetween += dob[Calendar.MONTH] - today[Calendar.MONTH]
        monthsBetween += (dob[Calendar.YEAR] - today[Calendar.YEAR]) * 12
        return monthsBetween
    }

    /**
     * Function for getting minutes difference
     * */
    fun getMinutesDifference(startDate: String, currentDate: String): Long {
        val diff = stringToDate(currentDate)?.time!! - stringToDate(startDate)?.time!!
        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Function for getting hours difference
     * */
    fun getHoursDifference(startDate: String, currentDate: String): Long {
        val diff = stringToDate(currentDate)?.time!! - stringToDate(startDate)?.time!!
        return TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Function for checking date is today's date
     * */
    fun isToday(date: String): Boolean {
        val d = stringToDate(date)
        return android.text.format.DateUtils.isToday(d!!.time)
    }

    /**
     * Function for checking date is yesterday's date
     * */
    private fun isYesterday(date: String): Boolean {
        val d = stringToDate(date)
        return android.text.format.DateUtils.isToday(d!!.time + android.text.format.DateUtils.DAY_IN_MILLIS)
    }

    /**
     * Function for checking date is tomorrow's date
     * */
    fun isTomorrow(date: String): Boolean {
        val d = stringToDate(date)
        return android.text.format.DateUtils.isToday(d!!.time - android.text.format.DateUtils.DAY_IN_MILLIS)
    }

    /**
     * Function for checking date is after the current date
     * */
    fun checkGreater(date: String): Boolean {
        val strDate = stringToDate(date)
        return Date().before(strDate)
    }

    /**
     * Function for getting date
     * */
    fun stringToDate(dtString: String): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = format.parse(dtString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date
    }

    /**
     * Function for getting day name
     * */
    fun getDayName(date: String): String? {
        var name: String? = null
        val outFormat = SimpleDateFormat("EEE")
        name = outFormat.format(stringToDate(date))
        return name
    }

    /**
     * Function for getting day name
     * */
    fun getDayNameWithoutTime(date: String): String? {
        var name: String? = null
        val outFormat = SimpleDateFormat("EEE")
        name = outFormat.format(stringToDateWithoutTime(date))
        return name
    }

    /**
     * Function for getting date without time
     * */
    fun stringToDateWithoutTime(dtString: String): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = null
        try {
            date = format.parse(dtString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date
    }

    /**
     * Function for local date to utc
     * */
    fun localToGMT(): String {
        val date = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    /**
     * Function for getting date
     * */
    fun getDateForPastFromDays(days: Int): String {
        var calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return dateFormat.format(dateToUTC(calendar.time))
    }

    /**
     * Function for getting past date
     * */
    fun getDateForPastFromDays(days: Int, ourDate: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val value = formatter.parse(ourDate)
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = value
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(calendar.time)
    }

    /**
     * Function for getting past date
     * */
    fun getDateForPastFromDaysChart(days: Int, ourDate: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val value = formatter.parse(ourDate)
        var calendar: Calendar = Calendar.getInstance()
        calendar.time = value
        calendar.add(Calendar.DAY_OF_YEAR, days)
        var dateFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        return dateFormat.format(calendar.time)
    }


    /**
     * Function for getting dates from minutes
     * */
    fun getDateFromMinutes(minutes: Int, ourDate: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val value = formatter.parse(ourDate)
        var calendar: Calendar = Calendar.getInstance()
        calendar.time = value
        calendar.add(Calendar.MINUTE, minutes)
        var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return dateFormat.format(calendar.time)
    }

    /**
     * Function for getting current date string
     * */
    fun getCurrentDate(): String? {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = df.format(Calendar.getInstance().time)
        return currentDate
    }

    /**
     * Function for checking for current month
     * */
    fun checkIfCurrentMonth(date2: String): Boolean {
        var currentDate = getCurrentDate()
        var myDate = date2
        if (currentDate!!.contains("T")) {
            currentDate = currentDate.split("T")[0]
        }

        if (myDate.contains("T")) {
            myDate = myDate.split("T")[0]
        }

        val currentYear = currentDate.split("-")[0]
        val currentMonth = currentDate.split("-")[1]

        val myDateYear = myDate.split("-")[0]
        val myDateMonth = myDate.split("-")[1]

        return myDateYear == currentYear && currentMonth == myDateMonth
    }

    /**
     * Function for getting local date from utc
     * */
    fun getLocalDate(OurDate: String): String {
        return try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = simpleDateFormat.parse(OurDate)
            simpleDateFormat.format(dateFromUTC(date))
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Function for checking for time remaining
     * */
    fun getDateDaysHoursDifference(date: String): String {
        val days = getDaysDifferenceComming(getLocalDate(date), getCurrentDate()!!)
        val hours = getHoursDifferenceComming(getLocalDate(date), getCurrentDate()!!) - (days * 24)
        val min = getMinutesDifferenceComming(
            getLocalDate(date),
            getCurrentDate()!!
        ) - (((days * 24) + hours) * 60)

        return when {
            days > 0 -> {
                "$days Days, $hours Hours"
            }
            hours > 0 -> {
                "$hours Hours, $min Minutes"
            }
            else -> {
                "$min Minutes"
            }
        }

    }

    /**
     * Function for checking time remaining
     * */
    fun getDateDaysHoursDifferenceDropOff(date: String): String {
        val days = getDaysDifferenceComming(date, getCurrentDate()!!)
        val hours = getHoursDifferenceComming(date, getCurrentDate()!!) - (days * 24)
        val min =
            getMinutesDifferenceComming(date, getCurrentDate()!!) - (((days * 24) + hours) * 60)

        return when {
            days > 0 -> {
                "$days Days - $hours Hours - $min Minutes"
            }
            hours > 0 -> {
                "$hours Hours - $min Minutes"
            }

            min < 0 -> {
                "$days Days $hours Hours $min Minutes minus"
            }
            else -> {
                "$min Minutes"
            }
        }

    }

    /**
     * Function for getting dates difference in days
     * */
    fun getDaysDifferenceComming(startDate: String, currentDate: String): Long {
        val diff = stringToDate(startDate)?.time!! - stringToDate(currentDate)?.time!!
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Function for getting minutes difference
     * */
    fun getMinutesDifferenceComming(startDate: String, currentDate: String): Long {
        val diff = stringToDate(startDate)?.time!! - stringToDate(currentDate)?.time!!
        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Function for getting hours difference
     * */
    fun getHoursDifferenceComming(startDate: String, currentDate: String): Long {
        val diff = stringToDate(startDate)?.time!! - stringToDate(currentDate)?.time!!
        return TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Function for converting seconds
     * */
    fun convertSeconds(seconds: Int): String {
        return String.format("%02d:%02d", (seconds % 3600) / 60, seconds % 60)
    }

    /**
     * Function for getting date without time
     * */
    fun getDateofBirthWithoutT(dob: String): String {
        val split = dob.split("T")
        return split[0]
    }

    /**
     * Function for getting week dates
     * */
    private fun getWeeksDates(weekNumber: Int, year: String): ArrayList<String> {
        val daysList = ArrayList<String>()
        val c = GregorianCalendar(Locale.getDefault())
        c.set(Calendar.WEEK_OF_YEAR, weekNumber)
        c.set(Calendar.YEAR, year.toInt())
        val firstDayOfWeek = c.firstDayOfWeek
        for (i in firstDayOfWeek until firstDayOfWeek + 7) {
            c.set(Calendar.DAY_OF_WEEK, i)
            daysList.add(SimpleDateFormat("yyyy-MM-dd").format(c.time))
        }
        return daysList
    }

    /**
     * Function for getting chart start date
     * */
    fun getChartFilterStartDate(ourDate: String, year: String): String? {
        val c = Calendar.getInstance()
        val ourDate = ourDate + " $year"
        val formatter = SimpleDateFormat("dd MMM yyyy")
        val value = formatter.parse(ourDate)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
        return dateFormatter.format(value)
    }

    /**
     * Function for getting chart end date
     * */
    fun getChartFilterEndDate(ourDate: String, year: String): String? {
        val c = Calendar.getInstance()
        val ourDate = ourDate + " $year"
        val formatter = SimpleDateFormat("dd MMM yyyy")
        val value = formatter.parse(ourDate)
        c.time = value
        c.add(Calendar.DATE, 6)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
        return dateFormatter.format(c.time)
    }

    /**
     * Function for getting week dates
     * */
    fun getChartFilterDateData(ourDate: String, year: String): ArrayList<String> {
        var list = ArrayList<String>()
        var ourDate = ourDate + " $year"
        if (getDaysDifference(
                getDateForPastFromDays(7, ourDate),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().time)
            ) > 0
        ) {
            ourDate = SimpleDateFormat("dd MMM yyyy").format(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(
                    getDateForPastFromDays(
                        -getDaysDifference(
                            getDateForPastFromDays(7, ourDate),
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().time)
                        ).toInt(), ourDate
                    )
                )
            )
        }

        val formatter = SimpleDateFormat("dd MMM yyyy")
        val value = formatter.parse(ourDate)
        for (i in 0 until 7) {
            val c = Calendar.getInstance()
            c.time = value
            c.add(Calendar.DATE, i)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
            list.add(dateFormatter.format(c.time))
        }
        return list
    }


    /**
     * Function for getting number of weeks between two dates
     * */
    fun getWeeksBetween(a: Date, b: Date): Int {
        var a = a
        var b = b
        if (b.before(a)) {
            return -getWeeksBetween(b, a)
        }
        a = resetTime(a)
        b = resetTime(b)

        val cal = GregorianCalendar()
        cal.time = a
        var weeks = 0
        while (cal.time.before(b)) {

            cal.add(Calendar.WEEK_OF_YEAR, 1)
            weeks++
        }
        return weeks
    }

    /**
     * Function for setting date's time to 00:00:00
     * */
    private fun resetTime(d: Date): Date {
        val cal = GregorianCalendar()
        cal.time = d
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    /**
     * Function for getting start and end date of month
     * */
    fun getStartAndEndDateOfMonth(month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, month)
        calendar[Calendar.DATE] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        val monthFirstDay = calendar.time
        calendar[Calendar.DATE] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthLastDay = calendar.time

        val df = SimpleDateFormat("yyyy-MM-dd")
        val startDateStr = df.format(monthFirstDay)
        val endDateStr = df.format(monthLastDay)
        Log.e("DateFirstLast", "$startDateStr $endDateStr")
        return "$startDateStr $endDateStr"
    }

    /**
     * Function for getting month
     * */
    fun convertCountToMonth(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> ""
        }
    }

    /**
     * Function for getting past 12 months
     * */
    fun getPast12Months(): ArrayList<String> {
        val pastMonthsList: ArrayList<String> = arrayListOf()
        for (i in 0 until (-(getMonthDifference("2020-01-01T00:00:00")) + 2)) {
            val calendar: Calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -i)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            pastMonthsList.add(dateFormat.format(dateToUTC(calendar.time)))
        }
        return pastMonthsList
    }

    /**
     * Function for getting month name from date
     * */
    fun getMonthNameFromDate(date: String): String {
        val splitedStartDate =
            date.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val subSplitedStartDate =
            splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return getMonthFullNameByValue(Integer.parseInt(subSplitedStartDate[1]))
    }

    /**
     * Function for getting year from date
     * */
    fun getYearFromDate(date: String): String {
        val splitedStartDate =
            date.split("T".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val subSplitedStartDate =
            splitedStartDate[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return subSplitedStartDate[0]
    }

    /**
     * Function for checking date is expired
     * */
    fun checkIfTestValid(date: String, hours: Int): Boolean {
        val expiryDate = stringToDate(getLocalDate(date))
        val calender = Calendar.getInstance()
        calender.time = expiryDate!!
        calender.add(Calendar.HOUR_OF_DAY, -hours)
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val strCurrentDate = df.format(calender.time)
        return getMinutesDifference(strCurrentDate, getCurrentDate()!!) < 0
    }

    /**
     * Function for changing the date format
     * */
    fun changeDateFormat(
        currentFormat: String,
        requiredFormat: String,
        dateString: String?
    ): String {

        if (dateString.isNullOrEmpty()) {
            return ""
        }
        var result = ""
        val formatterOld = SimpleDateFormat(currentFormat, Locale.getDefault())
        val formatterNew = SimpleDateFormat(requiredFormat, Locale.getDefault())
        var date: Date? = null
        try {
            date = formatterOld.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (date != null) {
            result = formatterNew.format(date)
        }
        Log.e("changeDateFormat", result )
        return result
    }

    /**
     * Function for checking difference between two dates is greater than 6
     * */
    fun checkDifference(fromTime: String, toTime: String): Boolean {
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val timeFrom = simpleDateFormat.parse(fromTime)
        val timeTo = simpleDateFormat.parse(toTime)
        val difference = timeTo.time - timeFrom.time
        val days = (difference / (1000 * 60 * 60 * 24))
        var hours = (difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60)
        val min =
            (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60)
        hours = if (hours < 0) -hours else hours
        return hours >= 6
    }

    /**
     * Function for filtering dates on the bases of two dates
     * */
    fun getListOfDatesExceptBetweenDates(
        startDate: String,
        endDate: String,
        unavailableDates: List<String>
    ): List<String> {
        return ArrayList(unavailableDates.filter {
            SimpleDateFormat(ServerDateFormat, Locale.getDefault()).parse(
                changeDateFormat(
                    ServerDateTimeFormat,
                    ServerDateFormat,
                    startDate
                )
            ).after(SimpleDateFormat(ServerDateFormat, Locale.getDefault()).parse(it))
                    || SimpleDateFormat(ServerDateFormat, Locale.getDefault()).parse(
                changeDateFormat(
                    ServerDateTimeFormat,
                    ServerDateFormat,
                    endDate
                )
            ).before(SimpleDateFormat(ServerDateFormat, Locale.getDefault()).parse(it))
        }.sorted())
    }

    /**
     * Function for checking any date exists between two dates
     * */
    fun checkDateFromListExistsBetweenTwoDates(
        startDate: String,
        endDate: String,
        unavailableDates: List<Calendar>
    ): Boolean {
        return ArrayList(unavailableDates.filter {
            SimpleDateFormat(ServerDateFormat).parse(
                startDate
            ).before(
                SimpleDateFormat(ServerDateFormat).parse(
                    SimpleDateFormat(
                        ServerDateFormat
                    ).format(it.time)
                )
            )
                    && SimpleDateFormat(ServerDateFormat).parse(
                endDate
            ).after(
                SimpleDateFormat(ServerDateFormat).parse(
                    SimpleDateFormat(ServerDateFormat).format(it.time)
                )
            )
        }.sorted()).isNotEmpty()
    }

    /**
     * Function for checking time is between custom hours
     * */
    fun checkTimeIsBetweenCustomHours(
        date: Date,
        pickUpCustomAvailability: CarCustomAvailability,
        dateTime: String
    ): Boolean {
        val sdfServerDateFormat = SimpleDateFormat(ServerDateFormat, Locale.getDefault())
        val sdfServerDateTimeFormat =
            SimpleDateFormat(ServerDateTimeFormat, Locale.getDefault())
        val startDateTime =
            sdfServerDateFormat.format(date) + " " + pickUpCustomAvailability.from + ":00"
        val endDateTime =
            sdfServerDateFormat.format(date) + " " + pickUpCustomAvailability.to + ":00"
        val dateToCompare = sdfServerDateTimeFormat.parse(dateTime)
        val startDate = sdfServerDateTimeFormat.parse(startDateTime)
        val endDate = sdfServerDateTimeFormat.parse(endDateTime)
        return ((startDate ?: Date()).before(dateToCompare) || (startDate
            ?: Date()) == dateToCompare)
                &&
                ((endDate ?: Date()).after(dateToCompare) || (endDate ?: Date()) == dateToCompare)
    }

    /**
     * Function for checking dates availability
     * */
    fun checkDatesAvailability(disabledDays: List<Calendar>, calendar: Calendar): Boolean =
        disabledDays.any {
            SimpleDateFormat(ServerDateFormat, Locale.getDefault()).parse(
                SimpleDateFormat(ServerDateFormat, Locale.getDefault()).format(calendar.time)
            )
                .equals(
                    SimpleDateFormat(ServerDateFormat, Locale.getDefault()).parse(
                        SimpleDateFormat(ServerDateFormat, Locale.getDefault()).format(it.time)
                    )
                )
        }

    fun changeDateFormat(
        requiredFormat: String,
        date: Date
    ): String {
        return SimpleDateFormat(requiredFormat).format(date)
    }


}