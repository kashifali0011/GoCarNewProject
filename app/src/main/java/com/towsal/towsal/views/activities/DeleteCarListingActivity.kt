package com.towsal.towsal.views.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityDeleteCarListingBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.DeleteReasonListModel
import com.towsal.towsal.network.serializer.settings.VehicleInfoSettingModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.DeleteCarReasonListingAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Activity class for delete listed car
 * */
class DeleteCarListingActivity : BaseActivity(), OnNetworkResponse,
    DeleteCarReasonListingAdapter.onClickListener {

    lateinit var binding: ActivityDeleteCarListingBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var model = DeleteReasonListModel()
    lateinit var adapter: DeleteCarReasonListingAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_delete_car_listing)
        binding.activity = this
        binding.layoutToolBar.setAsHostToolBar(
            R.string.delete_listing,
            supportActionBar
        )
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        settingsViewModel.getCarDeleteReasonList(
            true,
            Constants.API.GET_CAR_REASON_LIST,
            this
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.deleteListing -> {
                val carId =
                    (intent?.extras?.getSerializable(Constants.DataParsing.MODEL) as VehicleInfoSettingModel).id
                var selectedReason = false
                var listIds: ArrayList<Int>
                if (adapter.getList().isNotEmpty()) {
                    selectedReason = adapter.getList().any {
                        it.selected
                    }
                }
                if (selectedReason) {
                    listIds = ArrayList(adapter.getList().filter {
                        it.selected
                    }.map {
                        it.id
                    })
                    settingsViewModel.deleteCarList(
                        carId,
                        listIds,
                        true,
                        Constants.API.DELETE_CAR_LISTING,
                        this
                    )
                } else {
                    uiHelper.showLongToastInCenter(
                        this,
                        getString(R.string.select_reasons_for_delete_listing)
                    )
                }
            }
            R.id.clSelectValues -> {
                binding.cwOptionList.isVisible =
                    binding.cwOptionList.getVisibility() != View.VISIBLE
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_REASON_LIST -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    DeleteReasonListModel::class.java
                )
                adapter = DeleteCarReasonListingAdapter(
                    this,
                    uiHelper,
                    model.delete_car_reason_list,
                    this
                )
                binding.recyclerView.adapter = adapter
                binding.mainLayout.visibility = View.VISIBLE
            }
            Constants.API.DELETE_CAR_LISTING -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

//    fun makeDrawable() {
//        val rss = RoundRectShape(
//            floatArrayOf(
//                12f, 12f, 12f,
//                12f, 12f, 12f, 12f, 12f
//            ), null, null
//        )
//        val sds = ShapeDrawable(rss)
//        sds.shaderFactory = object : ShapeDrawable.ShaderFactory() {
//            override fun resize(width: Int, height: Int): Shader {
//                return LinearGradient(
//                    0f, 0f, 0f, height.toFloat(), intArrayOf(
//                        Color.parseColor("#e5e5e5"),
//                        Color.parseColor("#e5e5e5"),
//                        Color.parseColor("#e5e5e5"),
//                        Color.parseColor("#e5e5e5")
//                    ), floatArrayOf(
//                        0f,
//                        0.50f, 0.50f, 1f
//                    ), Shader.TileMode.REPEAT
//                )
//            }
//        }
//
//        val ld = LayerDrawable(arrayOf<Drawable>(sds, binding.cwOptionList.el))
//        ld.setLayerInset(
//            0,
//            5,
//            5,
//            0,
//            0
//        ) // inset the shadow so it doesn't start right at the left/top
//
//        ld.setLayerInset(
//            1,
//            0,
//            0,
//            5,
//            5
//        ) // inset the top drawable so we can leave a bit of space for the shadow to use
//
//
//        binding.cwOptionList.setBackgroundDrawable(ld)
//    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun onClick(selectItem: String) {
        binding.cwOptionList.isVisible = false
        binding.tvDeleteReason.text = selectItem

    }
}