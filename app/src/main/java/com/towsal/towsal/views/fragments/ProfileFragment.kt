package com.towsal.towsal.views.fragments

import android.app.Activity
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentProfileBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.showPopupWithIndication
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.interfaces.ProfileRVItemClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.profile.GetProfileResponseModel
import com.towsal.towsal.network.serializer.profile.UpdateImageModel
import com.towsal.towsal.network.serializer.settings.ProfileModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.activities.*
import com.towsal.towsal.views.adapters.ProfileSettingAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for profile settings
 * */
class ProfileFragment : BaseFragment(), OnNetworkResponse,
    ProfileRVItemClick, PopupCallback {

    lateinit var binding: FragmentProfileBinding
    val homeViewModel: MainScreenViewModel by viewModel()
    val tripsViewModel: TripsViewModel by viewModel()


    private val settingList: ArrayList<ProfileModel> = ArrayList()
    private var loadDataApi = true

    lateinit var navController: NavController
    var builder = CustomTabsIntent.Builder()
    var userName: String = ""

    var supportNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile,
            container,
            false
        )
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        setData()
        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.profile,
            actionBar = null,
            arrowVisible = false
        )
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {

        val pkgInfo: PackageInfo =
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        binding.versionName.text = "version ${pkgInfo.versionName}"
        settingList.clear()
        var model = ProfileModel()
        model.name = requireActivity().getString(R.string.account)
        model.image = R.drawable.ic_person_profile_new
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.favorites)
        model.image = R.drawable.ic_favority_new
        settingList.add(model)
        model = ProfileModel()
        model.name = getString(R.string.payment)
        model.image = R.drawable.ic_payment_card
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.driver_license)
        model.image = R.drawable.ic_driver_licencs
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.transaction_history)
        model.image = R.drawable.pic_transaction_history // remaining
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.support)
        model.image = R.drawable.ic_support_new
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.help)
        model.image = R.drawable.ic_help_new
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.change_password)
        model.image = R.drawable.ic_password_change_new
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.change_language)
        model.image = R.drawable.ic_change_language
        settingList.add(model)
        //adding rating and reviews section in profile
        model = ProfileModel()
        model.name = getString(R.string.reviews_and_ratings)
        model.image = R.drawable.ic_star_outline
        settingList.add(model)
        model = ProfileModel()
        model.name = requireActivity().getString(R.string.logout)
        model.image = R.drawable.ic_logout
        settingList.add(model)
        binding.recyclerView.apply {
            adapter = ProfileSettingAdapter(
                settingList,
                requireActivity(),
                uiHelper,
                this@ProfileFragment
            )
        }

        val userModel = preferenceHelper
            .getObject(
                Constants.USER_MODEL,
                UserModel::class.java
            ) as? UserModel
        if (!userModel?.imageUrl.isNullOrEmpty()) {
            Glide.with(requireActivity()).load(BuildConfig.IMAGE_BASE_URL + userModel?.imageUrl)
                .into(binding.ivUserImage)
        }
        binding.tvName.text = userModel?.name
        binding.tvJoinDateAndType.text = getString(
            R.string.joined, uiHelper.getFormattedDate(
                userModel?.created_at ?: "",
                Constants.ServerDateTimeFormat,
                Constants.ResponseFormatY,
                false
            )
        )

    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity?)!!.setBadgeIcon()
        /*...it was already done ....*/
        if (loadDataApi) {
            Log.d("loadApi", "true")
            homeViewModel.getProfile(true, Constants.API.GET_PROFILE, this)
        }
    }

    override fun onItemClick(position: Int) {
        when (position) {
            0 -> {
                // carCheckOutViewModel.showTermConditionPrivacyPolicyPopup(requireActivity(), this)
                uiHelper.openActivity(requireActivity(), AccountDetailsActivity::class.java)
            }
            1 -> {
                uiHelper.openActivity(requireActivity(), FavoriteListActivity::class.java)
            }
            2 -> {
                uiHelper.openActivity(requireActivity(), PaymentsActivity::class.java)
            }
            3 -> {
                uiHelper.openActivity(requireActivity(), DriverLicenseInfoActivity::class.java)
            }
            4 -> {
                uiHelper.openActivity(requireActivity(), TransactionHistoryActivity::class.java)
            }
            5 -> {
                val bundle = Bundle()
                bundle.putString("name", userName)
                bundle.putString(Constants.DataParsing.SUPPORT_NUMBER, supportNumber)
                uiHelper.openActivity(requireActivity(), SupportActivity::class.java, bundle)
            }
            6 -> {
                val url = BuildConfig.BASE_URL_WEB + "help"
                val customTabsIntent: CustomTabsIntent = builder.build()
                customTabsIntent.intent.`package` = "com.android.chrome"
                customTabsIntent.launchUrl(requireContext(), url.toUri())
            }
            7 -> {
                uiHelper.openActivity(requireActivity(), ChangePasswordActivity::class.java)
            }
            8 -> {
                val bundle = Bundle()
                bundle.putBoolean(Constants.IS_FROM_WITHIN_APP, true)
                bundle.putInt(Constants.BUNDLE_VALUE, Constants.CHANGE_LANGUAGE)
                uiHelper.openActivity(
                    requireActivity(),
                    LanguageSelectionActivity::class.java,
                    bundle
                )
            }
            9 -> {
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.USER_ID, (preferenceHelper.getObject(
                        Constants.USER_MODEL,
                        UserModel::class.java
                    ) as? UserModel)?.id ?: -1
                )
                uiHelper.openActivity(requireActivity(), UserProfileActivity::class.java, bundle)
            }
            10 -> {
                activity?.showPopupWithIndication(
                    this, R.string.logout,
                    R.string.aur_you_sure_you_want_to_logout,
                    false,
                    ""
                )
//                uiHelper.showPopup(
//                    requireActivity(),
//                    this,
//                    R.string.yes,
//                    R.string.no,
//                    R.string.logout,
//                    R.string.aur_you_sure_you_want_to_logout,
//                    true
//                )
            }
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {
            binding.ivUserImage -> {
                loadDataApi = false
                ImagePicker.with(requireActivity())
                    .crop(1f, 1f)
                    .compress(1024) //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(
                        1080,
                        1080
                    )  //Final image resolution will be less than 1080 x 1080(Optional)
                    .start { resultCode, data ->
                        when (resultCode) {
                            Activity.RESULT_OK -> {
                                val fileUri = data?.data
                                if (fileUri != null) {
                                    binding.ivUserImage.setImageURI(fileUri)
//                                    uiHelper.loadImage(binding.viewClickImage, fileUri.path!!)
                                    homeViewModel.updateImage(
                                        fileUri.path,
                                        true,
                                        Constants.API.UPDATE_IMAGE,
                                        this
                                    )
//                                    loadDataApi = true
                                }
                            }
                            ImagePicker.RESULT_ERROR -> {
                                Toast.makeText(
                                    activity,
                                    ImagePicker.getError(data),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                            else -> {
                            }
                        }
                    }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.LOGOUT -> {
                uiHelper.showLongToastInCenter(requireContext(), response?.message)
                preferenceHelper.clearKey(Constants.USER_MODEL)
                requireActivity().finish()
                uiHelper.openAndClearActivity(requireActivity(), LoginActivity::class.java)
            }
            Constants.API.GET_PROFILE -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    GetProfileResponseModel::class.java
                )
                Log.d("Resume", "onResumeApiCalling")
                Glide.with(requireActivity()).load(BuildConfig.IMAGE_BASE_URL + model.profile.image)
                    .into(binding.ivUserImage)
                binding.tvName.text = model.profile.name
                val userModel = preferenceHelper
                    .getObject(
                        Constants.USER_MODEL,
                        UserModel::class.java
                    ) as? UserModel
                userModel?.imageUrl = model.profile.image
                userModel?.name = model.profile.name
                preferenceHelper.saveObject(userModel ?: UserModel(), Constants.USER_MODEL)
                userName = model.profile.name
                supportNumber = model.supportNumber
                /*..trying to get profile image ....*/
                loadDataApi = true
            }
            Constants.API.UPDATE_IMAGE -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    UpdateImageModel::class.java
                )
                val userModel = preferenceHelper
                    .getObject(
                        Constants.USER_MODEL,
                        UserModel::class.java
                    ) as? UserModel

                userModel?.imageUrl = model.profile_image
                preferenceHelper.saveObject(userModel!!, Constants.USER_MODEL)
                loadDataApi = true
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireContext(), response?.message)
    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            homeViewModel.logout(true, Constants.API.LOGOUT, this)
        }
    }

    override fun popupButtonClick(value: Int, text_id_model: Any) {

    }
    //sdfkj

}