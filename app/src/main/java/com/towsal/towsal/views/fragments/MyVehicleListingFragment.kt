package com.towsal.towsal.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentMyVehicleListingFramgentBinding
import com.towsal.towsal.extensions.deleteCustomerCar
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.showListingHoldPopup
import com.towsal.towsal.interfaces.PickerCallback
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.settings.VehicleInfoSettingModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.activities.CarListActivity
import com.towsal.towsal.views.activities.DeleteCarListingActivity
import com.towsal.towsal.views.activities.PaymentsActivity
import com.towsal.towsal.views.adapters.MyCarsAdapter
import kotlinx.android.synthetic.main.activity_listing_status.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyVehicleListingFragment : BaseFragment(),
    View.OnClickListener,
    PickerCallback, OnNetworkResponse, PopupCallback {

    lateinit var binding: FragmentMyVehicleListingFramgentBinding
    val settingsViewModel: SettingsViewModel by activityViewModels()
    var selectedPosition = -1
    var selectedCarId = -1
    var list: MutableList<VehicleInfoSettingModel> = mutableListOf()
    var selectedStatus = Constants.CarStatus.SNOOZED

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            list.remove(list[selectedPosition])
            if (list.isEmpty())
            ((binding.rvMyCarListing.adapter) as MyCarsAdapter).notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMyVehicleListingFramgentBinding.bind(
            inflater.inflate(
                R.layout.fragment_my_vehicle_listing_framgent,
                container,
                false
            )
        )
        binding.fragment = this
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObserver()
    }

    private fun setObserver() {
        settingsViewModel.myCasList.observe(
            viewLifecycleOwner
        ) {
            list = it
            setRecycler()
            setWatcherListener()
        }
    }

    private fun setWatcherListener() {
        binding.edtSearchLicencePlate.addTextChangedListener { searchText ->
            if (searchText.toString().isNotEmpty()) {
                val copyList = list.filter {
                    it.plateNumber.contains(searchText.toString())
                }
                (binding.rvMyCarListing.adapter as MyCarsAdapter).setList(copyList)
            } else {
                (binding.rvMyCarListing.adapter as MyCarsAdapter).setList(list)
            }
        }
    }

    private fun setRecycler() {
        binding.rvMyCarListing.apply {
            adapter = MyCarsAdapter(list, uiHelper) { carStatus, carId, position, flow ->
                selectedCarId = carId
                selectedPosition = position
                when (flow) {
                    MyCarsAdapter.STATUS_CHANGE_FLOW -> {
                        selectedStatus = carStatus
                        when (carStatus) {
                            Constants.CarStatus.LISTED -> {
                                callChangeStatusApi(
                                    Constants.CarStatus.LISTED,
                                    "",
                                    ""
                                )
                            }
                            Constants.CarStatus.SNOOZED -> {
                                callChangeStatusApi(
                                    Constants.CarStatus.SNOOZED,
                                    "",
                                    ""
                                )
                            }
                        }
                    }
                    MyCarsAdapter.DELETE_FLOW -> {
                        activity?.deleteCustomerCar(
                            this@MyVehicleListingFragment
                        )
                    }

                    MyCarsAdapter.OPEN_DETAILS_FLOW -> {
                        val directions =
                            HostFragmentDirections.actionBnvHostToVehicleSettingFragment()
                                .setCarId(carId)
                        requireActivity().findNavController(
                            R.id.navHostFragment
                        )
                            .navigate(directions)
                    }

                }
            }
        }
    }

    override fun onSelected(date: Any?) {
        if (date.toString().isNotEmpty()) {
            callChangeStatusApi(
                Constants.CarStatus.SNOOZED,
                uiHelper.currentDate,
                date.toString()
            )
        }

    }

    private fun callChangeStatusApi(carStatus: Int, from: String, to: String) {
        settingsViewModel.updateCarStatus(
            carStatus,
            selectedCarId.toString(),
            from,
            to,
            true,
            Constants.API.UPDATE_CAR_STATUS,
            this@MyVehicleListingFragment
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.UPDATE_CAR_STATUS -> {
                val userModel = preferenceHelper.getObject(
                    Constants.USER_MODEL,
                    UserModel::class.java
                ) as UserModel
                if (userModel.hostType == Constants.HostType.INDIVIDUAL) {
                    val index = list.indexOfFirst {
                        it.status == Constants.CarStatus.LISTED
                    }
                    if (index != -1) {
                        list[index].status = Constants.CarStatus.SNOOZED
                        ((binding.rvMyCarListing.adapter) as MyCarsAdapter).notifyItemChanged(index)
                    }
                }
                list[selectedPosition].status = selectedStatus
                ((binding.rvMyCarListing.adapter) as MyCarsAdapter).notifyItemChanged(
                    selectedPosition
                )
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        if (response?.code == 424) {
            requireActivity().showListingHoldPopup {
                uiHelper.openActivity(requireActivity(), PaymentsActivity::class.java)
            }
        } else
            uiHelper.showLongToastInCenter(requireActivity(), response?.message)

    }

    override fun onClick(p0: View?) {
        p0?.preventDoubleClick()
        when (p0) {
            binding.btnAddNewVehicle -> {
                uiHelper.openActivity(requireActivity(), CarListActivity::class.java, true)
            }
        }
    }

    override fun popupButtonClick(value: Int) {
        val bundle = Bundle()
        bundle.putSerializable(Constants.DataParsing.MODEL, list[selectedPosition])
        val intent = Intent(
            requireActivity(),
            DeleteCarListingActivity::class.java
        )
        intent.putExtras(bundle)
        launcher.launch(intent)
    }

}
