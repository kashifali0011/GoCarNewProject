package com.towsal.towsal.views.activities

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityIntoBinding
import com.towsal.towsal.views.adapters.ViewPagerIntroAdapter

/**
 * Activity class for tutorial screen
 * */
class IntroActivity : BaseActivity() {
    lateinit var binding: ActivityIntoBinding
    private lateinit var layouts: IntArray
    var viewpagerAdapter: ViewPagerIntroAdapter? = null
    private lateinit var dots: Array<TextView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_into)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        layouts = intArrayOf(
            R.layout.intro_1,
            R.layout.intro_2,
            R.layout.intro_3,
            R.layout.intro_4
        )
        addBottomDots(0)
        viewpagerAdapter = ViewPagerIntroAdapter(layouts, this)
        binding.introViewPager.adapter = viewpagerAdapter
        binding.introViewPager.addOnPageChangeListener(viewPagerPageChangeListener)
    }

    /**
     * Function for adding dot views
     * */
    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)
        val colorsActive = resources.getIntArray(R.array.new_array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.new_array_dot_inactive)
        binding.layoutDots.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 50f
            dots[i]?.setTextColor(colorsInactive[currentPage])
            binding.layoutDots.addView(dots[i])
        }
        if (dots.isNotEmpty()) {
            dots[currentPage]?.setTextColor(colorsActive[currentPage])
        }
    }

    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object :
        ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            setTextViewsText(position)
            addBottomDots(position)
            if (position < layouts.size - 1) {
                binding.next.visibility = View.VISIBLE
                binding.imgNext.visibility = View.VISIBLE
            } else if (position == layouts.size - 1) {
                binding.layoutDots.visibility = View.GONE
                binding.tvGetStarted.visibility = View.VISIBLE
                binding.imgGetStarted.visibility = View.VISIBLE
                binding.next.visibility = View.GONE
                binding.imgNext.visibility = View.GONE
                binding.tvSkipIntro.visibility = View.GONE

            }
        }

        private fun setTextViewsText(position: Int) {
            var titleId = 0
            var messageId = 0
            binding.tvGoOnShoo.isVisible = false
            binding.tvIntro4Msg.isVisible = false
            when (position) {
                0 -> {
                    titleId = R.string.shareACar
                    messageId = R.string.intro_1_msg
                }
                1 -> {
                    titleId = R.string.book
                    messageId = R.string.intro_2_msg
                }
                2 -> {
                    titleId = R.string.earn
                    messageId = R.string.intro_3_msg
                }
                3 -> {
                    titleId = R.string.getToIt
                    binding.tvMessage.text = ""
                    binding.tvGoOnShoo.isVisible = true
                    binding.tvIntro4Msg.isVisible = true
                }
            }

            binding.tvTitle.text = getString(titleId)
            if (messageId != 0) {
                binding.tvMessage.text = getString(messageId)
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.tvSkipIntro -> {
                uiHelper.openAndClearActivity(this, LoginActivity::class.java)
            }
            R.id.next -> {
                val current = getItem()
                if (current < layouts.size) { // move to next screen
                    binding.introViewPager.currentItem = current
                }
                if (current == layouts.size) {
                    binding.next.visibility = View.GONE
                }
            }
            R.id.imgNext -> {
                val current = getItem()
                if (current < layouts.size) { // move to next screen
                    binding.introViewPager.currentItem = current
                }
                if (current == layouts.size) {
                    binding.next.visibility = View.GONE
                }
            }
            R.id.tvGetStarted -> {
                uiHelper.openAndClearActivity(this, LoginActivity::class.java)
            }
            R.id.imgGetStarted -> {
                uiHelper.openAndClearActivity(this, LoginActivity::class.java)
            }
        }
    }

    /**
     * Function for getting current item position
     * */
    private fun getItem(): Int {
        return binding.introViewPager.currentItem.plus(1)
    }
}