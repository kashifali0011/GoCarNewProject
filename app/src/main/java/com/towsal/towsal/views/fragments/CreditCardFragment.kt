package com.towsal.towsal.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentCreditCardBinding
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cardetails.UserInfoResponseModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepFourModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.UserInformationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for user credit card information
 * */
class CreditCardFragment : BaseFragment(), OnNetworkResponse {

    private val userInformationViewModel: UserInformationViewModel by viewModel()
    private val args: CreditCardFragmentArgs by navArgs()
    var model = UserInfoResponseModel()
    lateinit var binding: FragmentCreditCardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_credit_card,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        if (args.model != null) {
            model = args.model!!
            setData()
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {

        binding.cardNumberEdt.setText(model.stepFour.card_number)
        binding.titleEdt.setText(model.stepFour.title)
        binding.expiryEdt.setText(model.stepFour.exp_date)
        binding.cvcEdt.setText(model.stepFour.cvc)

        binding.expiryEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, start: Int, removed: Int, added: Int) {
                if (
                    start == 1 &&
                    start + added == 2 &&
                    p0?.contains('/')
                    == false
                ) {
                    binding.expiryEdt.setText("$p0/")
                } else if (
                    start == 3 &&
                    start - removed == 2 &&
                    p0?.contains('/')
                    == true
                ) {
                    binding
                        .expiryEdt
                        .setText(
                            p0.toString().replace("/", "")
                        )
                }
                binding
                    .expiryEdt
                    .setSelection(
                        binding.expiryEdt.text.toString().length
                    )
            }
        })

        binding.cardNumberEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

                if (s.isNotEmpty() && s.length < 16) {
                    binding.cardNumberEdt.error = getString(R.string.card_number_err_msg)
                }
            }
        })
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.saveBtn -> {
                when {
                    binding.titleEdt.text.toString().isEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.enter_card_title_err_msg)
                        )
                    }
                    binding.cardNumberEdt.text.toString().isEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.enter_card_number_err_msg)
                        )
                    }
                    binding.cardNumberEdt.text.toString().length < 16 -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.card_number_err_msg)
                        )
                    }
                    binding.expiryEdt.text.toString().isEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.enter_card_expiry_err_msg)
                        )
                    }
                    binding.expiryEdt.text.toString().length < 5 -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.enter_card_expiry_length_err_msg)
                        )
                    }
                    binding.cvcEdt.text.toString().isEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.enter_card_cvc_err_msg)
                        )
                    }
                    binding.cvcEdt.text.toString().length < 3 -> {
                        uiHelper.showLongToastInCenter(
                            requireContext(),
                            getString(R.string.enter_card_cvc_length_err_msg)
                        )
                    }
                    else -> {
                        val paymentModel = UserInfoStepFourModel()
                        paymentModel.step = 4
                        paymentModel.card_number = binding.cardNumberEdt.text.toString()
                        paymentModel.title = binding.titleEdt.text.toString()
                        paymentModel.exp_date = binding.expiryEdt.text.toString()
                        paymentModel.cvc = binding.cvcEdt.text.toString()
                        userInformationViewModel.saveCardInfo(
                            paymentModel,
                            true,
                            Constants.API.SAVE_USER_INFO,
                            this
                        )
                    }
                }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SAVE_USER_INFO -> {

            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireContext(), response?.message)
    }
}