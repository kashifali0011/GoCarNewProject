package com.towsal.towsal.views.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import com.otaliastudios.cameraview.size.AspectRatio
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityTakePhotoBinding
import com.towsal.towsal.extensions.convertBitmapToUri
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.utils.Constants
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class TakePhotoActivity : BaseActivity(), View.OnClickListener {


    lateinit var binding: ActivityTakePhotoBinding
    var moveInfo = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_take_photo)
        binding.activity = this
        val radius = 25f;

        val decorView = window.decorView
        val rootView = decorView.findViewById(android.R.id.content) as ViewGroup

        val windowBackground = decorView.background
        Log.e("params", "${binding.child.layoutParams.width}x${binding.child.layoutParams.height}")
        binding.blurView.setupWith(rootView, RenderScriptBlur(this))
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)


        if (intent.extras != null) {
            moveInfo = intent.getIntExtra(Constants.DataParsing.MOVE_TO_DRIVER_INFO, 0)
        }
        if (moveInfo == 1){
            binding.layoutToolBar.setAsGuestToolBar(
                R.string.take_photo,
                supportActionBar
            )
        }else {
            binding.layoutToolBar.setAsHostToolBar(
                R.string.take_photo,
                supportActionBar
            )
        }

    }

    override fun onResume() {
        super.onResume()
        binding.btnTakePhoto.isEnabled = false
        startCamera()
    }


    private fun startCamera() {
        binding.cameraView.setLifecycleOwner(this)
        val frameProcessor = FrameProcessor {
            val out = ByteArrayOutputStream()
            val yuvImage = YuvImage(
                it.getData(),
                ImageFormat.NV21,
                it.size.width,
                it.size.height,
                null
            )
            yuvImage.compressToJpeg(
                Rect(
                    0,
                    0,
                    it.size.width,
                    it.size.height,
                ), 90, out
            )
            val imageBytes: ByteArray = out.toByteArray()
            val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.ivBackground.setImageBitmap(
                image
            )
            extractDataFromFrame(it)
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                binding.cameraView.removeFrameProcessor(frameProcessor)
                lifecycleScope.launch(Dispatchers.Main) {
                    result.toBitmap(1000, 1000) {
                        val aspectRatio = AspectRatio.of(result.size)
                        Log.e("ratio", "$aspectRatio")
                        val uri = this@TakePhotoActivity.convertBitmapToUri(
                            it!!
                        )
                        val bundle = Bundle()
                        bundle.putString("file_uri", uri.path)
                        val intent = Intent()
                        intent.putExtras(bundle)
                        hideLoaderDialog()
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                }
            }
        })

        binding.cameraView.addFrameProcessor(frameProcessor)
    }

    private fun extractDataFromFrame(frame: Frame) {

        val localModel =
            LocalModel.Builder().setAssetFilePath("custom_models/object_labeler.tflite").build()

        val option = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()  // Optional
            .enableMultipleObjects()
            .build()

        val objectDetector = ObjectDetection.getClient(option)
        val inputImage = getInputImageFromFrame(frame)
        if (inputImage != null) {
            objectDetector.process(inputImage)
                .addOnCanceledListener {
                    binding.btnTakePhoto.isEnabled = false
                    binding.viewCameraRect.setBackgroundResource(R.drawable.bg_capture_photo_area)
                }
                .addOnCompleteListener {
                    it.result.forEach { item ->
                        if (item.labels.isNotEmpty() && item.labels[0].confidence > 0.5f) {
                            binding.viewCameraRect.setBackgroundResource(R.drawable.bg_gradient_camera_surface)
                            binding.btnTakePhoto.isEnabled = true
                        } else {
                            binding.viewCameraRect.setBackgroundResource(R.drawable.bg_capture_photo_area)
                            binding.btnTakePhoto.isEnabled = false
                        }
                        if (item.labels.isNotEmpty()) {
                            Log.e(
                                "extractDataFromFrame: Confidence",
                                item.labels[0].confidence.toString()
                            )
                        }
                    }
                }
                .addOnSuccessListener {
                    it.forEach { item ->
                        if (item.labels.isNotEmpty() && item.labels[0].confidence > 0.5f) {
                            binding.btnTakePhoto.isEnabled = item.labels.isNotEmpty()
                            binding.viewCameraRect.setBackgroundResource(R.drawable.bg_gradient_camera_surface)
                        } else {
                            binding.viewCameraRect.setBackgroundResource(R.drawable.bg_capture_photo_area)
                        }
                        if (item.labels.isNotEmpty()) {
                            Log.e(
                                "extractDataFromFrame: Confidence",
                                item.labels[0].confidence.toString()
                            )
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("ErrorMl", it.message!!)
                    binding.btnTakePhoto.isEnabled = false
                    binding.viewCameraRect.setBackgroundResource(R.drawable.bg_capture_photo_area)
                }

        }
    }

    private fun getInputImageFromFrame(frame: Frame) =
        InputImage.fromByteArray(
            frame.getData(),
            frame.size.width,
            frame.size.height,
            frame.rotation,
            InputImage.IMAGE_FORMAT_NV21
        )


    override fun onClick(v: View?) {
        v?.preventDoubleClick()
        when (v) {
            binding.btnTakePhoto -> {
                binding.cameraView.takePicture()
                showLoaderDialog()
            }
        }
    }


}