package com.google.papaia.activity

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.google.android.material.imageview.ShapeableImageView
import com.google.papaia.R
import com.google.papaia.response.PredictionResponse
import com.google.papaia.utils.RetrofitClient
import com.google.papaia.utils.SecurePrefsHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: FrameLayout
    private lateinit var imageGallery: FrameLayout
    private lateinit var galleryThumbnail: ShapeableImageView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var capturedImageView: ImageView
    private lateinit var btnRetake: Button
    private lateinit var btnUsePhoto: Button

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var isFlashOn = false
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    private var capturedUri: Uri? = null

//    private var isViewReady = false
    private var arePermissionsGranted = false

    // single-permission launcher (clearer)
//    private val cameraPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
//            Log.d(TAG, "cameraPermissionLauncher: granted=$granted")
//            if (granted) {
//                // ensure view has been laid out before starting camera
//                previewView.post { startCamera() }
//            } else {
//                Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//    private val requestGalleryPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                galleryLauncher.launch("image/*")
//            } else {
//                Toast.makeText(requireContext(), "Permission denied to access gallery", Toast.LENGTH_SHORT).show()
//            }
//        }

    // Launcher for multiple permissions
//    private val requestPermissionsLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            val allGranted = permissions.all { it.value }
//            if (allGranted) {
//                previewView.post { startCamera() }
//            } else {
//                Toast.makeText(requireContext(), "Camera & Gallery permissions are required", Toast.LENGTH_SHORT).show()
//            }
//        }

//    private val requestPermissionsLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            arePermissionsGranted = permissions.all { it.value }
//            if (!arePermissionsGranted) {
//                Toast.makeText(requireContext(), "Camera & Gallery permissions are required", Toast.LENGTH_SHORT).show()
//            } else {
//                tryStartCamera()
//            }
//
////            if (permissions.entries.all { it.value }) {
////                startCamera()
////            } else {
////                Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
////            }
//        }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // All permissions granted, start camera
                previewView.post { startCamera() }
                loadGalleryThumbnail()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera & Gallery permissions are required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

//    private fun checkPermissions() {
//        arePermissionsGranted = allPermissionsGranted()
//        if (arePermissionsGranted) {
//            previewView.post { startCamera() }
//        } else {
//            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
//        }
//    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            // Permissions already granted, start camera immediately
            previewView.post { startCamera() }
        } else {
            // Request permissions
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun tryStartCamera() {
        if (arePermissionsGranted) {
            previewView.post { startCamera() }
        }
    }


    // Use this instead of single permission
    private fun checkPermissionsAndStart() {
        if (allPermissionsGranted()) {
            previewView.post { startCamera() }
        } else {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                capturedUri = it
                showLoading()
                sendImageToApi(it) // directly upload
            }
        }

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
        cameraExecutor = Executors.newSingleThreadExecutor()


//        isViewReady = true
//        checkPermissions()

        // Check permission & start camera if already granted, otherwise ask
//        checkPermissionsAndStart()

        val flashToggle: ImageView = view.findViewById(R.id.flash_toggle)
        flashToggle.setOnClickListener {
            toggleFlash(flashToggle)
        }
    }

    // ✅ Add this new method - called when fragment becomes visible
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            // Fragment is now visible - start camera
            checkPermissions()
        } else {
            // Fragment is hidden - optionally stop camera to save resources
            cameraProvider?.unbindAll()
        }
    }

    override fun onResume() {
        super.onResume()
//        checkPermissionsAndStart()
//        checkPermissions()
        // Only restart if fragment is visible and permissions granted
        if (!isHidden && allPermissionsGranted() && cameraProvider != null) {
            previewView.post { startCamera() }
        }
        loadGalleryThumbnail()
    }

    private fun initViews(view: View) {
        previewView = view.findViewById(R.id.previewView)
        btnCapture = view.findViewById(R.id.btnCapture)
        imageGallery = view.findViewById(R.id.imageGallery)
        galleryThumbnail = view.findViewById(R.id.galleryThumbnail)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        progressBar = view.findViewById(R.id.progressBar)

        capturedImageView = view.findViewById(R.id.capturedImageView)
        btnRetake = view.findViewById(R.id.btnRetake)
        btnUsePhoto = view.findViewById(R.id.btnUsePhoto)
    }

    private fun setupClickListeners() {
        btnCapture.setOnClickListener { takePhoto() }

        btnRetake.setOnClickListener {
            capturedUri = null
            capturedImageView.visibility = View.GONE
            previewView.visibility = View.VISIBLE
            btnCapture.visibility = View.VISIBLE
            imageGallery.visibility = View.VISIBLE

            // Hide parent + children
            val captureActions = requireView().findViewById<View>(R.id.captureActions)
            captureActions.visibility = View.GONE
        }

        btnUsePhoto.setOnClickListener {
            capturedUri?.let {
                showLoading() // 👈 show loading immediately when clicked
                btnUsePhoto.isEnabled = false // 👈 prevent multiple clicks
                sendImageToApi(it)
            }
//            capturedUri?.let {sendImageToApi(it) }
        }

//        imageGallery.setOnClickListener {
//            val permission =
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
//                    Manifest.permission.READ_MEDIA_IMAGES
//                } else {
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                }
//
//            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
//                galleryLauncher.launch("image/*")
//            } else {
//                requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
//            }
//        }
        imageGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

