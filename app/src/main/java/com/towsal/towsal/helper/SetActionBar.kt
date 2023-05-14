package com.towsal.towsal.helper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.towsal.towsal.R

/**
 * Class for custom action bar
 * */
class SetActionBar(supportActionBar: ActionBar?, var activity: Activity?) {

    private val actionBar = supportActionBar
    var headerText: TextView? = null
    var back: ImageView? = null

    init {
        actionBar?.setDisplayShowHomeEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.elevation = 0f
        val customView: View? =
            activity?.layoutInflater?.inflate(R.layout.custom_actionbar_layout, null)
        actionBar?.setCustomView(
            customView,
            ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        val parent = customView?.parent as Toolbar?
        parent?.setPadding(0, 0, 0, 0)
        parent?.setContentInsetsAbsolute(0, 0)
        headerText = customView?.findViewById(R.id.toolbarTitle)
        back = customView?.findViewById(R.id.iv_arrowBack)

    }

    /**
     * Function for setting header text or title for action bar
     * */
    fun setActionBarHeaderText(text: String?) {

        headerText?.text = text
    }

    /**
     * Function for setting back button click for action bar
     * */
    fun setBackButton() {
        back?.visibility = View.VISIBLE
        back?.setOnClickListener {
            activity?.onBackPressed()
        }
    }


}