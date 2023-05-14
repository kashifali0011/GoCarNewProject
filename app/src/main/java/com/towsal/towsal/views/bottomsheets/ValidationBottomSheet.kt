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
import com.towsal.towsal.databinding.BottomSheetValidationBinding

class ValidationBottomSheet(
    val title: String,
    val message: String
): BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetValidationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme1)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetValidationBinding.bind(
            inflater.inflate(
                R.layout.bottom_sheet_validation,
                null
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvValidationMessage.text = message
        binding.tvTitle.text = title
    }
}