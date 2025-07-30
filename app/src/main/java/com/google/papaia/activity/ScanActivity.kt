package com.google.papaia.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R
import com.google.papaia.response.PredictionResponse
import com.google.papaia.utils.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: FrameLayout
    private lateinit var imageGallery: ImageView  // Fixed this

    private var imageCapture: ImageCapture? = null
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageGallery.setImageURI(uri)
            sendImageToApi(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val scan_back_arrow = findViewById<ImageView>(R.id.scan_back_arrow)
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        imageGallery = findViewById(R.id.imageGallery)  // Fixed

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        btnCapture.setOnClickListener { takePhoto() }
        imageGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        scan_back_arrow.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("navigateTo", "home")
            startActivity(intent)
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            "JPEG_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Toast.makeText(applicationContext, "Photo saved!", Toast.LENGTH_SHORT).show()
//                    imageGallery.setImageURI(Uri.fromFile(photoFile)) // show thumbnail
//                    val savedUri: Uri = output.savedUri ?: Uri.fromFile(photoFile)
                    imageGallery.setImageURI(savedUri)
                    sendImageToApi(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(applicationContext, "Capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun sendImageToApi(imageUri: Uri) {
        try {
            val file = uriToFile(imageUri)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val token = "Bearer " + (sharedPref.getString("token", "") ?: "")

            val call = RetrofitClient.instance.predictDisease(multipartBody, token)
            call.enqueue(object : retrofit2.Callback<PredictionResponse> {
                override fun onResponse(
                    call: retrofit2.Call<PredictionResponse>,
                    response: retrofit2.Response<PredictionResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!.predictedLabel
                        Toast.makeText(applicationContext, "Predicted: $result", Toast.LENGTH_LONG).show()

                        val remedy = when (result.lowercase()) {
                            "anthracnose" -> "Apply copper-based fungicides. Avoid overhead watering."
                            "blackspot" -> "Use neem oil weekly. Remove infected leaves immediately."
                            "powdery mildew" -> "Improve air circulation. Use sulfur sprays."
                            else -> "No specific remedy found. Consult local expert."
                        }
                        showScanDialog(result, remedy)

//                        val builder = android.app.AlertDialog.Builder(this@ScanActivity)
//                        builder.setTitle("Prediction Result")
//                        builder.setMessage("Detected Disease: $result")
//                        builder.setPositiveButton("OK") { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                        val dialog = builder.create()
//                        dialog.show()

                    } else {
                        Toast.makeText(applicationContext, "Prediction failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<PredictionResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read image file", Toast.LENGTH_SHORT).show()
            Log.e("ImageUpload", "Error: ${e.message}", e)
        }
    }

    //dialog box pop up after sending image
    private fun showScanDialog(predictedLabel: String, remedy: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_scan_result, null)
        val tvPrediction = dialogView.findViewById<TextView>(R.id.tv_prediction)
        val tvRemedy = dialogView.findViewById<TextView>(R.id.tv_remedy)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        tvPrediction.text = "Disease Detected: $predictedLabel"
        tvRemedy.text = remedy

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file
    }

    private val outputDirectory: File
        get() = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
