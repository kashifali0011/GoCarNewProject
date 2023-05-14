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
import com.towsal.towsal.databinding.BottomSheetPriceDetailsNewBinding

class PriceNewDetailsBottomSheet(

    var defaultDayCount : Int = 0,
    var mondayCount : Int = 0,
    var tuesdayCount : Int = 0,
    var wednesdayCount : Int = 0,
    var thursdayCount : Int = 0,
    var fridayCount : Int = 0,
    var saturdayCount : Int = 0,
    var sundayCount : Int = 0,


    var defaultDayPrice : Int = 0,
    var mondayPrice : Int = 0,
    var tuesdayPrice : Int = 0,
    var wednesdayPrice : Int = 0,
    var thursdayPrice : Int = 0,
    var fridayPrice : Int = 0,
    var saturdayPrice : Int = 0,
    var sundayPrice : Int = 0,
    var totalDiscount: Int = 0,
    var newCustomDaysPrices: String = ""





) : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetPriceDetailsNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPriceDetailsNewBinding.bind(
            inflater.inflate(
                R.layout.bottom_sheet_price_details_new,
                null
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


   var totalCustomPrice = 0
        var customTotalDays = 0
        totalCustomPrice = defaultDayPrice +  mondayPrice + tuesdayPrice + wednesdayPrice + thursdayPrice + fridayPrice + saturdayPrice + sundayPrice
        customTotalDays = defaultDayCount + mondayCount + tuesdayCount + wednesdayCount + thursdayCount + fridayCount + saturdayCount + sundayCount
        var customDay = defaultDayCount+mondayCount + tuesdayCount + wednesdayCount + thursdayCount + fridayCount + saturdayCount + sundayCount

        if (customDay < 3){
            binding.clDiscount.isVisible = false
        }
        if (customDay in 3..6){
            binding.clDiscount.isVisible = true
            binding.tvDiscountTitle.text = "3+ days discount"
        }

        if (customDay in 7..30){
            binding.clDiscount.isVisible = true
            binding.tvDiscountTitle.text = "7+ days discount"
        }
        if (customDay > 30){
            binding.clDiscount.isVisible = true
            binding.tvDiscountTitle.text = "30+ days discount"
        }



   if (customTotalDays == 1){
       binding.tvDiscountAmount.text = "- SAR $newCustomDaysPrices "
   }else if (customTotalDays > 1){

       if (totalDiscount > 0){
           var parDiscounts =  totalDiscount * totalCustomPrice / 100.0
           binding.tvDiscountAmount.text = "-SAR $newCustomDaysPrices"
       }else{

       }

   }
        if (defaultDayCount > 0){
            binding.clDefaultDay.isVisible = true
            if (defaultDayCount > 1) {
                binding.tvDefaultDay.text = "$defaultDayPrice SAR x $defaultDayCount days"
            }
            else
            { binding.tvDefaultDay.text = "$defaultDayPrice SAR x $defaultDayCount day"}
        }

        var customMonPrice = 0
        var customTuePrice = 0
        var customWedPrice = 0
        var customThuPrice = 0
        var customFriPrice = 0
        var customSatPrice = 0
        var customSunPrice = 0

        var discountDay = mondayCount + tuesdayCount + wednesdayCount + thursdayCount + fridayCount + saturdayCount + sundayCount
        var discountPrice = mondayPrice + tuesdayPrice + wednesdayPrice + thursdayPrice + fridayPrice + saturdayPrice + sundayPrice

        customMonPrice = mondayCount * mondayPrice
        customTuePrice = tuesdayCount * tuesdayPrice
        customWedPrice = wednesdayCount * wednesdayPrice
        customThuPrice = thursdayCount * thursdayPrice
        customFriPrice = fridayCount * fridayPrice
        customSatPrice = saturdayCount * saturdayPrice
        customSunPrice = sundayCount * sundayPrice

        var totalCustomDaysPrice =   customMonPrice + customTuePrice + customWedPrice + customThuPrice + customFriPrice + customSatPrice + customSunPrice
        var customPriceParDay = 0
        if (discountDay > 0){
            customPriceParDay = totalCustomDaysPrice / discountDay
        }


        if (customPriceParDay > 0) {
            binding.clMonday.isVisible = true
            binding.tvMonday.text = "$customPriceParDay SAR x $discountDay day"
        }

   binding.ivClose.setOnClickListener {
       dismiss()
   }
}
}