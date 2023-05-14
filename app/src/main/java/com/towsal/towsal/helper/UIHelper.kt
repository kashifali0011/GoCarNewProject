package com.towsal.towsal.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.CarMakesSearchPopUpBinding
import com.towsal.towsal.extensions.getDaySuffix
import com.towsal.towsal.extensions.hideKeyboard
import com.towsal.towsal.interfaces.PickerCallback
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.ApiInterface
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.CustomTypefaceSpan
import com.towsal.towsal.views.activities.MainActivity
import com.towsal.towsal.views.adapters.MakeAdapter
import com.towsal.towsal.views.listeners.ClickListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import org.koin.dsl.module
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


val uiHelperModule = module {
    factory { UIHelper() }
}

/**
 * Class for functions that are used to update ui
 * */
class UIHelper {
    var PERMISSION_CODE = 101

    //    var dateFormatDisplay = "MMM dd, yyyy"
    var dateFormatDisplay = "yyyy-MM-dd"
    private val msjsToast = ArrayList<Toast>()
    val currentDate: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())


    /**
     * Function for getting next date
     * */
    fun getTomorrowDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        return format.format(tomorrow)
    }

    /**
     * Function for getting date after 3 days of current date
     * */
    fun getDateBeforeOrAfterCurrentDate(count: Int): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, count)
        val tomorrow = calendar.time
        return format.format(tomorrow)
    }

    /**
     * Function for getting current date in a 12 hour format
     * */
    fun getCurrentTime(): String {
        val format = SimpleDateFormat("hh:00 a", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1)
        val today = calendar.time
        return format.format(today)
    }

    /**
     * Function for getting current date in a 24 hours format
     * */
    fun getCurrentTimeServer(): String {
        val format = SimpleDateFormat("HH:00:00", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1)
        val today = calendar.time
        return format.format(today)
    }

    val imagesUrl: String
        get() = "http://servicedesk.pk/woo_rides/"

    /**
     * Function for getting year from date
     * */
    fun getYear(myDate: Date): String {
        return SimpleDateFormat("yyyy", Locale.ENGLISH).format(myDate)
    }

    /**
     * Function for opening file
     * */
    fun openFile(url: File, context: Context) {
        try {
            var uri: Uri? = null

            if (url.toString().contains(".jpg") || url.toString()
                    .contains(".jpeg") || url.toString().contains(".png")
            ) {
                uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".easyphotopicker.fileprovider",
                    url
                )

            } else {
                uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".com.dtech.allodoctor.provider",
                    url
                )

            }


            val intent = Intent(Intent.ACTION_VIEW)
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".rar")) {
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".jpg") || url.toString()
                    .contains(".jpeg") || url.toString().contains(".png")
            ) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                url.toString().contains(".mpeg") || url.toString()
                    .contains(".mpe") || url.toString().contains(".mp4") || url.toString()
                    .contains(".avi")
            ) {
                // Video files
                intent.setDataAndType(uri, "video/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                intent.setDataAndType(uri, "*/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No application found which can open the file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Function for getting date in yyyy-mm-dd HH:mm:ss format
     * */
    fun ConvertDateInToYYMMDD(getDate: String): Date? {
        var convertedDate: Date? = null
        try {
            convertedDate = SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.ENGLISH).parse(getDate)
        } catch (e: Exception) {

        }

        return convertedDate

    }

    /**
     * Function for getting date in dd/mm/yyyy format
     * */
    fun ConvertDateInToDDMMYY(getDate: String): Date? {
        var convertedDate: Date? = null
        try {
            convertedDate = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(getDate)
        } catch (e: Exception) {

        }

        return convertedDate

    }

    /**
     * Function for getting formatted date
     * */
    fun getFormattedDate(mDate: Date): String {
        val daysFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return daysFormat.format(mDate)
    }

    /**
     * Function getting formatted date in string
     * */
    fun getFormattedStringFromString(day: Int, month: Int, year: Int): String {
        val dateString = "$year-$month-$day"
        val mDate = Date(dateString)
        val daysFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return daysFormat.format(mDate)
    }

    /**
     * Function for getting width in pixels
     * */
    fun getWidthInPixel(context: Context, dp: Float): Int {
        val metrics = context.resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.toInt()
    }

    /**
     * Function for getting formatted date
     * */
    fun getFormattedDate(
        date: String,
        currentFormat: String,
        returnFormat: String,
        localeEnglish: Boolean
    ): String {
        var local = Locale.ENGLISH
        return try {
            val currentFormatConvert = SimpleDateFormat(currentFormat, local)
            val formattedDate = currentFormatConvert.parse(date)
            val expectedFormatReturn = SimpleDateFormat(returnFormat, local)
            expectedFormatReturn.format(formattedDate!!)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getFormattedDateWithSuffix(
        date: String,
        format: String
    ): String {
        var local = Locale.ENGLISH
        return try {
            val currentFormatConvert = SimpleDateFormat(format, local)
            val formattedDate = currentFormatConvert.parse(date)
            val parts = date
            val partFirst = parts.split(",")[0]
            val partSecond = parts.split(",")[1] + SimpleDateFormat(
                "d",
                local
            ).format(formattedDate).toInt().getDaySuffix()
            val partThird = parts.split(",")[2]
            "$partFirst, $partSecond, $partThird "
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    /**
     * Function for killing all toasts
     * */
    private fun killAllToast() {
        for (t in msjsToast) {
            t.cancel()
        }
        msjsToast.clear()
    }

    /**
     * Function for showing toast in center
     */
    fun showLongToastInCenter(ctx: Context, message: String?) {
        //message = Strings.nullToEmpty( message );
        val toast = Toast.makeText(ctx, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        if (msjsToast.isNotEmpty()) {
            killAllToast()
        }
        toast.show()
        msjsToast.add(toast)
    }

    /**
     * Function for showing toast
     * */
    fun showToast(ctx: Context, message: String?) {
        //message = Strings.nullToEmpty( message );
        val toast = Toast.makeText(ctx, message, Toast.LENGTH_LONG)
        toast.show()
    }


    /**
     * Function for hiding the keyboard
     * */
    fun hideSoftKeyboard(context: Context, editText: EditText) {

        val imm = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)

    }

    /**
     * Function for showing call alert
     * */
    fun callAlert(context: Activity, msg: String) {
        val alertDialog = AlertDialog.Builder(context)


        // Setting Dialog Message
        alertDialog.setMessage(msg)

        // Setting OK Button
        alertDialog.setPositiveButton("Call") { dialog, which -> call(context, msg) }

        alertDialog.setNegativeButton("Cancel", null)
        alertDialog.create()

        // Showing Alert Message
        alertDialog.show()
    }

    /**
     * Function for showing soft keyboard
     * */
    fun showSoftKeyboard(context: Context, editText: EditText) {
        val imm = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInputFromWindow(editText.windowToken, InputMethodManager.SHOW_IMPLICIT, 0)
    }

    /**
     * Function for hiding soft keyboard
     * */
    fun hideSoftKeyboard(context: Context, view: View?) {
        val imm = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (view != null)
            imm.hideSoftInputFromWindow(view.windowToken, 0)

    }

    /**
     * Function for locateView
     * */
    fun locateView(v: View?): Rect? {
        val loc_int = IntArray(2)
        if (v == null)
            return null
        try {
            v.getLocationOnScreen(loc_int)
        } catch (npe: NullPointerException) {
            // Happens when the view doesn't exist on screen anymore.
            return null
        }

        val location = Rect()
        location.left = loc_int[0]
        location.top = loc_int[1]
        location.right = location.left + v.width
        location.bottom = location.top + v.height
        return location
    }

    /**
     * Function for animate layout from invisible state to visible state
     * */
    fun animateRise(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 250
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, -1.0f
        )
        animation.duration = 500
        set.addAnimation(animation)

        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                mLayout.visibility = View.INVISIBLE
            }
        })

        mLayout.startAnimation(set)

    }

    /**
     * Function for animate layout from top to bottom of the screen
     * */
    fun animateFall(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 250
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        animation.duration = 500
        set.addAnimation(animation)

        mLayout.startAnimation(set)

    }

    /**
     * Function for animate layout to sliding down position
     * */
    fun animateLayoutSlideDown(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 250
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        animation.duration = 150
        set.addAnimation(animation)

        val controller = LayoutAnimationController(
            set, 0.25f
        )
        mLayout.layoutAnimation = controller

    }

    /**
     * Function for animate right
     * */
    fun animateLayoutSlideToRight(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 750
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )

        animation.duration = 750
        set.addAnimation(animation)

        val controller = LayoutAnimationController(
            set, 0.25f
        )
        mLayout.layoutAnimation = controller

    }

    /**
     * Function for animate from right
     * */
    fun animateLayoutSlideFromRight(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 750
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )

        animation.duration = 750
        set.addAnimation(animation)

        val controller = LayoutAnimationController(
            set, 0.25f
        )
        mLayout.layoutAnimation = controller

    }

    /**
     * Function for animate layout to left
     * */
    fun animateLayoutSlideToLeft(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 750
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )

        animation.duration = 750
        set.addAnimation(animation)

        val controller = LayoutAnimationController(
            set, 0.25f
        )
        mLayout.layoutAnimation = controller

    }

    /**
     * Function for animate layout from right
     * */
    fun animateFromRight(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 250
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        animation.duration = 500
        set.addAnimation(animation)

        mLayout.startAnimation(set)

    }

    /**
     * Function for animate layout to right
     * */
    fun animateToRight(mLayout: ViewGroup) {

        val set = AnimationSet(true)

        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 250
        set.addAnimation(animation)

        animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        animation.duration = 500
        set.addAnimation(animation)

        mLayout.startAnimation(set)

    }


    /**
     * Function for getting power of two
     * */
    private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
        val k = Integer.highestOneBit(floor(ratio).toInt())
        return if (k == 0)
            1
        else {
            k
        }
    }

    /**
     * Function for converting uri to drawable
     * */
    fun convertUriToDrawable(context: Context, imageUri: Uri): Drawable? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            Drawable.createFromStream(inputStream, imageUri.toString())
        } catch (e: FileNotFoundException) {
            null
        }

    }

    /**
     * Function for save image
     * */
    fun saveImage(_bitmap: Bitmap, fileName: String, context: Context): File {
        val file = File(fileName)
        ValidateFolderExist(file.parent)
        var outStream: FileOutputStream? = null
        try {
            file.createNewFile()
            outStream = FileOutputStream(file)
            _bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
            outStream.flush()
            outStream.close()
            //addImageToGallery(file.getAbsolutePath(), context);
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    /**
     * Function for checking folder exists
     * */
    fun ValidateFolderExist(folderPath: String) {
        val dir = File(folderPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    /**
     * Function for opening dialer
     * */
    fun call(activity: Activity, number: String) {

        val call = Uri.parse("tel:$number")
        val surf = Intent(Intent.ACTION_DIAL, call)
        activity.startActivity(surf)
    }

    /**
     * Function for checking validity of email
     * */
    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    /**
     * Function for opening activity with empty stack
     * */
    fun openAndClearActivity(activity: Activity, calledActivity: Class<*>) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(myIntent)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity with empty stack
     * */
    fun openAndClearActivity(activity: Activity, calledActivity: Class<*>, isAnimation: Boolean) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(myIntent)
        if (isAnimation) {
            activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
        }
    }

    /**
     * Function for opening activity with empty stack
     * */
    fun openAndClearActivity(activity: Activity, calledActivity: Class<*>, bundle: Bundle) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        myIntent.putExtras(bundle)
        activity.startActivity(myIntent)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity
     * */
    fun openActivity(activity: Activity, calledActivity: Class<*>) {
        val myIntent = Intent(activity, calledActivity)
        activity.startActivity(myIntent)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity
     * */
    fun openActivity(activity: Activity, calledActivity: Class<*>, bundle: Bundle) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.putExtras(bundle)
        activity.startActivity(myIntent)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity
     * */
    fun openActivity(activity: Activity, calledActivity: Class<*>, singleTask: Boolean) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(myIntent)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity
     * */
    fun openActivity(
        activity: Activity,
        calledActivity: Class<*>,
        singleTask: Boolean,
        bundle: Bundle
    ) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.putExtras(bundle)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(myIntent)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity with new task
     * */
    fun openActivityNewTask(
        activity: Activity,
        calledActivity: Class<*>,
        bundle: Bundle
    ) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.putExtras(bundle)
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(myIntent)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity for result
     * */
    fun openActivityForResult(
        activity: Activity, calledActivity: Class<*>, singleTask: Boolean,
        code: Int, bundle: Bundle
    ) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.putExtras(bundle)
        //myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivityForResult(myIntent, code)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity for result
     * */
    fun openActivityForResult(
        activity: Activity, calledActivity: Class<*>, singleTask: Boolean,
        code: Int
    ) {
        val myIntent = Intent(activity, calledActivity)
        activity.startActivityForResult(myIntent, code)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity for result
     * */
    fun openActivityForResult(
        activity: Activity,
        calledActivity: Class<*>,
        bundle: Bundle,
        code: Int
    ) {
        val myIntent = Intent(activity, calledActivity)
        myIntent.putExtras(bundle)
        activity.startActivityForResult(myIntent, code)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity for result
     * */
    fun openActivityForResult(
        activity: Activity,
        calledActivity: Class<*>,
        code: Int
    ) {
        val myIntent = Intent(activity, calledActivity)
        activity.startActivityForResult(myIntent, code)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for opening activity for result
     * */
    fun openActivity(activity: Activity, calledActivity: Class<*>, code: Int) {
        val myIntent = Intent(activity, calledActivity)
        activity.startActivityForResult(myIntent, code)
        activity.overridePendingTransition(R.anim.animation_enter, R.anim.animation_exist)
    }

    /**
     * Function for hiding action bar
     * */
    fun hideActionBar(actionBar: ActionBar?) {
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.hide()
    }

    /**
     * Function for converting object to json
     * */
    fun jsonConverterObject(data: LinkedTreeMap<*, *>?): String {
        val jsonObj = JSONObject(data)
        val json = jsonObj.toString()
        return json
    }

    /**
     * Function for requesting permissions
     * */
    fun requestPermission(PERMISSION: Array<String>, context: Activity) {
        ActivityCompat.requestPermissions(context, PERMISSION, PERMISSION_CODE)
    }

    /**
     * Function for requesting permission
     * */
    fun requestPermission(PERMISSION: String, context: Activity) {
        ActivityCompat.requestPermissions(context, arrayOf(PERMISSION), PERMISSION_CODE)
    }

    /**
     * Function for creating multipart object
     * */
    fun multipartImage(path: String, name: String): MultipartBody.Part {
        val file = File(path)
        val mediaType = "Image/*".toMediaTypeOrNull()
        val requestBody: RequestBody = file.asRequestBody(mediaType)
        return MultipartBody.Part.createFormData(
            name,
            file.name,
            requestBody
        )
    }

    /**
     * Function for loading image
     * */
    fun glideLoadImage(context: Context, url: String, imageView: ImageView) {
//        Log.e("Image Loading", "URl =" + url)
        if (!url.contains("null")) {
            Glide
                .with(context)
                .load(url)
                .apply(
                    RequestOptions()
//                    .placeholder(R.drawable.bg_gradient)
//                    .error(R.drawable.bg_gradient)
                )
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            glideLoadImageFailed(
//                                MainApplication.applicationContext(),
//                                url,
//                                imageView
//                            )
////                            Log.e("Image Loading FAiled", "URL 123 =" + url)
//                        }, 500)
//                    Handler().postDelayed(Runnable { updateImage(image) }, 1000)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        }
    }

    /**
     * Function for loading the image
     * */
    fun glideLoadImageFailed(context: Context, url: String, imageView: ImageView) {

        if (!url.contains("null")) {
            Glide
                .with(context)
                .load(url)
                .apply(
                    RequestOptions()
                )
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        }
    }

    /**
     * Function for checking permission is granted
     * */
    fun isPermissionAllowed(context: Context, PERMISSION: String) = ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED


    /**
     * Function for checking permissions granted
     * */
    fun isPermissionAllowed(context: Context, PERMISSION: Array<String>): Boolean {
        var permissionGranted = false
        if (ContextCompat.checkSelfPermission(context, PERMISSION[0])
            == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
        }
        return permissionGranted
    }

    /**
     * Function for checking user logged in or not
     * */
    fun userLoggedIn(preferenceHelper: PreferenceHelper): Boolean {
        try {
            val userModel = preferenceHelper.getObject(
                Constants.USER_MODEL,
                UserModel::class.java
            ) as? UserModel
            if (userModel != null) {
                return userModel.access_token.isNotEmpty()
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Function for spannable string font changing
     * */
    fun spanString(targetStr: String, lookFor: String, font_: Int, context: Context): Spannable? {
        var spanString: Spannable? = null
        spanString = SpannableString(targetStr)
        var fromIndex: Int
        var startSpan: Int
        var endSpan: Int
        fromIndex = 0
        val font: Typeface = ResourcesCompat.getFont(context, font_)!!
        while (fromIndex < targetStr.length - 1) {
            startSpan = targetStr.indexOf(lookFor, fromIndex)
            if (startSpan == -1) break
            endSpan = startSpan + lookFor.length
            spanString.setSpan(
                CustomTypefaceSpan(font), startSpan,
                endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            fromIndex = endSpan
        }
        return spanString
    }

    /**
     * Function for spannable string coloring
     * */
    fun spanStringColor(targetStr: String, lookFor: String, color: Int): Spannable? {
        var spanString: Spannable? = null
        spanString = SpannableString(targetStr)
        var fromIndex: Int
        var startSpan: Int
        var endSpan: Int
        fromIndex = 0

        while (fromIndex < targetStr.length - 1) {
            startSpan = targetStr.indexOf(lookFor, fromIndex)
            if (startSpan == -1) break
            endSpan = startSpan + lookFor.length
            spanString.setSpan(
                ForegroundColorSpan(color),
                startSpan,
                endSpan,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            fromIndex = endSpan
        }
        return spanString
    }

    /**
     * Function for setting spinner adapter
     * */
    fun setSpinner(activity: Activity, list: ArrayList<String>, spinner: Spinner) {
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            activity,
            R.layout.spinner_simple_item, list
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    /**
     * Function for setting spinner with new layout
     * */
    fun setSpinnerWithNewLayout(activity: Activity, list: ArrayList<String>, spinner: Spinner) {
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            activity,
            R.layout.new_spinner_simple_item, list
        )
        adapter.setDropDownViewResource(R.layout.new_simple_spinner_drop_down_item)
        spinner.adapter = adapter
    }

    fun setSpinnerWithNewText(activity: Activity, list: ArrayList<String>, spinner: Spinner) {
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            activity,
            R.layout.simple_spinner_new_text, R.id.tvText1, list
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    /**
     * Function for creating multipart object list
     * */
    fun multiPartArrayList(list: ArrayList<Uri>, name: String): ArrayList<MultipartBody.Part> {
        val listMultipart = ArrayList<MultipartBody.Part>()
        for (i in list.indices) {
            val file = File(list[i].path.toString())
            val mediaType = "Image/*".toMediaTypeOrNull()
            val requestBody: RequestBody = file.asRequestBody(mediaType)
            listMultipart.add(
                MultipartBody.Part.createFormData(
                    "$name[$i]",
                    name + "." + file.extension,
                    requestBody
                )
            )
        }
        return listMultipart
    }

    fun multiPartArrayList(list: ArrayList<Uri>): ArrayList<MultipartBody.Part> {
        val name = ApiInterface.Companion.ParamNames.CAR_PHOTO
        val listMultipart = ArrayList<MultipartBody.Part>()
        for (i in list.indices) {
            val file = File(list[i].path.toString())
            val mediaType = "Image/*".toMediaTypeOrNull()
            val requestBody: RequestBody = file.asRequestBody(mediaType)
            listMultipart.add(
                MultipartBody.Part.createFormData(
                    "$name[$i][name]",
                    file.name,
                    requestBody
                )
            )
        }
        return listMultipart
    }

    fun multiPart(uri: Uri, name: String): ArrayList<MultipartBody.Part> {
        val listMultipart = ArrayList<MultipartBody.Part>()
        val file = File(uri.path.toString())
        val mediaType = "Image/*".toMediaTypeOrNull()
        val requestBody: RequestBody = file.asRequestBody(mediaType)
        listMultipart.add(
            MultipartBody.Part.createFormData(
                "$name[]",
                file.name + "." + file.extension,
                requestBody
            )
        )
        return listMultipart
    }

    fun multiPartFile(uri: Uri, name: String): ArrayList<MultipartBody.Part> {
        val listMultipart = ArrayList<MultipartBody.Part>()
        val file = File(uri.path.toString())
        val mediaType = "*/*".toMediaTypeOrNull()
        val requestBody: RequestBody = file.asRequestBody(mediaType)
        listMultipart.add(
            MultipartBody.Part.createFormData(
                "$name[]",
                "new file" + "." + file.extension,
                requestBody
            )
        )
        return listMultipart
    }

    /**
     * Function for creating multipart object list
     * */
    fun multiPartArrayListSingle(
        list: ArrayList<Uri>,
        name: String,
        index: Int
    ): ArrayList<MultipartBody.Part> {
        val listMultipart = ArrayList<MultipartBody.Part>()
        for (i in list.indices) {
            val file = File(list[i].path)
            val mediaType = "Image/*".toMediaTypeOrNull()
            val requestBody: RequestBody = file.asRequestBody(mediaType)
            listMultipart.add(
                MultipartBody.Part.createFormData(
                    "$name[$index]",
                    name + "." + file.extension,
                    requestBody
                )
            )
        }
        return listMultipart
    }

    /**
     * Function for creating multipart object list
     * */
    fun multiPartArrayList(
        list: ArrayList<Uri>,
        name: String,
        indexList: ArrayList<Int>
    ): ArrayList<MultipartBody.Part> {
        val listMultipart = ArrayList<MultipartBody.Part>()
        for (i in list.indices) {
            val file = File(list[i].path)
            val mediaType = "Image/*".toMediaTypeOrNull()
            val requestBody: RequestBody = file.asRequestBody(mediaType)
            listMultipart.add(
                MultipartBody.Part.createFormData(
                    "$name[${indexList[i]}]",
                    file.name,
                    requestBody
                )
            )
        }
        return listMultipart
    }

    /**
     * Function for converting uri to multipart
     * */
    fun uriToMultipart(uri: Uri, name: String): MultipartBody.Part {
        val file = fileFromContentUri(MainApplication.applicationContext(), uri)
        val mediaType = "Image/*".toMediaTypeOrNull()
        val requestBody: RequestBody = file.asRequestBody(mediaType)
        return MultipartBody.Part.createFormData(
            name,
            file.name,
            requestBody
        )
    }

    /**
     * Function for creating getting file created in cache directory
     * */
    fun fileFromContentUri(context: Context, contentUri: Uri): File {
        // Preparing Temp file name
        val fileExtension = getFileExtension(context, contentUri)
        val fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""

        // Creating Temp file
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }

    /**
     * Function for getting file extension
     * */
    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    /**
     * Function for copying file
     * */
    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }

    /**
     * Function for show date picker with limit
     * */
    fun showDatePickerWithLimit(
        activity: Activity?,
        callback: PickerCallback?,
        maxLimit: Boolean,
        minLimit: Boolean
    ) {
        val yy: Int
        val mm: Int
        val dd: Int
        val calendar = Calendar.getInstance()
        yy = calendar[Calendar.YEAR]
        mm = calendar[Calendar.MONTH]
        dd = calendar[Calendar.DATE]
        val dialog =
            DatePickerDialog(activity!!, { view, year, monthOfYear, dayOfMonth ->
                val monthOfYearNew = monthOfYear + 1
                var d = dayOfMonth.toString() + ""
                var m = monthOfYearNew.toString() + ""
                if (monthOfYearNew + "".length == 1) {
                    m = "0$monthOfYearNew"
                }
                if (dayOfMonth + "".length == 1) {
                    d = "0$dayOfMonth"
                }
                if (callback != null) {
                    @SuppressLint("SimpleDateFormat") val format =
                        SimpleDateFormat(dateFormatDisplay)
                    calendar[year, monthOfYearNew - 1] = dayOfMonth
                    val strDate = format.format(calendar.time)
                    callback.onSelected(strDate) // getMonth(monthOfYear)
                }
            }, yy, mm, dd)
        dialog.setOnCancelListener { dialog1 ->
            // callback?.onSelected("")
            dialog1.dismiss()
        }
        if (maxLimit) {
            dialog.datePicker.maxDate = System.currentTimeMillis() - 1000
        } else if (minLimit) {
            calendar.add(Calendar.DAY_OF_WEEK, 0)
            dialog.datePicker.minDate = calendar.timeInMillis
        }
        dialog.show()
    }

    /**
     * Function for loading image
     * */
    fun loadImage(imageView: ImageView, path: String) {
        Glide.with(imageView)
            .asBitmap()
            .load(path)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(imageView)
    }

    /**
     * Function for loading image
     * */
    fun loadSimpleImage(imageView: ImageView, path: String) {
        Glide.with(imageView)
            .load(path)
            .into(imageView)
    }


    /**
     * Function for converting bitmap to file
     * */
    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + fileNameToSave
            )
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    /**
     * Function for replacing fragment instance
     * */
    fun replaceFragment(context: Activity, id: Int, fragment: Fragment) {

        val fragmentTransaction =
            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(id, fragment)
        fragmentTransaction.setCustomAnimations(
            R.anim.animation_enter,
            R.anim.animation_exist,
            R.anim.slide_in_from_left,
            R.anim.slide_out_to_right
        )
        fragmentTransaction.commit()
    }

    /**
     * Function for adding fragment into stack and maintaining instance
     * */
    fun addFragment(context: Activity, id: Int, fragment: Fragment) {

        val fragmentTransaction =
            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        fragmentTransaction.add(id, fragment)
        fragmentTransaction.setCustomAnimations(
            R.anim.animation_enter,
            R.anim.animation_exist,
            R.anim.slide_in_from_left_short,
            R.anim.slide_out_to_right_short
        )
        fragmentTransaction.commit()
    }

    /**
     * Function for add Fragment and maintaining instance
     * */
    fun addFragment(
        frame: Int,
        fragment: Fragment,
        fragmentManager: FragmentManager
    ) {
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.animation_enter,
                R.anim.animation_exist,
                R.anim.slide_in_from_left_short,
                R.anim.slide_out_to_right_short
            )
            .add(frame, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Function for adding fragment in specific stack
     * */
    fun addFragmentWithBackStack(
        frame: Int,
        fragment: Fragment,
        fragmentManager: FragmentManager,
        stackName: String
    ) {
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.animation_enter,
                R.anim.animation_exist,
                R.anim.slide_in_from_left_short,
                R.anim.slide_out_to_right_short
            )
            .add(frame, fragment)
            .addToBackStack(stackName)
            .commit()
    }
/*    fun getCurrentDate(): String {
        val c = Calendar.getInstance().time
        val df =
            SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val formattedDate = df.format(c)
        return formattedDate
    }*/

    companion object {

        /**
         * Function for opening google map for navigation intent
         * */
        fun getRouteFromCurrent(context: Context) {
            val navigationIntentUri =
                Uri.parse("google.navigation_menu:q=" + 24.6859931 + "," + 46.7019997)//creating intent with latlng
            val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        }
    }

    /**
     * Function for opening activity for web view
     * */
    fun openUrl(activity: Activity, url: String) {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }

    /**
     * Function for counting lines
     * */
    fun countLines(textView: TextView): Int {
        return Math.ceil(
            textView.paint.measureText(textView.text.toString()) /
                    textView.measuredWidth.toDouble()
        ).toInt()
    }

    /**
     * Function for opening googleMap
     * */
    fun openGoogleMap(context: Context) {
        val navigationIntentUri =
            Uri.parse("google.navigation_menu:q=" + 24.6859931 + "," + 46.7019997)//creating intent with latlng
        val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    }

    /**
     * Function for opening google map
     * */
    fun openMaps(context: Context, lat: Double?, lng: Double?) {
        try {
            val url = "https://maps.google.com/maps?q=loc:$lat,$lng"
            val navigationIntentUri =
                Uri.parse(url)//creating intent with latlng
            val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
            mapIntent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            )
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            showLongToastInCenter(context, e.localizedMessage)
            e.printStackTrace()
        }
    }

    /**
     * Function for open direction directly on google map
     * */
    fun openDirectionDirectly(context: Context, lat: Double, lng: Double) {
        try {
            val navigationIntentUri =
                Uri.parse("google.navigation:q=" + lat + "," + lng) //creating intent with latlng

            val mapIntent =
                Intent(Intent.ACTION_VIEW, navigationIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            showLongToastInCenter(context, e.localizedMessage)
        }
    }

    /**
     * Function for get Formatted Date
     * */
    @SuppressLint("SimpleDateFormat")
    fun getFormattedDate(date: String, inputFormat: String, outputFormat: String): String {
        val simpleDateFormat = SimpleDateFormat(inputFormat)
        val convertDateFormat = SimpleDateFormat(outputFormat)
        val date = simpleDateFormat.parse(date)
        return try {
            convertDateFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Function for showing popup
     * */
    fun showPopup(
        activity: Context,
        callback: PopupCallback,
        positiveButtonId: Int,
        negativeButtonId: Int,
        titleId: Int,
        messageId: Int,
        isLogout: Boolean
    ) {
        val popup = Dialog(activity, R.style.full_screen_dialog)
        popup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popup.setCancelable(false)
        popup.setContentView(R.layout.popup_logout)
        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val crossBtn: ImageButton? = popup.findViewById(R.id.cross)
        if (isLogout) {
            crossBtn!!.visibility = View.VISIBLE
        } else {
            crossBtn!!.visibility = View.GONE
        }
        val yesBtn: Button? = popup.findViewById(R.id.btnYes)
        val help: TextView? = popup.findViewById(R.id.help)
        val guidelines: TextView? = popup.findViewById(R.id.guidlines)
        yesBtn!!.text = activity.getString(positiveButtonId)
        guidelines!!.text = activity.getString(messageId)
        help!!.text = activity.getString(titleId)
        val noBtn: Button? = popup.findViewById(R.id.btnNo)
        noBtn!!.text = activity.getString(negativeButtonId)

        crossBtn.setOnClickListener {
            try {
                popup.dismiss()
                callback.popupButtonClick(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        yesBtn.setOnClickListener {
            try {
                popup.dismiss()
                callback.popupButtonClick(1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        noBtn.setOnClickListener {
            try {
                popup.dismiss()
                callback.popupButtonClick(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            popup.show()
        } catch (e: Exception) {
            Log.e("PopUpException", e.localizedMessage ?: "")
            e.printStackTrace()
        }

    }

    /**
     * Function for showing search car makes popup
     * */
    fun showMakeSearchPopUp(
        activity: Activity,
        callback: PopupCallback,
        list: List<String>
    ) {
        var recyclerList: List<String> = list

        val binding = CarMakesSearchPopUpBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.car_makes_search_pop_up, null)
        )
        val popup = Dialog(activity, R.style.full_screen_dialog)
//        popup.window?.setSoftInputMode()
        popup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popup.setCancelable(false)
        popup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.setContentView(binding.root)
        popup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        popup.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        )

        binding.search.requestFocus()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = MakeAdapter(activity, recyclerList, object : ClickListener {
                override fun onClick(position: Int) {
                    callback.popupButtonClick(position, recyclerList[position])
                    popup.dismiss()
                }
            })

        }
        binding.search.addTextChangedListener {
            val adapter = binding.recyclerView.adapter as MakeAdapter
            if (it?.isNotEmpty() == true) {
                recyclerList = filterList(it.toString(), ArrayList(list))
                if (recyclerList.isNotEmpty()) {
                    adapter.changeList(recyclerList)
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.noDataLayout.visibility = View.GONE
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.noDataLayout.visibility = View.VISIBLE
                }
            } else {
                binding.noDataLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                recyclerList = list
                adapter.changeList(recyclerList)
            }
        }

        binding.search.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(activity)
                    return if (recyclerList.isNotEmpty() && binding.search.text.toString()
                            .isNotEmpty()
                    ) {
                        callback.popupButtonClick(0, recyclerList[0])
                        popup.dismiss()
                        true
                    } else if (binding.search.text.toString()
                            .isNotEmpty() && recyclerList.isEmpty()
                    ) {
                        showLongToastInCenter(
                            activity,
                            activity.getString(R.string.please_enter_valid_query)
                        )
                        false
                    } else {
                        showLongToastInCenter(
                            activity,
                            activity.getString(R.string.please_enter_your_query)
                        )
                        false
                    }
                }
                return false
            }
        })


        binding.close.setOnClickListener {
            popup.dismiss()
        }

        try {
            popup.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Function for filtering list
     * */
    private fun filterList(input: String, list: ArrayList<String>): List<String> {
        val filteredList = list.filter {
            it.startsWith(input, true)
        }
        return filteredList
    }

    /**
     * Function for showing enable location settings
     * */
    fun showEnableLocationSetting(
        activity: Activity,
    ) {
        activity.let {
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
                        Log.e("GPS", "Fail Response")
                        e.startResolutionForResult(
                            it,
                            MainActivity.LOCATION_SETTING_REQUEST
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                    }
                }
            }
        }
    }
}


