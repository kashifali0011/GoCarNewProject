package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentMessagesBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.helper.DelegateStatic
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.messages.MessagesResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.adapters.MessagesAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for messages threads
 * */
class MessagesFragment : BaseFragment(), OnNetworkResponse, RecyclerViewItemClick,
    MessagesAdapter.AllIsSeen {

    lateinit var binding: FragmentMessagesBinding
    val homeViewModel: MainScreenViewModel by viewModel()
    lateinit var adapter: MessagesAdapter
    var pageNation = 1
    var layoutManager: LinearLayoutManager? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_messages,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        setData()


    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        adapter = MessagesAdapter(requireActivity(), uiHelper, this)
        adapter.setListener(this)

        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.messages,
            toolBarBg = requireActivity().getDrawable(R.color.textColor3) ?: R.drawable.bg_blue,
            textColor = R.color.text_receive_msg,
            arrowColor = R.color.black_text_black_variation_18,
            arrowVisible = false
        )
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                adapter.filter.filter(s)
            }
        })

        binding.chatList.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                var lastIndux = layoutManager?.findLastVisibleItemPosition()
                Log.d("lastIndux" , lastIndux.toString())
            }
        })
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_MESSAGES -> {
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    MessagesResponseModel::class.java
                )
                 layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                binding.chatList.layoutManager = layoutManager
                binding.chatList.adapter = adapter
                adapter.setList(responseModel.messagesList)
                binding.noDataLayout.isVisible = responseModel.messagesList.isEmpty()
                binding.clData.isVisible = responseModel.messagesList.isNotEmpty()

                pageNation = responseModel.pagination.totalPages
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)

    }

    override fun onResume() {
        super.onResume()
        homeViewModel.getMessagesNew(1, true, Constants.API.GET_MESSAGES, this)
    }

    override fun onItemClick(position: Int) {
        if (position == 0) {
            binding.noDataLayout.visibility = View.VISIBLE
        } else {
            binding.noDataLayout.visibility = View.GONE
        }
    }

    override fun onAllMessagesSeen() {
        if (DelegateStatic.tmainActivity != null)
            DelegateStatic.tmainActivity.setBadgeIcon()
    }

}