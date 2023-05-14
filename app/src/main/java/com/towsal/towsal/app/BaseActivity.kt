package com.towsal.towsal.app

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.anggrayudi.storage.SimpleStorageHelper
import com.oppwa.mobile.connect.exception.PaymentError
import com.towsal.towsal.R
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.Network
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.GPSTracker
import com.towsal.towsal.views.activities.LoginActivity
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.android.inject
import java.io.IOException
import java.net.SocketException

/**
 * Every activity should extend from base activity which contains some generic functions like initRxErrorHandler
 */
abstract class BaseActivity : LocalizationActivity() {
    val network: Network by inject()
    val preferenceHelper: PreferenceHelper by inject()
    val uiHelper: UIHelper by inject()
    var setActionBar: SetActionBar? = null
    var progressDialog: Dialog? = null
    var count = 0
    val storageHelper = SimpleStorageHelper(this)

    val PERMISSION =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    var languageSelected = ""

    companion object {
        var gpsTracker: GPSTracker? = null
        lateinit var activity: LocalizationActivity
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val extras = intent.extras
            when (extras?.getInt(Constants.TYPE)) {
                Constants.CHANGE_LANGUAGE -> {
                    val language = extras.getString(Constants.LANGUAGE)
                    if (language != null)
                        setLanguage(language)
                }
                Constants.SHOW_LOADER -> {
                    showLoaderDialog()
                }
                Constants.HIDE_LOADER -> {
                    hideLoaderDialog()
                }
                Constants.ResponseCodes.UNAUTHORIZED -> {
                    when (activity) {
                        is LoginActivity -> {

                        }
                        else -> {
                            preferenceHelper.clearAllPreferences()
                            uiHelper.openAndClearActivity(
                                this@BaseActivity,
                                LoginActivity::class.java
                            )
                        }
                    }

                }
                Constants.MESSAGE ->{
                    val threadId = extras.getString(Constants.DataParsing.THREAD_ID)
                    val messageId = extras.getString(Constants.DataParsing.MESSAGE_ID)
                    val textMessage = extras.getString(Constants.DataParsing.TEXT_MESSAGE)

                    if (threadId != null && messageId != null && textMessage != null )
                        message(threadId , messageId , textMessage)
                }
            }


        }
    }
    open fun message(threadId: String, messageId: String, textMessage: String) {}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        initRxErrorHandler()
        languageSelected =
            preferenceHelper.getLanguage(Constants.LANGUAGE, Constants.ENGLISH).toString()
        gpsTracker = GPSTracker(this)
        registerReceiver(
            broadcastReceiver,
            IntentFilter(Constants.BROCAST_RECIEVER)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.cancel()
        }
    }

    /**
     * Function for handling api errors
     * */
    private fun initRxErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable ->

            if (throwable is NullPointerException ||
                throwable is IllegalArgumentException ||
                throwable is IllegalStateException ||
                throwable is UndeliverableException
            ) {
                // that's likely a bug in the application or bug in rxjava or the request is cancelled
                throwable.cause?.let {
                    Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                        Thread.currentThread(),
                        it
                    )
                }
                return@setErrorHandler
            }

            if (throwable is IOException ||
                throwable is SocketException ||
                throwable is InterruptedException
            ) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }


            Log.w("Undeliverable exception", throwable)
        }
    }

    /**
     * Function for showing progress dialog
     * */
    fun showLoaderDialog() {
        count += 1
        if (count == 1) {
            progressDialog = Dialog(this, R.style.full_screen_dialog)
            progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialog?.setCancelable(false)
            progressDialog?.setContentView(R.layout.progress_dialog_layout)
            progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            if (progressDialog != null) {
                try {
                    if (!progressDialog?.isShowing!! && !this.isDestroyed)
                        progressDialog?.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Function for hiding progress dialog
     * */
    fun hideLoaderDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            try {
                progressDialog?.dismiss()
                progressDialog = null
                count = 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Function for showing alert dialog with custom message string parameter
     * */
    protected fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .setCancelable(false)
            .show()
    }

    /**
     * Function for showing alert dialog with custom resource id as parameter
     * */
    protected fun showAlertDialog(message: Int) {
        AlertDialog.Builder(this)
            .setMessage(getString(message))
            .setPositiveButton(R.string.ok, null)
            .setCancelable(false)
            .show()
    }

    /**
     * Function for showing the error dialog
     * */
    fun showErrorDialog(message: String) {
        runOnUiThread { showAlertDialog(message) }
    }

    /**
     * Function for showing the error dialog
     * */
    fun showErrorDialog(paymentError: PaymentError) {
        runOnUiThread { showErrorDialog(paymentError.errorMessage) }
    }


}