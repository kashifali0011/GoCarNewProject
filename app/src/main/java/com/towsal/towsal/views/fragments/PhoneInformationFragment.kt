package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentPhoneInformationBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.UserInformationViewModel
import com.towsal.towsal.views.activities.UserInformationActivity

/**
 * Fragment phone information
 * */
class PhoneInformationFragment : BaseFragment() {
    var model = UserInfoResponseModel()

    private val args: ProfilePhotoFragmentArgs by navArgs()
    lateinit var binding: FragmentPhoneInformationBinding
    private val userInformationViewModel: UserInformationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_phone_information,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        val userModel = PreferenceHelper()
            .getObject(
                Constants.USER_MODEL,
                UserModel::class.java
            ) as? UserModel
        if (!userModel?.phone.isNullOrEmpty())
            binding.phoneNumber.text = userModel?.phone
        if (args.model != null) {
            model = args.model!!

        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.nextBtn -> {
                moveToNextFragment()
            }

        }
    }

    /**
     * Function for moving to next fragment
     * */
    private fun moveToNextFragment() {
        val navController =
            requireActivity().findNavController(R.id.navGraphUserInformation)
        val bundle = Bundle()
        bundle.putSerializable(Constants.DataParsing.MODEL, model)
        navController.navigate(
            R.id.action_phoneInformationFragment_to_profilePhotFragment,
            bundle
        )
        if (UserInformationActivity.stepInfoCallback != null) {
            UserInformationActivity.stepInfoCallback?.stepNumber(Constants.CarStep.STEP_2_COMPLETED)
        }
    }
}