package com.towsal.towsal.views.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.towsal.towsal.R
import com.towsal.towsal.databinding.BottomSheetPriceDetailsBinding

class PriceDetailsBottomSheet(
    private val isDiscountApplied: Boolean = false,
    private val originalAmount: String,
    private val discountedAmount: String = "",
    private val noOfDaysDiscount: Int = 0,
    private val totalDaysCount: Int = 0
) : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetPriceDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPriceDetailsBinding.bind(
            inflater.inflate(
                R.layout.bottom_sheet_price_details,
                null
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clDiscount.isVisible = isDiscountApplied
        binding.tvDiscountTitle.text = "$noOfDaysDiscount+ days discount"
        binding.tvDiscountAmount.text = "- ${getString(R.string.sar)} $discountedAmount"
        if (totalDaysCount > 1)
            binding.tvDays.text = "$originalAmount SAR x $totalDaysCount days"
        else
            binding.tvDays.text = "$originalAmount SAR x $totalDaysCount day"

        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }
}