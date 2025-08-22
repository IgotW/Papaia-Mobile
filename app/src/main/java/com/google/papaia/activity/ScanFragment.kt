package com.google.papaia.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.papaia.R
import com.google.papaia.response.PredictionResponse
import com.google.papaia.utils.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class ScanFragment : Fragment() {
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: FrameLayout
    private lateinit var imageGallery: ImageView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var progressBar: ProgressBar

    private var imageCapture: ImageCapture? = null

    private var camera: Camera? = null
    private var isFlashOn = false
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        // Flash button
        val flashToggle: ImageView = view.findViewById(R.id.flash_toggle)
        flashToggle.setOnClickListener {
            toggleFlash(flashToggle)
        }
    }

    private fun initViews(view: View) {
        previewView = view.findViewById(R.id.previewView)
        btnCapture = view.findViewById(R.id.btnCapture)
        imageGallery = view.findViewById(R.id.imageGallery)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnCapture.setOnClickListener { takePhoto() }

        imageGallery.setOnClickListener {
            val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                galleryLauncher.launch("image/*")
            } else {
                requestGalleryPermissionLauncher.launch(permission)
            }
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        showLoading()  // show overlay

        val photoFile = File(
            outputDirectory,
            "JPEG_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Toast.makeText(requireContext(), "Photo saved!", Toast.LENGTH_SHORT).show()
                    imageGallery.setImageURI(savedUri)
                    sendImageToApi(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    hideLoading()
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(requireContext(), "Capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun sendImageToApi(imageUri: Uri) {
        try {
            val file = uriToFile(imageUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val tokenValue = sharedPref.getString("token", null)

            if (tokenValue.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Authentication token missing", Toast.LENGTH_SHORT).show()
                return
            }

            val token = "Bearer $tokenValue"

            RetrofitClient.instance.predictDisease(multipartBody, token)
                .enqueue(object : retrofit2.Callback<PredictionResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<PredictionResponse>,
                        response: retrofit2.Response<PredictionResponse>
                    ) {
                        hideLoading()
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!.prediction ?: "Unknown"
                            val remedy = when (result.lowercase()) {
                                "anthracnose" -> "Apply copper-based fungicides. Avoid overhead watering."
                                "ring spot" -> "Use neem oil weekly. Remove infected leaves immediately."
                                "powdery mildew" -> "Improve air circulation. Use sulfur sprays."
                                "healthy" -> "Your crop is healthy"
                                else -> "No specific remedy found. Consult local expert."
                            }
                            showScanDialog(result, remedy)
                        } else {
                            Log.e("API_ERROR", "Code: ${response.code()} Body: ${response.errorBody()?.string()}")
                            Toast.makeText(requireContext(), "Prediction failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<PredictionResponse>, t: Throwable) {
                        hideLoading()
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to read image file", Toast.LENGTH_SHORT).show()
            Log.e("ImageUpload", "Error: ${e.message}", e)
        }
    }

    private fun showScanDialog(predictedLabel: String, remedy: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_scan_result, null)
        val tvPrediction = dialogView.findViewById<TextView>(R.id.tv_prediction)
        val tvRemedy = dialogView.findViewById<TextView>(R.id.tv_remedy)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        tvPrediction.text = "Disease Detected: $predictedLabel"
        tvRemedy.text = remedy

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)!!
        val file = File(outputDirectory, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file
    }

    private val outputDirectory: File
        get() = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    // Permission launchers using ActivityResultContract
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
                // Optionally navigate back or disable camera functionality
            }
        }

    private val requestGalleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            } else {
                Toast.makeText(requireContext(), "Permission denied to access gallery", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageGallery.setImageURI(uri)
                sendImageToApi(uri)
            }
        }

    private fun toggleFlash(flashToggle: ImageView) {
        camera?.let {
            val cameraInfo = it.cameraInfo
            val cameraControl = it.cameraControl

            if (cameraInfo.hasFlashUnit()) {
                isFlashOn = !isFlashOn
                cameraControl.enableTorch(isFlashOn)

                // Change icon
                if (isFlashOn) {
                    flashToggle.setImageResource(R.drawable.ic_flash_on)
                    Toast.makeText(requireContext(), "Flash ON", Toast.LENGTH_SHORT).show()
                } else {
                    flashToggle.setImageResource(R.drawable.ic_flash_off)
                    Toast.makeText(requireContext(), "Flash OFF", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No flash available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        @JvmStatic
        fun newInstance() = ScanFragment()
    }
}