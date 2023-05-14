package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityViewAllCarFeaturesBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.adapters.BasicCarFeatureViewOnlyAdapter
import com.towsal.towsal.views.adapters.CarFeatureAdapter

/**
 * Activity class for var features
 * */
class ViewAllCarFeaturesActivity : BaseActivity() {
    var carFeatures: ArrayList<CarMakeInfoModel> = null ?: ArrayList()
    lateinit var binding: ActivityViewAllCarFeaturesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_all_car_features)
        actionBarSetting()
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            carFeatures =
                intent.getSerializableExtra(Constants.DataParsing.MODEL) as ArrayList<CarMakeInfoModel>
                val name = intent.getStringExtra(Constants.DataParsing.NAME)
            binding.tvCarName.text = name
            val adapter = CarFeatureAdapter(this, uiHelper, false)
            binding.featureListRecyclerView.adapter = adapter
            adapter.setList(carFeatures)
        }
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.car_features,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.back -> {
                finish()
            }
        }
    }
}