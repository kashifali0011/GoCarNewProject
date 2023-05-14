package com.towsal.towsal.extensions

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.anggrayudi.storage.extension.fromSingleUri
import com.anggrayudi.storage.extension.isDownloadsDocument
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.*
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.serializer.cardetails.CarCustomAvailability
import com.towsal.towsal.network.serializer.checkoutcarbooking.Charges
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.ExtensionEnum
import com.towsal.towsal.utils.ImagePicker
import com.towsal.towsal.views.bottomsheets.ValidationBottomSheet
import com.towsal.towsal.views.fragments.FiltersFragment
import kotlinx.android.synthetic.main.custom_dialog_gellery_camera.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Extension function for showing additional charges popup
 * */
fun Activity.openChargesPopUp(charges: Charges, isInvoice: Boolean, remaining: String) {
    val popup = Dialog(this, R.style.full_screen_dialog)
    val binding = ChargesPopUpBinding.bind(
        LayoutInflater.from(this).inflate(R.layout.charges_pop_up, null)
    )
    popup.requestWindowFeature(Window.FEATURE_NO_TITLE)
    popup.setCancelable(false)
    popup.setContentView(binding.root)
    popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    popup.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    binding.close.setOnClickListener {
        popup.dismiss()
    }
    val title: String
    val isCredit: Boolean
    //        for receipt
    if (!isInvoice) {
        title = this.resources.getString(R.string.receipt).lowercase(Locale.getDefault())
        binding.title.text = "${this.resources.getString(R.string.updated)} ${
            this.resources.getString(R.string.receipt)
        }"
        binding.clGoCarFees.visibility = View.VISIBLE
        binding.tvGoCarFees.text =
            resources.getString(R.string.gocar_fee_20_, charges.goCarFeePercentage.toString() + "%")
        binding.tvGoCarFeesAmount.text =
            resources.getString(R.string.sar_, " - " + charges.goCarFee)
        binding.tvVatAmount.text =
            resources.getString(R.string.sar_, "-" + charges.updatedVat)

        binding.tvPaid.text = this.resources.getString(R.string.previously_earned)
        binding.tvPaidPrice.text =
            this.resources.getString(R.string.sar_, charges.previouslyEarned.toString())
        binding.tvTotalAmount.text = resources.getString(R.string.sar_, charges.earned)
        binding.tvTotal.text = resources.getString(R.string.you_earned)

        // checking for negative value
        if (remaining.contains("-")) {
            binding.tvRemaining.text = resources.getString(R.string.payable)
            binding.clRemaining.setBackgroundResource(R.drawable.bg_payable_charges)
            isCredit = false
        } else {
            binding.clRemaining.setBackgroundResource(R.drawable.bg_credit_charges)
            binding.tvRemaining.text = resources.getString(R.string.receivable)
            isCredit = true
        }
    } else {
        title = this.resources.getString(R.string.invoice).lowercase(Locale.getDefault())
        binding.tvVatAmount.text =
            this.resources.getString(R.string.sar_, charges.updatedVat)
        binding.tvVatAmount.setTextColor(
            ResourcesCompat.getColor(
                this.resources,
                R.color.black,
                null
            )
        )
        binding.tvVat.text = this.resources.getString(R.string.vat_, "${charges.vatPercentage}%")

        binding.tvPaid.text = this.resources.getString(R.string.paid_trip_price)
        binding.tvPaidPrice.text =
            this.resources.getString(R.string.sar_, charges.alreadyPaidTripPrice.toString())
        binding.clGoCarFees.visibility = View.GONE
        binding.tvTotalAmount.text = resources.getString(R.string.sar_, charges.total)
        binding.tvTotal.text = resources.getString(R.string.total)

        // checking for negative value
        if (remaining.contains("-")) {
            binding.clRemaining.setBackgroundResource(R.drawable.bg_credit_charges)
            binding.tvRemaining.text = resources.getString(R.string.credit)
            isCredit = true
        } else {
            binding.clRemaining.setBackgroundResource(R.drawable.bg_payable_charges)
            binding.tvRemaining.text = resources.getString(R.string.payable)
            isCredit = false
        }

    }
    binding.tvRemainingAmount.text =
        resources.getString(R.string.sar_, remaining.replace("-", ""))
    if (isCredit) {
        binding.tvRemainingAmount.setTextColor(
            ResourcesCompat.getColor(resources, R.color.text_receive_msg, null)
        )
        binding.tvRemaining.setTextColor(
            ResourcesCompat.getColor(resources, R.color.text_receive_msg, null)
        )
    } else {
        binding.tvRemainingAmount.setTextColor(
            ResourcesCompat.getColor(resources, R.color.black_text_black_variation_18, null)
        )
        binding.tvRemaining.setTextColor(
            ResourcesCompat.getColor(resources, R.color.black_text_black_variation_18, null)
        )
    }

    setUiAccordingToType(charges, this, binding, title)
    try {
        popup.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Function for setting additional charges screen according to type
 * */
private fun setUiAccordingToType(
    charges: Charges,
    activity: Context,
    binding: ChargesPopUpBinding,
    title: String,
) {
    when (charges.is_extended) {
        ExtensionEnum.TRIP_EXTENDED.value -> {

            binding.clDays.visibility = View.VISIBLE
            binding.clDaysPrice.visibility = View.VISIBLE
            binding.clKilometers.visibility = View.GONE
            binding.clKilometersPrice.visibility = View.GONE
            binding.tvPriceAmount.text =
                activity.resources.getString(R.string.sar_, charges.additionCharges.toString())
            setNoOfDays(binding, charges, activity)
            binding.tvDaysType.text = activity.resources.getString(R.string.extended_days)
            binding.tvPriceType.text =
                activity.resources.getString(R.string.edition_trip_fee)
            binding.tvMessage.text = "This $title has been generated for the trip extension."
        }
        ExtensionEnum.TRIP_REDUCED.value -> {
            //                visibility
            binding.clDays.visibility = View.VISIBLE
            binding.clDaysPrice.visibility = View.VISIBLE
            binding.clKilometers.visibility = View.GONE
            binding.clKilometersPrice.visibility = View.GONE

            //                text setting
            setNoOfDays(binding, charges, activity)
            binding.tvPriceAmount.text =
                activity.resources.getString(R.string.sar_, charges.updatedTripPrice.toString())
            binding.clGoCarFees.visibility = View.GONE
            binding.clVat.visibility = View.GONE
            binding.tvDaysType.text = activity.resources.getString(R.string.days_refunded)
            binding.tvPriceType.text =
                activity.resources.getString(R.string.credit_due)
            binding.tvMessage.text = "This $title has been generated for the trip reduction."

        }
        ExtensionEnum.KILOMETERS_EXCEEDED.value -> {

            //                visibility
            binding.clDays.visibility = View.GONE
            binding.clDaysPrice.visibility = View.GONE
            binding.clKilometersPrice.visibility = View.VISIBLE
            binding.clKilometers.visibility = View.VISIBLE

            //                text setting
            binding.tvNoOfKilometers.text =
                activity.getString(R.string.km_, charges.exceedingKilometers.toString())
            binding.tvKilometersAmount.text =
                activity.getString(R.string.sar_, charges.exceedingCharges)
            binding.tvMessage.text =
                "This $title has been generated for the additional kilometers driven."
            binding.tvKilometersPrice.text =
                activity.resources.getString(R.string.additionally_charged)
        }
        ExtensionEnum.KILOMETERS_EXCEEDED_AND_TRIP_EXTENDED.value -> {
            //                visibility
            binding.clDays.visibility = View.VISIBLE
            binding.clDaysPrice.visibility = View.VISIBLE
            binding.clKilometers.visibility = View.VISIBLE
            binding.clKilometersPrice.visibility = View.VISIBLE

            //                text setting
            setNoOfDays(binding, charges, activity)
            binding.tvPriceAmount.text =
                activity.resources.getString(R.string.sar_, charges.updatedTripPrice.toString())
            binding.tvDaysType.text = activity.resources.getString(R.string.extended_days)
            binding.tvPriceType.text =
                activity.resources.getString(R.string.extended_trip_price)
            binding.tvMessage.text =
                "This $title has been generated for the trip extension and kilometers exceeded."
            binding.tvNoOfKilometers.text =
                activity.getString(R.string.km_, charges.exceedingKilometers.toString())
            binding.tvKilometersAmount.text =
                activity.getString(R.string.sar_, charges.exceedingCharges)
        }
        ExtensionEnum.KILOMETERS_EXCEEDED_AND_TRIP_REDUCED.value -> {

            //                visibility
            binding.clDays.visibility = View.VISIBLE
            binding.clKilometersPrice.visibility = View.VISIBLE
            binding.clDaysPrice.visibility = View.VISIBLE
            binding.clKilometers.visibility = View.VISIBLE
            //                text setting
            setNoOfDays(binding, charges, activity)
            binding.tvDaysType.text = activity.resources.getString(R.string.reduced_days)
            binding.tvPriceType.text =
                activity.resources.getString(R.string.reduced_trip_price)
            binding.tvMessage.text =
                "This $title has been generated for the trip reduction and kilometers exceeded."
            binding.tvNoOfKilometers.text =
                activity.getString(R.string.km_, charges.exceedingKilometers.toString())
            binding.tvKilometersAmount.text =
                activity.getString(R.string.sar_, charges.exceedingCharges)

            binding.tvPriceAmount.text =
                activity.resources.getString(R.string.sar_, charges.updatedTripPrice.toString())
        }
    }
}


/**
 * Extension function for showing additional charges popup
 * */
fun Activity.openAccruedFines(protectionFee: String, lateReturnFine: String , totalFine: String) {
    val popup = Dialog(this, R.style.full_screen_dialog)
    val binding = InvoiceFineLayoutBinding.bind(
        LayoutInflater.from(this).inflate(R.layout.invoice_fine_layout, null)
    )

    popup.requestWindowFeature(Window.FEATURE_NO_TITLE)
    popup.setCancelable(false)
    popup.setContentView(binding.root)
    popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    popup.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    binding.close.setOnClickListener {
        popup.dismiss()
    }

    if (lateReturnFine == "0.00" || lateReturnFine == "0"){
        binding.clLateReturn.isVisible = false
    }else{
        binding.clLateReturn.isVisible = true
        binding.tvLateReturnPrice.text = "SAR $lateReturnFine"


    }

    if (protectionFee == "0.00" || protectionFee == "0"){
        binding.clImproperReturn.isVisible = false
    }else{

        binding.tvNoOfDays.text = "SAR $protectionFee"
        binding.clImproperReturn.isVisible = true
    }

    binding.tvTotalAmount.text = "SAR $totalFine"



    try {
        popup.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun Activity.openMap(lat:Double ,long: Double){
    var uri = "http://maps.google.com/maps?q=loc:" + lat + "," + long
    var intent =  Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    startActivity(intent)
}

/**
 * Extension function for showing additional charges popup
 * */
fun Activity.openAccruedFinesReceipt( failureToReport: String, hostNoShow: String ,cancellationFine: String, totalFine: String) {
    val popup = Dialog(this, R.style.full_screen_dialog)
   /* val binding = ReceiptFineLayoutBinding.bind(
        LayoutInflater.from(this).inflate(R.layout.receipt_fine_layout, null)
    )*/

    val binding = ReceptFineLayoutNewDesignBinding.bind(
        LayoutInflater.from(this).inflate(R.layout.recept_fine_layout_new_design, null)
    )

    popup.requestWindowFeature(Window.FEATURE_NO_TITLE)
    popup.setCancelable(false)
    popup.setContentView(binding.root)
    popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    popup.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    binding.close.setOnClickListener {
        popup.dismiss()
    }

    if (failureToReport == "0.00" || failureToReport == "0"){
        binding.clFailureReport.isVisible = false
    }else{
        binding.clFailureReport.isVisible = true
        binding.tvFailureReportPrice.text = "SAR $failureToReport"
    }

    if (hostNoShow == "0.00" || hostNoShow == "0"){
        binding.clHostNoShow.isVisible = false
    }else{
        binding.tvHostNoShowPrice.text = "SAR $hostNoShow"
        binding.clHostNoShow.isVisible = true
    }

    if (cancellationFine == "0.00" || cancellationFine == "0"){
        binding.clTripNoCancel.isVisible = false
    }else{
        binding.tvTripCancel.text = "SAR $cancellationFine"
        binding.clTripNoCancel.isVisible = true
    }

    binding.tvTotalAmount.text = "SAR $totalFine"



    try {
        popup.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

/**
 * Function for handling singulars and plurals of no of days
 * */

private fun setNoOfDays(binding: ChargesPopUpBinding, charges: Charges, activity: Context) {
    binding.tvNoOfDays.text =
        charges.updatedDays.toString()
}

/**
 * Extension function for changing text view text color
 * */
fun TextView.changeTextColor(id: Int) {
    this.setTextColor(ResourcesCompat.getColor(this.context.resources, id, null))
}

/**
 * Function for hiding keyboard
 * */
fun hideKeyboard(context: Activity) {
    val inputManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = context.currentFocus
    if (view != null) {
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

/**
 * Extension function for preventing double clicks
 * */
fun View.preventDoubleClick() {
    isClickable = false
    Handler(Looper.getMainLooper()).postDelayed({ isClickable = true }, 1000L)
}

/**
 * Extension function for setting text view's gradient text colors
 * */
fun TextView.setGradientTextColor(
    colors: IntArray,
    x0: Float = 0f,
    y0: Float = 0f,
    x1: Float = 0f,
    y1: Float = this.lineHeight.toFloat(),
    positions: FloatArray? = null,
) {

    val shader: Shader = LinearGradient(
        x0, y0, x1, y1,
        colors, positions, Shader.TileMode.CLAMP
    )
    this.paint.shader = shader

}

fun TextView.setGradientTextColorNew(colors: IntArray) {

    val shader: Shader = LinearGradient(
        0f, 100f, 90f, 0f,
        colors, null, Shader.TileMode.REPEAT
    )
    this.paint.shader = shader

}

/*fun TextView.setGradientTextColorBasics(colors: IntArray) {

    val shader: Shader = LinearGradient(
        -25f, -90f, 20f, 20f,
        colors, null, Shader.TileMode.REPEAT
    )
    this.paint.shader = shader

}*/

fun Activity.showPopupWithIndication(
    callback: PopupCallback,
    resString: Int = R.string.delete,
    resMessageId: Int = R.string.delete_confirmation_message,
    paymentStatus : Boolean = false,
    deleteMsg: String = ""
) {
    Log.d("paymentStatus" , paymentStatus.toString())
    val deleteDialog = Dialog(this, R.style.full_screen_dialog)
    val binding = PopupDeletePaymentOptionBinding.bind(
        LayoutInflater.from(this).inflate(
            R.layout.popup_delete_payment_option,
            null
        )
    )
    if (paymentStatus){
        binding.tvMessage.text = deleteMsg
    }else{
        binding.tvMessage.text = getString(resMessageId)
    }
    binding.ivIndication.setText(resString)

    deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    deleteDialog.setCancelable(false)
    deleteDialog.setContentView(binding.root)
    deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    deleteDialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    binding.btnPositiveAction.setOnClickListener {
        try {
            callback.popupButtonClick(1)
            deleteDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    binding.btnNegativeAction.setOnClickListener {
        try {
            callback.popupButtonClick(0)
            deleteDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    try {
        deleteDialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Activity.deleteCustomerCar(
    callback: PopupCallback,
) {
    val deleteDialog = Dialog(this, R.style.full_screen_dialog)
    val binding = DeleteCarLayoutBinding.bind(
        LayoutInflater.from(this).inflate(
            R.layout.delete_car_layout,
            null
        )
    )

    deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    deleteDialog.setCancelable(false)
    deleteDialog.setContentView(binding.root)
    deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    deleteDialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    binding.btnCancel.setOnClickListener {
        try {
            deleteDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    binding.btnDelete.setOnClickListener {
        try {
            callback.popupButtonClick(0)
            deleteDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    try {
        deleteDialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Activity.showPaymentSuccessPopup(
    totalAmount: String,
    transactionNo: Int,
    callback: (dialog: Dialog) -> Unit
) {
    val paymentSuccessDialog = Dialog(this, R.style.full_screen_dialog)
    val binding = PopupPaymentSuccessBinding.bind(
        LayoutInflater.from(this).inflate(
            R.layout.popup_payment_success,
            null
        )
    )
    binding.tvTotalAmount.text = "${this.getString(R.string.sar)} $totalAmount"
    binding.tvTransactionNumber.text = "$transactionNo"
    binding.tvPaymentDate.text = SimpleDateFormat(
        "MMMM dd, yyyy"
    ).format(Calendar.getInstance().time)
    paymentSuccessDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    paymentSuccessDialog.setCancelable(true)
    paymentSuccessDialog.setContentView(binding.root)
    paymentSuccessDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    paymentSuccessDialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    try {
        paymentSuccessDialog.show()
        callback(paymentSuccessDialog)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.showListingHoldPopup(callback: () -> Unit) {
    val listingOnHoldDialog = Dialog(this, R.style.full_screen_dialog)
    val binding = PopupListingOnHoldBinding.bind(
        LayoutInflater.from(this).inflate(
            R.layout.popup_listing_on_hold,
            null
        )
    )
    listingOnHoldDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    listingOnHoldDialog.setCancelable(true)
    listingOnHoldDialog.setContentView(binding.root)
    listingOnHoldDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    listingOnHoldDialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    binding.btnAddPaymentMethod.setOnClickListener {
        callback()
        listingOnHoldDialog.dismiss()
    }
    try {
        listingOnHoldDialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.showToastPopup(message: String) {
    val popupCalendarToastDialog = Dialog(this, R.style.full_screen_dialog)
    val binding = PopupCalendarToastBinding.bind(
        LayoutInflater.from(this).inflate(
            R.layout.popup_calendar_toast,
            null
        )
    )
    popupCalendarToastDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    popupCalendarToastDialog.setCancelable(true)
    popupCalendarToastDialog.setContentView(binding.root)
    popupCalendarToastDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    popupCalendarToastDialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    binding.guidlines.text = message

    try {
        popupCalendarToastDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            popupCalendarToastDialog.dismiss()
        }, 2000)
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun Calendar.isDateAvailable(
    unAvailableDate: List<String>,
    daysCustomAvailability: List<CarCustomAvailability>,
    sdfServerDateFormat: SimpleDateFormat
): Boolean {
    return daysCustomAvailability.any {
        it.dayIndex == this[Calendar.DAY_OF_WEEK] && it.isUnavailable == Constants.Availability.UNAVAILABLE
    } || unAvailableDate.contains(sdfServerDateFormat.format(this.time))
}


fun Int.getDaySuffix(): String {
    return if (this in 11..13) {
        "th"
    } else when (this % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

fun Activity.showCancelledPopUp(callBack: () -> Unit) {
    val popupCancelledTripDialog = Dialog(this, R.style.full_screen_dialog)
    val binding = PopupTripCancelledBinding.bind(
        LayoutInflater.from(this).inflate(
            R.layout.popup_trip_cancelled,
            null
        )
    )
    popupCancelledTripDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    popupCancelledTripDialog.setCancelable(false)
    popupCancelledTripDialog.setContentView(binding.root)
    popupCancelledTripDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    binding.btnOk.setOnClickListener {
        callBack()
        popupCancelledTripDialog.dismiss()
    }

    try {
        popupCancelledTripDialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Activity.showCancelTripImagesPopUp(id: Any, callBack: (id: Any, type: Any) -> Unit) {
    val popupCancelledTripDialog = Dialog(this, R.style.full_screen_dialog)
    val binding = PopupUploadCancellationReasonImagesBinding.bind(
        LayoutInflater.from(this).inflate(
            R.layout.popup_upload_cancellation_reason_images,
            null
        )
    )
    popupCancelledTripDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    popupCancelledTripDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    popupCancelledTripDialog.setContentView(binding.root)

    binding.tvBtnUploadImages.setOnClickListener {
        callBack(id, Constants.FileTypes.IMAGES)
        popupCancelledTripDialog.dismiss()
    }
    binding.tvBtnUploadPdfs.setOnClickListener {
        callBack(id, Constants.FileTypes.PDF)
        popupCancelledTripDialog.dismiss()
    }

    try {
        popupCancelledTripDialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun getFileName(wholePath: String): String {
    var name: String? = null
    val start: Int = wholePath.lastIndexOf('/')
    val end: Int = wholePath.length
    //lastIndexOf('.');
    name = wholePath.substring((start + 1), end)
    return name
}

fun browseDocuments(
    launcher: ActivityResultLauncher<Intent>, mimeTypes: Array<String> = arrayOf(
        "application/pdf",
    )
) {
    val intent = Intent(
        Intent.ACTION_OPEN_DOCUMENT
    )
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
    intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
    launcher.launch(Intent.createChooser(intent, "ChooseFile"))
}

fun browseGallery(
    launcher: ActivityResultLauncher<Intent>, mimeTypes: Array<String> = arrayOf(
        Constants.MIME_TYPES,
    )
) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
    if (mimeTypes.isNotEmpty()) {
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }
    launcher.launch(Intent.createChooser(intent, "ChooseFile"))
}

fun Context.openFile(path: String) {
    val file = File(path)
    val uri =
        FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", file)
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags =
        Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
    /*  val uri = Uri.parse(FileUtils.getPath(this, Uri.parse(path)))*/
    if (file.extension.lowercase(Locale.getDefault()) == "doc" || file.extension.lowercase(Locale.getDefault()) == "docx") {
        intent.setDataAndType(uri, "application/msword")
    } else if (file.extension.lowercase(Locale.getDefault()) == "ppt" || file.extension.lowercase(
            Locale.getDefault()
        ) == "pptx"
    ) {
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
    } else if (file.extension.lowercase(Locale.getDefault()) == "xls" || file.extension.lowercase(
            Locale.getDefault()
        ) == "xlsx"
    ) {
        intent.setDataAndType(uri, "application/vnd.ms-excel")
    } else if (file.extension.lowercase(Locale.getDefault()) == "txt") {
        intent.setDataAndType(uri, "text/plain")
    } else if (file.extension.lowercase(Locale.getDefault()) == "pdf") {
        intent.setDataAndType(uri, "application/pdf")
    } else if (file.extension.lowercase(Locale.getDefault()) == "zip") {
        intent.setDataAndType(uri, "application/zip")
    }

    this.startActivity(Intent.createChooser(intent, "Open File"))
}


/**
 * Function for enabling location settings
 * */
fun Activity.showEnableLocationSetting(locationLauncher: ActivityResultLauncher<IntentSenderRequest>) {
    BaseActivity.activity.let {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val task = LocationServices.getSettingsClient(it)
            .checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (states?.isLocationPresent!!) {
                Log.e("GPS", "on Location")
            }
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    val intentSenderRequest: IntentSenderRequest =
                        IntentSenderRequest.Builder(
                            e.resolution.intentSender
                        ).build()
                    locationLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }
}

fun getValueInPx(context: Context?, cornerRadius: Int): Float =
    (cornerRadius * (context!!.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

fun ImageView.glideLoadCornerImage(path: String) {
    Glide.with(this)
        .load(path)
        .transform(
            CenterCrop(),
            GranularRoundedCorners(
                getValueInPx(
                    this.context, 15
                ),
                0f,
                0f,
                getValueInPx(
                    this.context, 15
                )
            )
        )
        .into(this)
}

fun ImageView.loadImage(path: String) {
    Glide.with(this)
        .load(path)
        .into(this)
}

fun ViewDataBinding.setAsHostToolBar(
    titleId: Int,
    actionBar: ActionBar? = null,
    bool: Boolean = true,
) {
    actionBar?.hide()
    when (this) {
        is LayoutNewCustomActionBarBinding -> {
            this.clToolBar.setBackgroundResource(R.drawable.bg_gradient_toolbar_host)
            this.toolbarTitle.setText(titleId)
            this.ivArrowBack.apply {
                setOnClickListener {
                    ((context) as Activity).onBackPressed()
                }
                setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.line_bg_1
                    ),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                isVisible = bool
            }

        }
        else -> {

        }
    }
}

fun ViewDataBinding.setAsGuestToolBar(
    titleId: Any,
    actionBar: ActionBar? = null,
    toolBarBg: Any = R.drawable.bg_blue,
    textColor: Int = R.color.white,
    arrowColor: Int = R.color.sky_blue_variation_1,
    arrowVisible: Boolean = true,
    filterIconVisible: Boolean = false
) {
    actionBar?.hide()
    when (this) {
        is LayoutNewCustomActionBarBinding -> {
            this.clToolBar.apply {
                when (toolBarBg) {
                    is Int -> {
                        setBackgroundResource(toolBarBg)
                    }
                    is Drawable -> {
                        background = toolBarBg
                    }
                }
            }
            this.toolbarTitle.apply {
                text =
                    when (titleId) {
                        is String -> titleId
                        else -> context.getString(titleId as Int)
                    }
                setTextColor(
                    context.getColor(textColor)
                )
            }
            this.ivArrowBack.apply {
                isVisible = arrowVisible
                setOnClickListener {
                    (context as Activity).onBackPressed()
                }
                setColorFilter(
                    ContextCompat.getColor(
                        context,
                        arrowColor
                    ),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            this.filter.apply {
                isVisible = filterIconVisible
            }
        }
        else -> {

        }
    }
}

fun DatePicker.formatDate(ymdOrder: String) {
    val system = Resources.getSystem()
    val idYear = system.getIdentifier("year", "id", "android")
    val idMonth = system.getIdentifier("month", "id", "android")
    val idDay = system.getIdentifier("day", "id", "android")
    val idLayout = system.getIdentifier("pickers", "id", "android")
    val spinnerYear = findViewById<View>(idYear) as NumberPicker
    val spinnerMonth = findViewById<View>(idMonth) as NumberPicker
    val spinnerDay = findViewById<View>(idDay) as NumberPicker
    val layout = findViewById<View>(idLayout) as LinearLayout
    layout.removeAllViews()
    for (i in 0 until 3) {
        when (ymdOrder[i]) {
            'y' -> {
                layout.addView(spinnerYear)
                setImeOptions(spinnerYear, i)
            }
            'm' -> {
                layout.addView(spinnerMonth)
                setImeOptions(spinnerMonth, i)
            }
            'd' -> {
                layout.addView(spinnerDay)
                setImeOptions(spinnerDay, i)
            }
            else -> throw IllegalArgumentException("Invalid char[] ymdOrder")
        }
    }
}

private fun setImeOptions(spinner: NumberPicker, spinnerIndex: Int) {
    val imeOptions: Int = if (spinnerIndex < 3 - 1) {
        EditorInfo.IME_ACTION_NEXT
    } else {
        EditorInfo.IME_ACTION_DONE
    }
    val idPickerInput: Int =
        Resources.getSystem().getIdentifier("numberpicker_input", "id", "android")
    val input = spinner.findViewById<View>(idPickerInput) as TextView
    input.imeOptions = imeOptions
}

fun Context.convertBitmapToUri(bitmap: Bitmap): Uri {
    val newBitmap = this.compressBitmap(bitmap)
    val file = File(this.cacheDir, System.currentTimeMillis().toString() + ".png")
    try {
        file.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    val bos = ByteArrayOutputStream()
    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
    val bitmapdata = bos.toByteArray()
    try {
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return Uri.fromFile(file)
}

fun Context.compressBitmap(image: Bitmap): Bitmap {

    var finalImage: Bitmap? = null
    if (image.width > Constants.CONST_DEFAULT_IMAGE_WIDTH || image.height > Constants.CONST_DEFAULT_IMAGE_WIDTH) {
        if (image.width > image.height) {
            val ratio = (Constants.CONST_DEFAULT_IMAGE_WIDTH.toFloat() / image.width.toFloat())
            finalImage = Bitmap.createScaledBitmap(
                image,
                Constants.CONST_DEFAULT_IMAGE_WIDTH,
                (image.height * ratio).toInt(),
                true
            )

        } else if (image.height > image.width) {

            val ratio = (Constants.CONST_DEFAULT_IMAGE_WIDTH.toFloat() / image.height.toFloat())
            finalImage = Bitmap.createScaledBitmap(
                image,
                (image.width * ratio).toInt(),
                Constants.CONST_DEFAULT_IMAGE_WIDTH,
                true
            )

        } else {
            if (image.width > Constants.CONST_DEFAULT_IMAGE_WIDTH) {
                finalImage = Bitmap.createScaledBitmap(
                    image,
                    Constants.CONST_DEFAULT_IMAGE_WIDTH,
                    Constants.CONST_DEFAULT_IMAGE_WIDTH,
                    true
                )
            } else {
                finalImage = image
            }
        }
    } else {
        finalImage = if (image.width > image.height) {
            val ratio = ((image.width * 0.7) / image.width.toFloat())
            Bitmap.createScaledBitmap(
                image,
                (image.width * 0.7).toInt(),
                (image.height * ratio).toInt(),
                true
            )
        } else {
            val ratio = ((image.height * 0.7) / image.height.toFloat())
            Bitmap.createScaledBitmap(
                image,
                (image.width * ratio).toInt(),
                (image.height * 0.7).toInt(),
                true
            )
        }
    }
    return finalImage!!
}

fun Context.showChooseAppDialog(
    callBack: (ImagePicker) -> Unit,
) {
    val layoutInflater = LayoutInflater.from(this)
    val customView = layoutInflater.inflate(R.layout.custom_dialog_gellery_camera, null)

    val dialog = AlertDialog.Builder(this)
        .setTitle(R.string.title_choose_image_provider)
        .setView(customView)
        .setOnCancelListener {
            it.dismiss()
        }
        .setNegativeButton(R.string.action_cancel) { _, _ ->
        }
        .setOnDismissListener {
        }
        .show()

    // Handle Camera option click
    customView.lytCameraPick.setOnClickListener {
        callBack(ImagePicker.CAMERA)
        dialog.dismiss()
    }

    // Handle Gallery option click
    customView.lytGalleryPick.setOnClickListener {
        callBack(ImagePicker.GALLERY)
        dialog.dismiss()
    }
}


/**
 * @return Intent Gallery Document Intent
 */
fun getGalleryDocumentIntent(mimeTypes: Array<String>): Intent {
    // Show Document Intent
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).applyImageTypes(mimeTypes)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    return intent
}

private fun Intent.applyImageTypes(mimeTypes: Array<String>): Intent {
    // Apply filter to show image only in intent
    type = "image/*"
    if (mimeTypes.isNotEmpty()) {
        putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }
    return this
}

fun EditText.hideOrShowPassword(imageView: ImageView) {
    this.apply {
        if (inputType in listOf(
                InputType.TYPE_CLASS_TEXT,
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            )
        ) {
            imageView.setImageResource(
                R.drawable.ic_show_password
            )
            inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(
                R.drawable.ic_show_password
            )
        }
    }
}

fun AppCompatActivity.setValidationMessage(title: String = "Incorrect password", messageId: Any) {
    this.lifecycleScope.launch {
        val validationBottomSheet = ValidationBottomSheet(
            title = title,
            message = when (messageId) {
                is String -> messageId
                is Int -> getString(messageId)
                else -> ""
            }
        )
        validationBottomSheet.show(
            supportFragmentManager, "f"
        )
        delay(6000)
        validationBottomSheet.dismiss()
    }
}

@Throws(IOException::class)
fun copy(src: File?, dst: File?) {
    FileInputStream(src).use { `in` ->
        FileOutputStream(dst).use { out ->
            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
        }
    }
}


fun getFilePathFromCache(uri: Uri, contentResolver: ContentResolver, context: Context): String {

    contentResolver.let {
        val filePath = (context.applicationInfo.dataDir + File.separator
                + System.currentTimeMillis())
        val file = File(filePath)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buf = ByteArray(4096)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                    buf,
                    0,
                    len
                )
            }
        }
        return file.absolutePath
    }
}

fun Context.intentToDocumentFiles(intent: Intent?): List<DocumentFile> {
    val uris = intent?.clipData?.run {
        val list = mutableListOf<Uri>()
        for (i in 0 until itemCount) {
            list.add(getItemAt(i).uri)
        }
        list.takeIf { it.isNotEmpty() }
    } ?: listOf(intent?.data ?: return emptyList())

    return uris.mapNotNull { uri ->
        if (uri.isDownloadsDocument && SDK_INT < 28 && uri.path?.startsWith("/document/raw:") == true) {
            val fullPath = uri.path.orEmpty().substringAfterLast("/document/raw:")
            DocumentFile.fromFile(File(fullPath))
        } else {
            this.fromSingleUri(uri)
        }
    }.filter { it.isFile }
}

fun Activity.openSharingIntent(path: String) = try {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    val shareMessage: String = """
        $path
        """.trimIndent()
    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
    ContextCompat.startActivity(this, Intent.createChooser(shareIntent, "choose one"), null)
} catch (e: java.lang.Exception) {
    //e.toString();
}

fun Activity.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


fun getMaxCalendar(dateString: String?, currentFormat: String): () -> Calendar? =  {
    val maxDate: Date? =
        stringToDate(dateString, currentFormat)
    val maxDateCalendar: Calendar? = if (maxDate != null) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = maxDate
        calendar
    } else null
    maxDateCalendar
}

fun stringToDate(dateString: String?, currentFormat: String): Date? {
    return if (dateString == null) {
        null
    } else {
        val format = SimpleDateFormat(currentFormat, Locale.getDefault())
        var date: Date? = null
        try {
            date = format.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        date
    }
}

fun dateToString(date: Date, targetFormat: String): String = SimpleDateFormat(targetFormat).format(date)