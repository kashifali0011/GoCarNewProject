package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentEarningSettingBinding
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.earnings.EarningBarChart
import com.towsal.towsal.network.serializer.earnings.EarningResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import okhttp3.internal.toHexString
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

/**
 * Fragment class for user earning settings screen
 * */
class EarningSettingFragment : BaseFragment(),
    OnNetworkResponse {

    private var barChartLabels: ArrayList<String> = ArrayList()

    private val settingsViewModel: SettingsViewModel by viewModel()
    private var singleDay = ""

    companion object {
        const val DAY = 1
        const val WEEK = 2
        const val MONTH = 3
        const val YEAR = 4
        var dateFormat = "MMM d, yyyy"
        var monthFormat = "MMMM YYYY"
        var yearFormat = "YYYY"
        var dayFormat = "EEEE"
    }


    private val chartColors = intArrayOf(
        ColorTemplate.rgb("#0F0F7D"),
        ColorTemplate.rgb("#0F0F7D"),
        ColorTemplate.rgb("#0F0F7D"),
        ColorTemplate.rgb("#0F0F7D")
    )
    private lateinit var binding: FragmentEarningSettingBinding
    private var currentEnum = -1
    private var nextDate = ""
    private var previousDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_earning_setting,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainLayout.visibility = View.GONE
        binding.fragment = this
        callApiAccordingToSelection()
    }

    private fun callApiAccordingToSelection() {
        if(binding.filterDay.background == requireActivity().getDrawable(R.drawable.shape_filter_earing_new))
            updateUI(DAY, uiHelper.currentDate)

        else if(binding.filterWeek.background == requireActivity().getDrawable(R.drawable.shape_filter_earing_new))
            updateUI(WEEK, uiHelper.currentDate)

        else if(binding.filterMonth.background == requireActivity().getDrawable(R.drawable.shape_filter_earing_new))
            updateUI(MONTH, uiHelper.currentDate)

        else if(binding.filterYear.background == requireActivity().getDrawable(R.drawable.shape_filter_earing_new))
            updateUI(YEAR, uiHelper.currentDate)
        else
            updateUI(DAY, uiHelper.currentDate)
    }

    /**
     * Function for setting up bar chart
     * */
    private fun barChartSetting(reports: EarningResponseModel) {
        val barDataSet = BarDataSet(getData(reports.barChartValues), "")
        barDataSet.setColors(*chartColors)
        barDataSet.setDrawValues(false)
        barDataSet.formSize = 0f
        val barData = BarData(barDataSet)
        val xAxis: XAxis = binding.barChart.xAxis!!
        val yAxisLeft: YAxis? = binding.barChart.axisLeft
        yAxisLeft?.axisMinimum = 0f
        yAxisLeft?.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return (floor(value.toDouble()).toInt()).toString()
            }
        }
        xAxis.position = XAxisPosition.BOTTOM
        val formatter = IndexAxisValueFormatter(barChartLabels)
        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter
        binding.barChart.data = barData
        binding.barChart.data.barWidth = 0.3f
        binding.barChart.isDoubleTapToZoomEnabled = false
        binding.barChart.axisRight?.isEnabled = false
        binding.barChart.xAxis.setDrawGridLines(false)
        binding.barChart.xAxis.setDrawAxisLine(false)
        binding.barChart.setScaleEnabled(false)
        binding.barChart.setPinchZoom(false)

        binding.barChart.animateXY(1000, 1000)
        binding.barChart.invalidate()
        binding.barChart.description?.text = ""
        binding.barChart.description?.isEnabled = false
        binding.barChart.setDrawGridBackground(false)

        val yAxis = binding.barChart.axisLeft
        yAxis.axisLineColor = ContextCompat.getColor(requireActivity(),R.color.black_text_black_variation_18)
        yAxis.axisLineWidth = 0.7f
        yAxis.textColor = ContextCompat.getColor(requireActivity(),R.color.black_text_black_variation_18)
        yAxis.typeface = ResourcesCompat.getFont(requireActivity(),R.font.inter_medium)
        yAxis.textSize = 12f


        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = ContextCompat.getColor(requireActivity(),R.color.black_text_black_variation_18)
        xAxis.axisLineWidth = 0.7f
        xAxis.textColor = ContextCompat.getColor(requireActivity(),R.color.black_text_black_variation_18)
        xAxis.typeface = ResourcesCompat.getFont(requireActivity(),R.font.inter_medium)
        xAxis.textSize = 12f



    }

    /**
     * Function for getting bar entries
     * */
    @SuppressLint("SimpleDateFormat")
    private fun getData(barChartValues: ArrayList<EarningBarChart>): ArrayList<BarEntry> {
        val entries: ArrayList<BarEntry> = ArrayList()
        barChartLabels = ArrayList()
        if (barChartValues.isNotEmpty()) {
            for (i in barChartValues.indices) {
                entries.add(BarEntry(i.toFloat(), barChartValues[i].yAxisValue))
                barChartValues[i].label?.let { barChartLabels.add(it) }
            }
        }
        if (currentEnum == DAY) {
            barChartLabels.clear()
            barChartLabels.add(singleDay)
        }
        if (currentEnum == WEEK) {
            barChartLabels.clear()
            if (barChartValues.isNotEmpty()) {
                for (i in barChartValues.indices) {
                    if (!barChartValues[i].date.isNullOrEmpty()) {
                        val dateName = uiHelper.getFormattedDate(
                            barChartValues[i].date!!,
                            Constants.ServerDateFormat,
                            Constants.DayFormat
                        )
                        barChartLabels.add(dateName)
                    }
                }
            }
        }
        if (currentEnum == MONTH) {
            barChartLabels.clear()
            if (barChartValues.isNotEmpty()) {
                for (i in barChartValues.indices) {
                    if (!barChartValues[i].date.isNullOrEmpty()) {
                        val simpleDateFormat = SimpleDateFormat(Constants.ServerDateFormat)
                        val calender = Calendar.getInstance()
                        calender.time = simpleDateFormat.parse(barChartValues[i].date!!)!!
                        barChartLabels.add(requireActivity().getString(R.string.week) + " " + (i + 1))
                    }
                }
            }
        }
        if (currentEnum == YEAR) {
            barChartLabels.clear()
            for (i in barChartValues.indices) {
                if (!barChartValues[i].date.isNullOrEmpty()) {
                    val simpleDateFormat = SimpleDateFormat(Constants.ServerDateFormat)
                    val calender = Calendar.getInstance()
                    calender.time = simpleDateFormat.parse(barChartValues[i].date!!)!!
                    val month: Int = calender.get(Calendar.MONTH)
                    val label =
                        if (month >= Calendar.JANUARY && month <= Calendar.MARCH) "Jan-Mar"
                        else if (month >= Calendar.APRIL && month <= Calendar.JUNE) "Apr-Jun"
                        else if (month >= Calendar.JULY && month <= Calendar.SEPTEMBER) "July-Sep"
                        else "Oct-Dec"
                    barChartLabels.add(label)
                }
            }
        }
        return entries
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.filterDay -> {
                currentEnum = DAY
                updateUI(DAY, uiHelper.currentDate)
            }
            R.id.filterWeek -> {
                currentEnum = WEEK
                updateUI(WEEK, uiHelper.currentDate)
            }
            R.id.filterMonth -> {
                currentEnum = MONTH
                updateUI(MONTH, uiHelper.currentDate)
            }
            R.id.filterYear -> {
                currentEnum = YEAR
                updateUI(YEAR, uiHelper.currentDate)
            }
            R.id.next -> {
                updateUI(currentEnum, nextDate)
            }
            R.id.previous -> {
                updateUI(currentEnum, previousDate)
            }
        }
    }

    /**
     * Function for handling ui
     * */
    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    private fun updateUI(i: Int, date: String) {
        binding.filterDay.background = null
        binding.filterDay.setTextColor(requireActivity().getColor(R.color.host_name_color))

        binding.filterWeek.background = null
        binding.filterWeek.setTextColor(requireActivity().getColor(R.color.host_name_color))

        binding.filterMonth.background = null
        binding.filterMonth.setTextColor(requireActivity().getColor(R.color.host_name_color))

        binding.filterYear.background = null
        binding.filterYear.setTextColor(requireActivity().getColor(R.color.host_name_color))
        settingsViewModel.getEarningReport(i, date, true, Constants.API.GET_EARNING_REPORT, this)
        when (i) {
            DAY -> {
                binding.filterDay.background =
                    requireActivity().getDrawable(R.drawable.shape_filter_earing_new)
                binding.filterDay.setTextColor(requireActivity().getColor(R.color.colorAccent))
                binding.header.text = uiHelper.getFormattedDate(
                    date, Constants.ServerDateFormat,
                    dateFormat
                )

                singleDay = uiHelper.getFormattedDate(
                    date, Constants.ServerDateFormat,
                    dayFormat
                )
            }
            WEEK -> {
                binding.filterWeek.background =
                    requireActivity().getDrawable(R.drawable.shape_filter_earing_new)
                binding.filterWeek.setTextColor(requireActivity().getColor(R.color.colorAccent))
                val stringDate = getWeekStartAndEndDate(date)
                binding.header.text = stringDate
            }
            MONTH -> {
                binding.filterMonth.background =
                    requireActivity().getDrawable(R.drawable.shape_filter_earing_new)
                binding.filterMonth.setTextColor(requireActivity().getColor(R.color.colorAccent))
                binding.header.text = uiHelper.getFormattedDate(
                    date, Constants.ServerDateFormat,
                    monthFormat
                )
            }
            YEAR -> {
                binding.filterYear.background =
                    requireActivity().getDrawable(R.drawable.shape_filter_earing_new)
                binding.filterYear.setTextColor(requireActivity().getColor(R.color.colorAccent))
                binding.header.text = uiHelper.getFormattedDate(
                    date, Constants.ServerDateFormat,
                    yearFormat
                )
            }

        }
    }

    /**
     * getting date range when type selected is week
     * */
    private fun getWeekStartAndEndDate(dateString: String): String {
        val df: DateFormat = SimpleDateFormat(Constants.ServerDateFormat, Locale.ENGLISH)
        val dayMonthFormat: DateFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        val datePosted: String = dateString

        val date: Date?
        return try {
            date = df.parse(datePosted)

            val start: Calendar = Calendar.getInstance()
            start.time = date!!
            start.set(Calendar.DAY_OF_WEEK, 1)
            start.add(Calendar.DAY_OF_YEAR, 1)

            val end: Calendar = Calendar.getInstance()
            end.time = date
            end.set(Calendar.DAY_OF_WEEK, 7)
            end.add(Calendar.DAY_OF_YEAR, 1)

            val stringDate: String =
                dayMonthFormat.format(start.time) + " - " + dayMonthFormat.format(
                    end.time
                )
            stringDate
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_EARNING_REPORT -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    EarningResponseModel::class.java
                )
                var count = 0
                var numberOfBookings = 0
                var revenue = 0
                if (model.barChartValues.isNotEmpty()) {
                    for (i in model.barChartValues.indices) {
                        if (model.barChartValues[i].yAxisValue == 0f) {
                            count += 1
                        }
                        revenue += model.barChartValues[i].yAxisValue.toInt()
                        numberOfBookings += model.barChartValues[i].total_count
                    }
                }
                binding.bookings.text = "$numberOfBookings"
                binding.revenue.text = "$revenue SAR"
                if (model.barChartValues.isEmpty() || count == model.barChartValues.size) {
                    binding.barChart.visibility = View.INVISIBLE
                    binding.noDataLayout.visibility = View.VISIBLE
                } else {
                    binding.barChart.visibility = View.VISIBLE
                    binding.noDataLayout.visibility = View.GONE
                    barChartSetting(model)
                }
                nextDate = model.next_date
                previousDate = model.prev_date
                binding.totalBookings.text = model.total_booking + ""

                if (model.total_revenue ?: "0" == "0"){
                    binding.totalRevenue.text ="0 SAR"
                }else{
                    binding.totalRevenue.text = model.total_revenue +" SAR" ?: "0"
                }


                binding.mainLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
    }
}