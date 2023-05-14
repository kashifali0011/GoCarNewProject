package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentCalendarBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.SettingsViewModel
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse {

    private var carId = 0
    lateinit var binding: FragmentCalendarBinding
    val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCalendarBinding.bind(
            inflater.inflate(R.layout.fragment_calendar, container, false)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()
        binding.fragment = this
        binding.layoutToolBar.setAsGuestToolBar(
            R.string.your_calendar,
            null
        )
        binding.calendarView.setMinimumDate(
            calendar
        )
    }

    override fun onClick(p0: View?) {
        p0?.preventDoubleClick()
        when (p0) {
            binding.save -> {
                if (binding.calendarView.selectedDates.isNotEmpty())
                    settingsViewModel.updateCarStatus(
                        Constants.CarStatus.SNOOZED,
                        carId.toString(),
                        uiHelper.currentDate,
                        DateUtil.changeDateFormat(
                            Constants.ServerDateFormat,
                            binding.calendarView.selectedDates[0].time
                        ),
                        true,
                        Constants.API.UPDATE_CAR_STATUS,
                        this
                    )
                else {
                    uiHelper.showLongToastInCenter(
                        requireActivity(),
                        getString(R.string.please_select_date)
                    )
                }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.UPDATE_CAR_STATUS -> {
                uiHelper.showLongToastInCenter(requireActivity(), response?.message ?: "")
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message ?: "")
    }
}