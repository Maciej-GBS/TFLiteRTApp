package com.gummybearstudio.tflitetester.fragments

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageProxy
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.core.content.ContextCompat
import com.gummybearstudio.tflitetester.R
import com.gummybearstudio.tflitetester.databinding.CameraScreenBinding
import com.gummybearstudio.tflitetester.backend.DetectedObject
import com.gummybearstudio.tflitetester.backend.ModelFileProvider
import com.gummybearstudio.tflitetester.backend.ObjectDetectionHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), ObjectDetectionHandler.ResultListener {

    private var _camBinding: CameraScreenBinding? = null
    private val camBinding
        get() = _camBinding!!

    private lateinit var bitmapBuffer: Bitmap
    private var detectionHandler: ObjectDetectionHandler? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraReady: Boolean = false

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.navigatorContainer)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }
        if (cameraReady)
            loadModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (cameraReady)
            loadModel()
    }

    override fun onDestroyView() {
        cameraReady = false
        _camBinding = null
        super.onDestroyView()
        detectionHandler?.closeInference()
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _camBinding = CameraScreenBinding.inflate(inflater, container, false)
        return camBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        camBinding.cameraViewFinder.post {
            setUpCamera()
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
                cameraReady = true
                loadModel()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = camBinding.cameraViewFinder.display.rotation
    }

    // Declare and bind preview, capture and analysis use cases
    private fun bindCameraUseCases() {
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(camBinding.cameraViewFinder.display.rotation)
                .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(camBinding.cameraViewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            // The image rotation and RGB image buffer are initialized only once
                            // the analyzer has started running
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        infer(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(camBinding.cameraViewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e("CameraFragment", "Use case binding failed", exc)
        }
    }

    private fun infer(image: ImageProxy) {
        if (detectionHandler == null) {
            ModelFileProvider.modelFile?.also {
                detectionHandler = ObjectDetectionHandler()
                detectionHandler!!.addListener(this)
                detectionHandler!!.prepareInference(it)
            }
        }

        detectionHandler?.apply {
            image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
            runInference(bitmapBuffer)
        }
    }

    private fun loadModel() {
        if (ModelFileProvider.modelFile == null || ModelFileProvider.modelFile?.exists() == false) {
            Log.d("CameraFragment", "Model file missing, launching open dialog...")
            Navigation.findNavController(requireActivity(), R.id.navigatorContainer)
                .navigate(CameraFragmentDirections.actionCameraToLoader())
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(
        results: List<DetectedObject>?,
        inferenceTime: Long
    ) {
        activity?.runOnUiThread {
            camBinding.overlay.setResults(
                results ?: listOf()
            )
        }
    }
}
