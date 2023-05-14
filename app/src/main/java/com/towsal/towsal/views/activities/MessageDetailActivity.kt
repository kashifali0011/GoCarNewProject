package com.towsal.towsal.views.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityMessageDetailBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.helper.DelegateStatic
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.messages.*
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.adapters.MessagesDateAdapter
import com.towsal.towsal.views.adapters.MessagesDetailAdapter
import com.towsal.towsal.views.fragments.FilterTripStatusFragment
import kotlinx.android.synthetic.main.item_chat_list.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for messages
 * */
class MessageDetailActivity : BaseActivity(), OnNetworkResponse{

    val homeViewModel: MainScreenViewModel by viewModel()
    var carCheckoutId = 0
    var threadIdMessage = 0
    lateinit var binding: ActivityMessageDetailBinding
    var userModel: UserModel? = null
    lateinit var adapter: MessagesDetailAdapter
    lateinit var responseModel: GetMessageDetails
    var mArrayList: ArrayList<DateAndMessageList> = ArrayList()
    var pageNumber = 0
    var layoutManager: LinearLayoutManager? = null
    var totalPages = 0
    var checkCallApi: Boolean = true
    var numberCallApi = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_detail)
        binding.activity = this
        setData()
    }
    /**
     * Function for calling messages api
     * */
    fun callApi() {
        Log.d("callBothApi", "true")

        if (!DelegateStatic.notificationTitle.isNullOrEmpty()) {
            if (DelegateStatic.notificationTitle!! == "New message received") {
                setData()
            }
        } else {

        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        binding.toolBar.setAsGuestToolBar(
            titleId = R.string.messages, actionBar = supportActionBar, toolBarBg = getDrawable(R.color.textColor3) ?: R.drawable.bg_blue, R.color.text_receive_msg, R.color.black_text_black_variation_18
        )

        if (intent.extras != null) {
            threadIdMessage = intent!!.extras!!.getInt(Constants.DataParsing.ID, 0)
            preferenceHelper.setInt(Constants.DataParsing.USER_THREAD_ID, threadIdMessage)
            userModel = preferenceHelper.getObject(
                Constants.USER_MODEL, UserModel::class.java
            ) as? UserModel


            adapter = MessagesDetailAdapter(this, uiHelper, userModel!!/*, list*/)
            layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
            binding.listMessages.layoutManager = layoutManager
            binding.listMessages.adapter = adapter
            setMessageDetails()
            binding.listMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val position = layoutManager?.findLastCompletelyVisibleItemPosition()

                    if (position == numberCallApi) {
                        setMessageDetails()
                        numberCallApi += 10
                        checkCallApi = false
                    }
                }
            })
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_MESSAGE_DETAIL -> {
                responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    GetMessageDetails::class.java
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    var list = ArrayList<MessagesDetailModel>()
                    for (model in responseModel.messagesList) {
                        val alreadyExistsDateTag = adapter.mArrayList.find { it.viewType == 2 && it.message == model.date }
                        if (alreadyExistsDateTag != null) {
                            adapter.mArrayList.remove(alreadyExistsDateTag)
                        }
                        list.addAll(model.messagesList)
                        list.add( MessagesDetailModel().apply {
                            viewType = 2
                            message = model.date
                        })
                    }
                    if (checkCallApi) {
                        adapter.notifyItems(list)
                        binding.listMessages.scrollToPosition(0)

                    } else {
                        adapter.mArrayList.addAll(adapter.mArrayList.size -1, list)
                        adapter.notifyItems(adapter.mArrayList)
                    }
                    totalPages = responseModel.pagination.totalPages
                    binding.listMessages.isVisible = true
                }, 500)

                binding.name.text = responseModel.messenger.name
                uiHelper.loadImage(binding.image, responseModel.messenger.profile.toString())
                val stringTrips = if ((responseModel.messenger.total_trips ?: 0) > 1) "trips"
                else "trip"
                binding.tvUserDetail.text = "${responseModel.messenger.user_type}, ${responseModel.messenger.total_trips} ${stringTrips}, joined ${
                    DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat, "MMM yyyy", responseModel.messenger.joining_date
                    )
                }"
            }
            Constants.API.SEND_MESSAGE -> {

                val messageModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    MessagesDetailModel::class.java
                )
                val index = adapter.mArrayList.indexOfFirst { it.viewType == 2 && it.message.lowercase() == "today" }
                if (index == -1) {
                    adapter.mArrayList.add(0,MessagesDetailModel().apply {
                        viewType = 2
                        message = "today"
                    })
                }
                adapter.mArrayList.add( 0,messageModel)
                adapter.notifyItems(adapter.mArrayList)
                binding.search.setText("")
                binding.listMessages.scrollToPosition(0)

            }
            Constants.API.SEEN_MESSAGE -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>), BaseResponse::class.java
                )
            }
        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.ivSend -> {
                if (binding.search.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(this, getString(R.string.enter_message))
                } else {
                    val model = SendMessageModel()
                    model.message = binding.search.text.toString()
                    model.threadId = threadIdMessage
                    homeViewModel.sendMessage(model, true, Constants.API.SEND_MESSAGE, this)
                }
            }
        }
    }

    /**
     * Function for periodically call messages api
     * */
    fun CoroutineScope.launchPeriodicAsync(
        repeatMillis: Long, action: () -> Unit
    ) = this.async {
        if (repeatMillis > 0) {
            while (true) {
                action()
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            preferenceHelper.clearKey(Constants.DataParsing.USER_THREAD_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        homeViewModel.seenMessage(
            carCheckoutId, false, Constants.API.SEEN_MESSAGE, this
        )

    }

    private fun setMessageDetails() {
        homeViewModel.getMessageDetailNew(
            threadIdMessage, ++pageNumber, true, Constants.API.GET_MESSAGE_DETAIL, this
        )
    }
    override fun message(threadId: String, messageId: String, textMessage: String) {
        Log.d("textMessage" ,textMessage)
        val index = adapter.mArrayList.indexOfFirst { it.viewType == 2 && it.message.lowercase() == "today" }
        if (index == -1) {
            adapter.mArrayList.add(0,MessagesDetailModel().apply {
                viewType = 2
                message = "today"
            })
        }
        adapter.mArrayList.add( 0, MessagesDetailModel().apply {
            responseModel.messenger.id?.let {
                id =   messageId.toInt()
                message = textMessage
                this.threadId = threadId.toInt()
                sender_id = it
            }
        })
        adapter.notifyItems(adapter.mArrayList)
        super.message(threadId, messageId, textMessage)
    }
}