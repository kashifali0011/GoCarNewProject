package com.towsal.towsal.views.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.databinding.DataBindingUtil
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.ActivitySplashBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.utils.Constants
import kotlinx.android.synthetic.main.activity_splash.*

/**
 * Activity class for splash screen
 * */
class SplashActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        uiHelper.hideActionBar(supportActionBar)
        MainApplication.appOpen = true
        binding.activity = this
    }

    override fun onResume() {
        super.onResume()
        setUpView()
    }

    /**
     * Function for setting up view
     * */
    private fun setUpView() {
        val update = Handler(Looper.getMainLooper())
        update.postDelayed(
            {
                val extras = intent.extras
                val trigger = extras?.get(Constants.TRIGGER_NOTIFICATION)
                val trigger2 = extras?.get("google.delivered_priority") as? String?
                val isFirstRun =
                    preferenceHelper.getBolOther(Constants.DataParsing.IS_FIRST_RUN, true)
                if (isFirstRun) {
                    btnPrimaryButton.visibility = View.VISIBLE
                    btnPrimaryButton.isEnabled = true
                } else {
                    val userModel = preferenceHelper.getObject(
                        Constants.USER_MODEL,
                        UserModel::class.java
                    ) as? UserModel
                    if (userModel != null && userModel.access_token.isNotEmpty()) {
                        MainApplication.isLoggedIn = true
                        if ((trigger != null && trigger.toString()
                                .toInt() != 0) || !trigger2.isNullOrEmpty()
                        ) {
                            MainActivity.GOTO_FRAGMENT = MainActivity.GOTO_TRIPS_FRAGMENT
                            //Main Activity
                            uiHelper.openAndClearActivity(this, MainActivity::class.java, false)
                        } else {
                            //redirecting straight to the main activity not on carsearch
                            uiHelper.openAndClearActivity(this, MainActivity::class.java, false)
                        }
                    } else {
                        MainApplication.isLoggedIn = false
                        //Login Activity
                        uiHelper.openAndClearActivity(this, LoginActivity::class.java, false)
                    }
                }
            }, 3000
        )
    }

    /**
     * Function for click listeners
     * */
    override fun onClick(view: View?) {
        view?.preventDoubleClick()
        uiHelper.openAndClearActivity(this, LanguageSelectionActivity::class.java)
    }
}