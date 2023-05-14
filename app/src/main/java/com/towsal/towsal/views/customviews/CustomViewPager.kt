package com.towsal.towsal.views.customviews

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager
import java.lang.reflect.Field

/**
 * Class custom view pager
 * */
open class CustomViewPager : ViewPager {
    //    val TAG_RESTORE_VIEWPAGER_STATE_POS = "TAG_RESTORE_VIEWPAGER_STATE_POS".hashCode()
//    val BUNDLE_VIEW_PAGER_POS = "pos"
//    val BUNDLE_VIEW_STATE = "viewstate"
//    val BUNDLE_PAGER_VIEW_STATE = "pagerviewstate"
//    val BUNDLE_CHILDREN_VIEW_STATES = "childrenviewstates"
    private val mSavedViewStates: HashMap<Int, SparseArray<Parcelable>?> = HashMap()

    constructor(context: Context?) : super(context!!) {
        setMyScroller()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        setMyScroller()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean { // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean { // Never allow swiping to switch between pages
        return false
    }

    //down one is added for smooth scrolling
    private fun setMyScroller() {
        try {
            val viewpager: Class<*> = ViewPager::class.java
            val scroller: Field = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, MyScroller(context))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class MyScroller(context: Context?) :
        Scroller(context, DecelerateInterpolator()) {
        override fun startScroll(
            startX: Int,
            startY: Int,
            dx: Int,
            dy: Int,
            duration: Int
        ) {
            super.startScroll(startX, startY, dx, dy, 350 /*1 secs*/)
        }
    }
}