package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentMessagesDetailsBinding
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.messages.CreateThreadModel
import com.towsal.towsal.network.serializer.messages.GetMessageDetails
import com.towsal.towsal.network.serializer.messages.MessagesDetailModel
import com.towsal.towsal.network.serializer.messages.SendMessageModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.viewmodel.MessengerModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.MessagesDetailAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel

class MessagesDetailsFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse {


    val homeViewModel: MainScreenViewModel by viewModel()
    val tripsViewModel: TripsViewModel by activityViewModels()
    var carCheckoutId = 0
    lateinit var binding: FragmentMessagesDetailsBinding
    var receiverId = 0
    var userModel: UserModel? = null

    lateinit var adapter: MessagesDetailAdapter

    var threadId = 0
    var messenger: MessengerModel? = null
    lateinit var responseModel: GetMessageDetails

    var layoutManager: LinearLayoutManager? = null
    var pageNumber = 0
    var totalPages = 0
    var checkCallApi: Boolean = true
    var numberCallApi = 10


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMessagesDetailsBinding.bind(
            inflater.inflate(
                R.layout.fragment_messages_details,
                container,
                false
            )
        )
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
        setUiObserver()
    }

    private fun setUiObserver() {
        tripsViewModel.message.observe(viewLifecycleOwner) { messageDetailModel ->
            val indexOfMessage = adapter.mArrayList.indexOfFirst { it.id == messageDetailModel.id }
            if(indexOfMessage == -1) {
                val index = adapter.mArrayList.indexOfFirst { it.viewType == 2 && it.message.lowercase() == "today" }
                if (index == -1 ) {
                    adapter.mArrayList.add(0,MessagesDetailModel().apply {
                        viewType = 2
                        message = "today"
                    })
                }
                adapter.mArrayList.add( 0, messageDetailModel)
                adapter.notifyItems(adapter.mArrayList)
            }
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        carCheckoutId = tripsViewModel.tripId
        threadId = tripsViewModel.threadId
        preferenceHelper.setInt(Constants.DataParsing.USER_THREAD_ID, threadId)
        userModel = preferenceHelper
            .getObject(
                Constants.USER_MODEL,
                UserModel::class.java
            ) as? UserModel
        receiverId = tripsViewModel.receiverId

        if (threadId == 0) {
            messenger = tripsViewModel.messenger
            setMessengerData()
        }else{
            setMessageDetails()
        }

        adapter = MessagesDetailAdapter(requireActivity(), uiHelper, userModel!!)
        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, true)
        binding.listMessages.layoutManager = layoutManager
        binding.listMessages.adapter = adapter
        binding.listMessages.scrollToPosition(0)


        binding.listMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = layoutManager?.findLastCompletelyVisibleItemPosition()

                if (position == numberCallApi && threadId != 0) {
                    setMessageDetails()
                    numberCallApi += 10
                    checkCallApi = false
                }
            }
        })
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
                        list.add(MessagesDetailModel().apply {
                            viewType = 2
                            message = model.date
                        })
                    }
                    if (checkCallApi) {
                        adapter.notifyItems(list)
                        binding.listMessages.adapter = adapter
                        binding.listMessages.scrollToPosition(0)

                    } else {
                        adapter.mArrayList.addAll(adapter.mArrayList.size - 1, list)
                        adapter.notifyItems(adapter.mArrayList)
                    }


                    totalPages = responseModel.pagination.totalPages


                }, 500)

              //  messenger = tripsViewModel.messenger
                messenger = responseModel.messenger
                setMessengerData()


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
                Log.d("seenMessage", "successfullyy")
            }
            Constants.API.CREATE_THREAD_ID -> {
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CreateThreadModel::class.java
                )
                threadId = responseModel.id
                sendMessageRequest()
            }
        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
    }

    /**
     * Function for click listeners
     * */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivSend -> {
                if (binding.search.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        requireActivity(),
                        getString(R.string.enter_message)
                    )
                } else {
                    if (threadId != 0) {
                        sendMessageRequest()
                    } else {
                        homeViewModel.createThreadId(receiverId, true, Constants.API.CREATE_THREAD_ID, this)
                    }
                }
            }
        }
    }

    private fun sendMessageRequest() {
        val model = SendMessageModel()
        model.threadId = threadId
        model.message = binding.search.text.toString()
        homeViewModel.sendMessage(model, true, Constants.API.SEND_MESSAGE, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.seenMessage(
            carCheckoutId,
            false,
            Constants.API.SEEN_MESSAGE,
            this
        )
    }

    private fun setMessengerData() {
        messenger?.let {
            binding.name.text = it.name.toString()
            uiHelper.loadImage(
                binding.image,
                messenger?.profile.toString()
            )
            val stringTrips = if ((it.total_trips
                    ?: 0) > 1
            )
                "trips"
            else
                "trip"
            binding.tvUserDetail.text =
                "${it.user_type}, ${it.total_trips} ${stringTrips}, joined ${
                    DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        "MMM yyyy",
                        it.joining_date
                    )
                }"
        }

    }

    private fun setMessageDetails() {
        homeViewModel.getMessageDetailNew(
            threadId, ++pageNumber, true, Constants.API.GET_MESSAGE_DETAIL, this
        )
    }

     fun message(threadId: String, messageId: String, textMessage: String) {
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
        binding.search.setText("")
        binding.listMessages.scrollToPosition(0)
    }
}