//    private fun checkCameraPermissionAndStart() {
//        val permission = Manifest.permission.CAMERA
//        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
//            // start camera once layout is ready
//            previewView.post { startCamera() }
//        } else {
//            Log.d(TAG, "Camera permission not granted; requesting.")
//            cameraPermissionLauncher.launch(permission)
//        }
//    }

    //new
    private fun loadGalleryThumbnail() {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            requireContext().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    requireActivity().runOnUiThread {
                        galleryThumbnail.setImageURI(contentUri)
                        galleryThumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading gallery thumbnail: ${e.message}", e)
        }
    }

    private fun startCamera() {
        try {
            Log.d(TAG, "startCamera() called")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    if (cameraProvider == null) {
                        cameraProvider = provider
                    }

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    provider.unbindAll()
                    camera = provider.bindToLifecycle(
                        viewLifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    Log.d("CameraX", "Camera started successfully")
                } catch (e: Exception) {
                    Log.e("CameraX", "Use case binding failed", e)
                    Toast.makeText(requireContext(), "Failed to open camera", Toast.LENGTH_SHORT).show()
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        } catch (e: Exception) {
            Log.e(TAG, "startCamera - exception", e)
            Toast.makeText(requireContext(), "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(outputDirectory, "JPEG_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedUri = Uri.fromFile(photoFile)
                    requireActivity().runOnUiThread {
                        showCapturedImage(capturedUri!!)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(requireContext(), "Capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showCapturedImage(uri: Uri) {
        previewView.visibility = View.GONE
        btnCapture.visibility = View.GONE
        imageGallery.visibility = View.GONE

        capturedImageView.setImageURI(uri)
        capturedImageView.visibility = View.VISIBLE

        // Show parent container and child buttons
        val captureActions = requireView().findViewById<View>(R.id.captureActions)
        captureActions.visibility = View.VISIBLE
        btnRetake.visibility = View.VISIBLE
        btnUsePhoto.visibility = View.VISIBLE
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
                        btnUsePhoto.isEnabled = true
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!.prediction ?: "Unknown"
                            val remedy = response.body()!!.suggestions
                                ?: "No specific remedy found. Consult local expert."
                            showScanDialog(result, remedy)
                            SecurePrefsHelper.getToken(requireContext())?.let {
                                (activity as? DashboardActivity)?.refreshAnalytics(it)
                            }
                        } else {
                            Log.e("API_ERROR", "Code: ${response.code()} Body: ${response.errorBody()?.string()}")
                            Toast.makeText(requireContext(), "Prediction failed", Toast.LENGTH_SHORT).show()
                        }
                        startCamera()
                    }

                    override fun onFailure(call: retrofit2.Call<PredictionResponse>, t: Throwable) {
                        hideLoading()
                        btnUsePhoto.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            hideLoading()
            Toast.makeText(requireContext(), "Failed to read image file", Toast.LENGTH_SHORT).show()
            Log.e("ImageUpload", "Error: ${e.message}", e)
            startCamera()
        }
    }

    private fun showScanDialog(predictedLabel: String, remedy: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_scan_result, null)
        val tvPrediction = dialogView.findViewById<TextView>(R.id.tv_prediction)
        val tvRemedy = dialogView.findViewById<TextView>(R.id.tv_remedy)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        tvPrediction.text = "Disease Detected: $predictedLabel"
        tvRemedy.text = remedy

        if (predictedLabel.equals("Healthy", ignoreCase = true)) {
            tvPrediction.setTextColor(Color.parseColor("#00712D"))
        } else {
            tvPrediction.setTextColor(Color.parseColor("#FF9100"))
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnOk.setOnClickListener {
            dialog.dismiss()
            // Restore preview + capture button
            previewView.visibility = View.VISIBLE
            btnCapture.visibility = View.VISIBLE
            imageGallery.visibility = View.VISIBLE
            capturedImageView.visibility = View.GONE
            btnRetake.visibility = View.GONE
            btnUsePhoto.visibility = View.GONE
        }

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

//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
//    }

//    private val requestPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            if (permissions.entries.all { it.value }) {
//                startCamera()
//            } else {
//                Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
//            }
//        }


    private val outputDirectory: File
        get() = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    private fun toggleFlash(flashToggle: ImageView) {
        camera?.let {
            val cameraInfo = it.cameraInfo
            val cameraControl = it.cameraControl

            if (cameraInfo.hasFlashUnit()) {
                isFlashOn = !isFlashOn
                cameraControl.enableTorch(isFlashOn)
                flashToggle.setImageResource(
                    if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off
                )
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
        cameraProvider?.unbindAll()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        @JvmStatic
        fun newInstance() = ScanFragment()
    }
}
