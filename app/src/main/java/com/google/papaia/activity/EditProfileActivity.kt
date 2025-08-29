package com.google.papaia.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.papaia.R
import com.google.papaia.response.ApiResponse
import com.google.papaia.response.UserResponse
import com.google.papaia.utils.RetrofitClient
import com.google.papaia.utils.RetrofitClient.instance
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private lateinit var province: EditText
    private lateinit var zipcode: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        initializeViews()

        // Setup blur effect for profile picture
        setupProfilePicture()

        // Get shared preferences and tokens
        prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val userId = prefs.getString("id", null)
        val bearerToken = "Bearer $token"

        // Load user data
        loadUserData(bearerToken, userId!!)

        // Setup click listeners
        setupClickListeners(bearerToken, userId)
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

    private fun setupProfilePicture() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.userprofile)
        Blurry.with(this)
            .radius(15)
            .from(bitmap)
            .into(profilePic)
    }

    private fun loadUserData(bearerToken: String, userId: String) {
        RetrofitClient.instance.getUserById(bearerToken, userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()!!.user

                    // Safely update UI
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
                    province.setText(user.province)
                    zipcode.setText(user.zipCode)

                } else {
                    Log.e("GET_USER", "Failed to load user data: ${response.code()} ${response.message()}")
                    Toast.makeText(this@EditProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("GET_USER", "Exception: ${t.message}")
                Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners(bearerToken: String, userId: String) {
        // Back button click listener
        backButton.setOnClickListener {
            navigateBackToDashboard()
        }

        // Change photo button click listener
        changePhotoButton.setOnClickListener {
            // TODO: Implement photo picker functionality
            Toast.makeText(this, "Photo picker coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Birth date field click listener
        birthdate.setOnClickListener {
            // TODO: Implement date picker functionality
            Toast.makeText(this, "Date picker coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Save button click listener
        saveButton.setOnClickListener {
            updateUserProfile(bearerToken, userId)
        }
    }

    private fun updateUserProfile(bearerToken: String, userId: String) {
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

        val call = RetrofitClient.instance.updateUser(bearerToken, userId, updatedUser)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("UPDATE_USER", "Successfully updated user.")
                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                    // Update SharedPreferences with latest values
                    updateSharedPreferences()

                    navigateBackToDashboard()
                } else {
                    Log.e("UPDATE_USER", "Update failed: ${response.code()} ${response.message()}")
                    Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("UPDATE_USER", "Error: ${t.message}")
                Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun navigateBackToDashboard() {
        val intent = Intent(this@EditProfileActivity, DashboardActivity::class.java)
        intent.putExtra("navigateTo", "profile")
        startActivity(intent)
        finish()
    }
}