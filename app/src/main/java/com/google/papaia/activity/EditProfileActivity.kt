package com.google.papaia.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.papaia.R
import com.google.papaia.response.ApiResponse
import com.google.papaia.response.UploadResponse
import com.google.papaia.response.UserResponse
import com.google.papaia.utils.RetrofitClient
import com.yalantis.ucrop.UCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var profilePic: ImageView
    private lateinit var backButton: ImageView
    private lateinit var changePhotoButton: ImageView
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var firstname: EditText
    private lateinit var middlename: EditText
    private lateinit var lastname: EditText
    private lateinit var suffix: EditText
    private lateinit var contactnumber: EditText
    private lateinit var birthdate: EditText
    private lateinit var street: EditText
    private lateinit var barangay: EditText
    private lateinit var municipality: EditText
    private lateinit var province: AutoCompleteTextView
    private lateinit var zipcode: EditText
    private lateinit var saveButton: Button

    private var bearerToken: String? = null
    private var idNumber: String? = null
    private var isBlurred = false  // 🔹 NEW: track blur state

    // 🔹 Photo picker launcher
    // Replace existing launcher with UCrop integration
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                startCrop(it) // start UCrop instead of direct upload
            }
        }
    // UCrop launcher
    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let { uploadProfilePicture(it) }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
            }
        }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "${UUID.randomUUID()}.jpg"))

        val options = UCrop.Options().apply {
            setCircleDimmedLayer(true) // crop frame is circle
            setFreeStyleCropEnabled(true) // allow rotation/freestyle
            setShowCropFrame(true)
            setShowCropGrid(true)
        }

        val uCrop = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f) // force square
            .withOptions(options)

        cropLauncher.launch(uCrop.getIntent(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()

        prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        idNumber = prefs.getString("idNumber", null)
        bearerToken = "Bearer $token"

        setupDropdowns()

        loadUserData()

        setupClickListeners()
    }

    private fun initializeViews() {
        profilePic = findViewById(R.id.imageview_edit_profilepic)
        backButton = findViewById(R.id.btn_back)
        changePhotoButton = findViewById(R.id.btn_change_photo)
        username = findViewById(R.id.edittext_edit_username)
        email = findViewById(R.id.edittext_edit_email)
        firstname = findViewById(R.id.edittext_edit_firstname)
        middlename = findViewById(R.id.edittext_edit_middlename)
        lastname = findViewById(R.id.edittext_edit_lastname)
        suffix = findViewById(R.id.edittext_edit_suffix)
        contactnumber = findViewById(R.id.edittext_edit_contactnumber)
        birthdate = findViewById(R.id.edittext_edit_birthdate)
        street = findViewById(R.id.edittext_edit_street)
        barangay = findViewById(R.id.edittext_edit_barangay)
        municipality = findViewById(R.id.edittext_edit_municipality)
        province = findViewById(R.id.edittext_edit_province)
        zipcode = findViewById(R.id.edittext_edit_zipcode)
        saveButton = findViewById(R.id.btn_save_changes)
    }

    // 🔹 Load normal vs blurred image
    private fun showProfilePicture(url: String, blur: Boolean = false) {
        val glideRequest = Glide.with(this)
            .load(url)
            .placeholder(R.drawable.userprofile)

        if (blur) {
            glideRequest
                .transform(CenterCrop(), BlurTransformation(25, 3))
                .into(profilePic)
        } else {
            glideRequest
                .circleCrop()
                .into(profilePic)
        }
    }

    private fun loadUserData() {
        bearerToken?.let { token ->
            idNumber?.let { id ->
                RetrofitClient.instance.getUserById(token, id)
                    .enqueue(object : Callback<UserResponse> {
                        override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                val user = response.body()!!.user

                                username.setText(user.username)
                                email.setText(user.email)
                                firstname.setText(user.firstName)
                                middlename.setText(user.middleName)
                                lastname.setText(user.lastName)
                                suffix.setText(user.suffix)
                                contactnumber.setText(user.contactNumber)
                                birthdate.setText(user.birthDate)
                                street.setText(user.street)
                                barangay.setText(user.barangay)
                                municipality.setText(user.municipality)
                                province.setText(user.province, false)
                                zipcode.setText(user.zipCode)

                                // Load profile picture
                                user.profilePicture?.let { url ->
                                    Glide.with(this@EditProfileActivity)
                                        .load(url)
                                        .placeholder(R.drawable.userprofile)
                                        .transform(
                                            MultiTransformation(
                                                BlurTransformation(2, 1), // 🔹 blur
                                                ColorFilterTransformation(Color.parseColor("#80000000")), // 🔹 dark overlay
                                                CircleCrop() // 🔹 circular crop
                                            )
                                        )
                                        .into(profilePic)
                                }

                            } else {
                                Toast.makeText(this@EditProfileActivity, "Failed to load user", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                            Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("navigateTo", "profile")
            setResult(RESULT_OK, intent)
            finish()
        }

        changePhotoButton.setOnClickListener {
            // 🔹 Toggle blur instead of immediately launching picker
            isBlurred = !isBlurred
            val url = prefs.getString("profileImageUrl", null)
            if (!url.isNullOrEmpty()) {
                showProfilePicture(url, blur = isBlurred)
            } else {
                pickImageLauncher.launch("image/*") // fallback if no saved photo
            }
        }

        saveButton.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun updateUserProfile() {
        val updatedUser = mutableMapOf<String, String>()
        updatedUser["firstName"] = firstname.text.toString()
        updatedUser["middleName"] = middlename.text.toString()
        updatedUser["lastName"] = lastname.text.toString()
        updatedUser["suffix"] = suffix.text.toString()
        updatedUser["contactNumber"] = contactnumber.text.toString()
        updatedUser["birthDate"] = birthdate.text.toString()
        updatedUser["street"] = street.text.toString()
        updatedUser["barangay"] = barangay.text.toString()
        updatedUser["municipality"] = municipality.text.toString()
        updatedUser["province"] = province.text.toString()
        updatedUser["zipCode"] = zipcode.text.toString()

        bearerToken?.let { token ->
            idNumber?.let { id ->
                RetrofitClient.instance.updateUser(token, id, updatedUser)
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(this@EditProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                                updateSharedPreferences()
                                navigateBackToDashboard()
                            } else {
                                Toast.makeText(this@EditProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        val file = File(uri.path!!)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        bearerToken?.let { token ->
            idNumber?.let { id ->
                RetrofitClient.instance.updateProfilePicture(token, body)
                    .enqueue(object : Callback<UploadResponse> {
                        override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                            if (response.isSuccessful) {
                                val newImageUrl = response.body()?.profilePicture
                                if (!newImageUrl.isNullOrEmpty()) {
//                                    Glide.with(this@EditProfileActivity)
//                                        .load(newImageUrl)
//                                        .placeholder(R.drawable.userprofile)
//                                        .into(profilePic)

                                    prefs.edit().putString("profileImageUrl", newImageUrl).apply()
                                    showProfilePicture(newImageUrl, blur = false)
                                    Toast.makeText(this@EditProfileActivity, "Photo updated!", Toast.LENGTH_SHORT).show()

                                    // Save to prefs
//                                    prefs.edit().putString("profileImageUrl", newImageUrl).apply()
//
//                                    Toast.makeText(this@EditProfileActivity, "Photo updated!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@EditProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                            Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }

    private fun updateSharedPreferences() {
        val editor = prefs.edit()
        editor.putString("firstname", firstname.text.toString())
        editor.putString("middlename", middlename.text.toString())
        editor.putString("lastname", lastname.text.toString())
        editor.putString("suffix", suffix.text.toString())
        editor.putString("contactnumber", contactnumber.text.toString())
        editor.putString("birthdate", birthdate.text.toString())
        editor.putString("street", street.text.toString())
        editor.putString("barangay", barangay.text.toString())
        editor.putString("municipality", municipality.text.toString())
        editor.putString("province", province.text.toString())
        editor.putString("zipcode", zipcode.text.toString())
        editor.apply()
    }

    private fun setupDropdowns() {
        // Philippines provinces dropdown
        val provinces = arrayOf(
            "Abra", "Agusan del Norte", "Agusan del Sur", "Aklan", "Albay", "Antique",
            "Apayao", "Aurora", "Basilan", "Bataan", "Batanes", "Batangas", "Benguet",
            "Biliran", "Bohol", "Bukidnon", "Bulacan", "Cagayan", "Camarines Norte",
            "Camarines Sur", "Camiguin", "Capiz", "Catanduanes", "Cavite", "Cebu",
            "Compostela Valley", "Cotabato", "Davao del Norte", "Davao del Sur",
            "Davao Oriental", "Dinagat Islands", "Eastern Samar", "Guimaras", "Ifugao",
            "Ilocos Norte", "Ilocos Sur", "Iloilo", "Isabela", "Kalinga", "Laguna",
            "Lanao del Norte", "Lanao del Sur", "La Union", "Leyte", "Maguindanao",
            "Marinduque", "Masbate", "Metro Manila", "Misamis Occidental", "Misamis Oriental",
            "Mountain Province", "Negros Occidental", "Negros Oriental", "Northern Samar",
            "Nueva Ecija", "Nueva Vizcaya", "Occidental Mindoro", "Oriental Mindoro",
            "Palawan", "Pampanga", "Pangasinan", "Quezon", "Quirino", "Rizal", "Romblon",
            "Samar", "Sarangani", "Siquijor", "Sorsogon", "South Cotabato", "Southern Leyte",
            "Sultan Kudarat", "Sulu", "Surigao del Norte", "Surigao del Sur", "Tarlac",
            "Tawi-Tawi", "Zambales", "Zamboanga del Norte", "Zamboanga del Sur",
            "Zamboanga Sibugay"
        )
        val provinceAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, provinces)
        province.setAdapter(provinceAdapter)

        // show all options when clicked or focused
        province.setOnClickListener { province.showDropDown() }
        province.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) province.showDropDown()
        }

        province.threshold = 0
    }

    private fun navigateBackToDashboard() {
        val intent = Intent(this@EditProfileActivity, DashboardActivity::class.java)
        intent.putExtra("navigateTo", "profile")
        startActivity(intent)
        finish()
    }
}
