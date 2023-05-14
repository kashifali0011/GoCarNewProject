package com.towsal.towsal.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.extensions.preventDoubleClick
import kotlinx.android.synthetic.main.activity_host_help.*

/**
 * Activity class for host help
 * */
class HostHelpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_help)

        findViewById<Button>(R.id.btnPhone).setOnClickListener {
            it.preventDoubleClick()
            openDialer()
        }

        findViewById<Button>(R.id.btnEmail).setOnClickListener {
            it.preventDoubleClick()
            openMailBox()
        }

        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            it.preventDoubleClick()
            this.finish()
        }
    }

    /**
     * Function for mail box
     * */
    private fun openMailBox() {
//        //changing button transition
//        btnEmail.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
//        btnEmail.setBackgroundResource(R.drawable.button_green_bg)
//
//        //changing button transition
//        btnPhone.setTextColor(ContextCompat.getColor(applicationContext, R.color.grey))
//        btnPhone.setBackgroundResource(R.drawable.button_primary_bg)

        //get input from EditTexts and save in variables
        val recipient = "support@gocar.sa"
        val subject = "Help Call"

        //method call for email intent with these inputs as parameters
        sendEmail(recipient, subject)
    }

    /**
     * Function for opening of the phone dialer
     * */
    private fun openDialer() {
        //changing button transition
        btnPhone.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        btnPhone.setBackgroundResource(R.drawable.button_green_bg)

        //changing button transition
        btnEmail.setTextColor(ContextCompat.getColor(applicationContext, R.color.grey))
        btnEmail.setBackgroundResource(R.drawable.button_primary_bg)

        val phone = " +9661111111"
        val u: Uri = Uri.parse("tel:" + phone.toString())
        val i = Intent(Intent.ACTION_DIAL, u)

        try {
            // Launch the Phone app's dialer with a phone
            // number to dial a call.
            startActivity(i)
        } catch (s: SecurityException) {
            // show() method display the toast with
            // exception message.
            Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.btnPhone -> {

                //changing button transition
                btnPhone.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorWhite
                    )
                )
                btnPhone.setBackgroundResource(R.drawable.button_green_bg)

                val phone = "+923216675432"
                val u: Uri = Uri.parse("tel:" + phone.toString())
                val i = Intent(Intent.ACTION_DIAL, u)

                try {
                    // Launch the Phone app's dialer with a phone
                    // number to dial a call.
                    startActivity(i)
                } catch (s: SecurityException) {
                    // show() method display the toast with
                    // exception message.
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG)
                        .show()
                }
            }
            R.id.btnEmail -> {

                //changing button transition
                btnEmail.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorWhite
                    )
                )
                btnEmail.setBackgroundResource(R.drawable.button_green_bg)

                //get input from EditTexts and save in variables
                val recipient = "dummyemail@gmail.com"
                val subject = "Help Call"

                //method call for email intent with these inputs as parameters
                sendEmail(recipient, subject)
            }
            R.id.backBtn -> {
                this.finish()
            }
        }

    }

    /**
     * Function for sending email
     * */
    private fun sendEmail(recipient: String, subject: String) {
        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        //put the message in the intent
        //mIntent.putExtra(Intent.EXTRA_TEXT, message)

        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }

}

