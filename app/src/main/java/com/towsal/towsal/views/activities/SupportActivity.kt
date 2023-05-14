package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivitySupportBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.profile.GetProfileResponseModel
import com.towsal.towsal.network.serializer.profile.SecondMessage
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SupportActivity : BaseActivity() , OnNetworkResponse {
    lateinit var binding: ActivitySupportBinding
    var name: String = ""
    var supportNumber = "+966 9 20 00 0560"
    val homeViewModel: MainScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_support)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        setData()
    }

    private fun setData() {
        if (intent.extras != null) {
            name = intent.extras!!.getString("name")!!
           // supportNumber = intent.extras!!.getString(Constants.DataParsing.SUPPORT_NUMBER)!!

        }
       // binding.tvCall.text = "Call Najm $supportNumber"
        binding.tvHi.text = "HI ${
            if (name.split(" ").size > 1) {
                "${name.split(" ")[0]},"
            } else {
                "$name,"
            }
        }"
        binding.toolBar.setAsGuestToolBar(
            titleId = R.string.support,
            actionBar = supportActionBar,
            toolBarBg = R.drawable.bg_support_screen_toolbar,
            textColor = R.color.white,
            arrowColor = R.color.line_bg_1
        )
    }


    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.btnCallNow -> {
                binding.clMessageLayout.isVisible = true
            }
            R.id.clMessageLayout -> {
                binding.clMessageLayout.isVisible = false
            }
            R.id.ivClosed -> {
                binding.clMessageLayout.isVisible = false
            }
            R.id.btnSecondMessage -> {
                var message = binding.edtMessage.text
                if (message.isNotEmpty()){
                    homeViewModel.sendMessage(
                        "$message",
                        true,
                        Constants.API.SEND_MESSAGE,
                        this
                    )
                }else{
                    uiHelper.showLongToastInCenter(applicationContext, "Enter message")
                }

            }


            }
        }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SEND_MESSAGE -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    SecondMessage::class.java
                )
                binding.edtMessage.setText("")
                binding.clMessageLayout.isVisible = false
            }
        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        binding.clMessageLayout.isVisible = false
        binding.edtMessage.setText("")
        uiHelper.showLongToastInCenter(applicationContext, response?.message)
    }

}
// uiHelper.call(this, supportNumber)