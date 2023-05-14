package com.towsal.towsal.views.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityFullScreenImageBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.adapters.ImageAdapter

/**
 * Function for full screen image
 * */
class FullScreenImageActivity : BaseActivity(), View.OnClickListener {

    lateinit var imagesList: ArrayList<String>
    lateinit var binding: ActivityFullScreenImageBinding
    var isViewHidden: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_image)

        binding.btnBack.setOnClickListener(this)
        uiHelper.hideActionBar(supportActionBar)
        binding.clImageRecycler.isVisible =
            (intent.extras ?: Bundle()).containsKey(Constants.DataParsing.IMAGES)
        if ((intent.extras ?: Bundle()).containsKey(Constants.DataParsing.IMAGES)) {
            val position = intent.extras?.getInt("position", 0)
            (intent.extras?.getSerializable(Constants.DataParsing.IMAGES) as ArrayList<*>).also {
                imagesList =
                    it as ArrayList<String>
            }
            settingUpRecyclerView(position)
        } else if (
            (intent.extras ?: Bundle()).containsKey(Constants.DataParsing.IMAGE)
        ) {
            val url = (intent.extras ?: Bundle()).getString(Constants.DataParsing.IMAGE) ?: ""
            binding.clImage.isVisible = !url.contains(".pdf", true)
            binding.clWebView.isVisible = url.contains(".pdf", true)
            if (!url.contains(".pdf", true)) {
                binding.image.loadImage(
                    BuildConfig.IMAGE_BASE_URL + url
                )
            } else {

                Handler(Looper.getMainLooper()).postDelayed({
                    binding.wvPdfLoader.webViewClient = WebViewClient()
                    binding.wvPdfLoader.settings.setSupportZoom(false)
                    binding.wvPdfLoader.settings.javaScriptEnabled = true
                    binding.wvPdfLoader.settings.builtInZoomControls = false

                    showLoaderDialog()
                    Log.e("onCreate: ", BuildConfig.IMAGE_BASE_URL + url)
                    binding.wvPdfLoader.loadUrl("https://docs.google.com/gview?embedded=true&url=${BuildConfig.IMAGE_BASE_URL + url}")
                    binding.wvPdfLoader.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            hideLoaderDialog()
                        }
                    }
                }, 500)
            }
        }
    }

    private fun settingUpRecyclerView(position: Int?) {

        binding.recyclerView.apply {
            layoutManager =
                LinearLayoutManager(
                    this@FullScreenImageActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            adapter =
                ImageAdapter(this@FullScreenImageActivity, imagesList, uiHelper) {
                    isViewHidden = !isViewHidden
                    setViewsVisibility()
                }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager: LinearLayoutManager =
                        recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemPosition != 0 && visibleItemPosition % imagesList.size == 0) {
                        binding.recyclerView.layoutManager!!.scrollToPosition(0)
                    } else {
                        binding.position.text =
                            "${(visibleItemPosition % imagesList.size) + 1} of ${imagesList.size}"
                    }
                }
            })

        }

        position?.let { binding.recyclerView.scrollToPosition(it) }
        val helper: SnapHelper = PagerSnapHelper()
        helper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setViewsVisibility() {
        binding.btnBack.isVisible = isViewHidden
        binding.position.isVisible = isViewHidden
    }


    /**
     * Function for click listeners
     * */
    override fun onClick(v: View?) {
        v?.preventDoubleClick()
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
        }

    }
}