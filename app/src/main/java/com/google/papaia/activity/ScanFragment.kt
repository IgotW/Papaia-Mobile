package com.google.papaia.activity

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.util.Size
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

    private val MAX_UPLOAD_MB = 10

//    private var loadingHandler = android.os.Handler()
//    private var dotCount = 0

    //    private var isViewReady = false
    private var arePermissionsGranted = false


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

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            // Permissions already granted, start camera immediately
            previewView.post { startCamera() }
        } else {
            // Request permissions
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
        imageGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

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
            val fileSizeMB = getFileSizeInMB(imageUri)
            if (fileSizeMB > MAX_UPLOAD_MB) {
                hideLoading()
                Toast.makeText(
                    requireContext(),
                    "Image too large! Max allowed: $MAX_UPLOAD_MB MB\nSelected: ${"%.2f".format(fileSizeMB)} MB",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

//            val file = uriToFile(imageUri)
            val file = compressImageFile(uriToFile(imageUri))

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

//                        if (response.isSuccessful && response.body() != null) {
//                            val result = response.body()!!.prediction ?: "Unknown"
//                            val remedy = response.body()!!.suggestions
//                                ?: "No specific remedy found. Consult local expert."
//                            showScanDialog(result, remedy)
//                            SecurePrefsHelper.getToken(requireContext())?.let {
//                                (activity as? DashboardActivity)?.refreshAnalytics(it)
//                            }
//                        } else {
//                            Log.e("API_ERROR", "Code: ${response.code()} Body: ${response.errorBody()?.string()}")
//                            Toast.makeText(requireContext(), "Prediction failed", Toast.LENGTH_SHORT).show()
//                        }

                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!

                            // ✅ Handle case when model confidence < 0.5
                            if (!body.success) {
                                val confidencePercent = (body.confidence ?: 0.0) * 100
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Unrecognized Disease")
                                    .setMessage(
                                        "${body.message ?: "Disease not recognized. Please try again with a clearer image."}\n\n" +
                                                "AI Verified: ${"%.2f".format(confidencePercent)}%"
                                    )
                                    .setPositiveButton("Try Again") { dialog, _ ->
                                        dialog.dismiss()
                                        startCamera()
                                    }
                                    .setCancelable(false)
                                    .show()
                                return
                            }

                            // ✅ If prediction was successful
                            val result = body.prediction ?: "Unknown"
                            val remedy = body.suggestions
                                ?: "No specific remedy found. Consult local expert."

                            showScanDialog(result, remedy, body.confidence, capturedUri)

                            SecurePrefsHelper.getToken(requireContext())?.let {
                                (activity as? DashboardActivity)?.refreshAnalytics(it)
                            }
                        } else {
                            Log.e(
                                "API_ERROR",
                                "Code: ${response.code()} Body: ${response.errorBody()?.string()}"
                            )
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

    private fun showScanDialog(predictedLabel: String, remedy: String, confidence: Double? = null, imageUri: Uri? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_scan_result, null)
        val imagePrev = dialogView.findViewById<ImageView>(R.id.iv_result_image)
        val tvConfidence = dialogView.findViewById<TextView>(R.id.tv_confidence)
        val tvPrediction = dialogView.findViewById<TextView>(R.id.tv_prediction)
        val tvRemedy = dialogView.findViewById<TextView>(R.id.tv_remedy)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        tvPrediction.text = "Detected: $predictedLabel"
        tvPrediction.setTextColor(
            if (predictedLabel.equals("Healthy", ignoreCase = true))
                Color.parseColor("#00712D")
            else
                Color.parseColor("#FF9100")
        )
//        if (predictedLabel.equals("Healthy", ignoreCase = true)) {
//            tvPrediction.setTextColor(Color.parseColor("#00712D"))
//        } else {
//            tvPrediction.setTextColor(Color.parseColor("#FF9100"))
//        }

        tvConfidence.text = confidence?.let {
            "Confidence: ${"%.2f".format(it * 100)}%"
        } ?: ""

//        tvRemedy.text = remedy
        tvRemedy.text = formatRemedyText(remedy)

        if (imageUri != null) {
            imagePrev.visibility = View.VISIBLE
            imagePrev.setImageURI(imageUri)
        } else {
            imagePrev.visibility = View.GONE
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

            startCamera()
        }

        dialog.show()
    }

    private fun formatRemedyText(remedy: String?): CharSequence {
        if (remedy.isNullOrBlank()) {
            return "No treatment suggestions available."
        }

        // Split remedy by bullets or newlines for formatting
        val steps = remedy.split("*", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Format with circled numbers and emoji steps
        val formatted = steps.mapIndexed { index, step ->
            "<b>🌱 Suggestion ${getCircledNumber(index + 1)}</b><br>${step}"
        }.joinToString("<br><br>")

        // Render as styled HTML text
        return Html.fromHtml(formatted, Html.FROM_HTML_MODE_LEGACY)
    }

    private fun getCircledNumber(number: Int): String {
        return when (number) {
            1 -> "①"
            2 -> "②"
            3 -> "③"
            4 -> "④"
            5 -> "⑤"
            6 -> "⑥"
            7 -> "⑦"
            8 -> "⑧"
            9 -> "⑨"
            10 -> "⑩"
            else -> "⊙"
        }
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

    private fun compressImageFile(originalFile: File): File {
        val compressedFile = File(outputDirectory, "compressed_${System.currentTimeMillis()}.jpg")

        val bitmap = BitmapFactory.decodeFile(originalFile.path)

        val outputStream = FileOutputStream(compressedFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream) // 70% quality
        outputStream.flush()
        outputStream.close()

        return compressedFile
    }



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

    private fun getFileSizeInMB(uri: Uri): Double {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            it.moveToFirst()
            val size = it.getLong(sizeIndex)
            return size / (1024.0 * 1024.0) // Convert bytes → MB
        }
        return 0.0
    }


    private fun showLoading() {
        loadingOverlay.visibility = View.VISIBLE
    }

//    private fun showLoading() {
//        loadingOverlay.visibility = View.VISIBLE
//
//        val loadingText = view?.findViewById<TextView>(R.id.tvLoadingText)
//
//        dotCount = 0
//
//        loadingHandler.post(object : Runnable {
//            override fun run() {
//                dotCount = (dotCount + 1) % 4
//                val dots = ".".repeat(dotCount)
//                loadingText?.text = "Fetching result$dots"
//                loadingHandler.postDelayed(this, 500) // update every 0.5s
//            }
//        })
//    }

    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
    }

//    private fun hideLoading() {
//        loadingOverlay.visibility = View.GONE
//        loadingHandler.removeCallbacksAndMessages(null)
//
//        val loadingText = view?.findViewById<TextView>(R.id.tvLoadingText)
//        loadingText?.text = "Fetching result..."
//    }

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
