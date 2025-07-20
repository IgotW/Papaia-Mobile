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
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var firstname: EditText
    private lateinit var middlename: EditText
    private lateinit var lastname: EditText
    private lateinit var suffix: EditText
    private lateinit var street: EditText
    private lateinit var barangay: EditText
    private lateinit var municipality: EditText
    private lateinit var province: EditText
    private lateinit var zipcode: EditText
    private lateinit var button_edit: Button
    private lateinit var cancel: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        profilePic = findViewById(R.id.imageview_edit_profilepic)
        username = findViewById(R.id.edittext_edit_username)
        email = findViewById(R.id.edittext_edit_email)
        firstname = findViewById(R.id.edittext_edit_firstname)
        middlename = findViewById(R.id.edittext_edit_middlename)
        lastname = findViewById(R.id.edittext_edit_lastname)
        suffix = findViewById(R.id.edittext_edit_suffix)
        street = findViewById(R.id.edittext_edit_street)
        barangay = findViewById(R.id.edittext_edit_barangay)
        municipality = findViewById(R.id.edittext_edit_municipality)
        province = findViewById(R.id.edittext_edit_province)
        zipcode = findViewById(R.id.edittext_edit_zipcode)
        button_edit = findViewById(R.id.button_editprofile)
        cancel = findViewById(R.id.button_editprofile_cancel)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.userprofile)

        Blurry.with(this) // Use 'this' instead of 'context'
            .radius(15)
            .from(bitmap)
            .into(profilePic) // Use your variable name here

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        val token = prefs.getString("token", null)
        val userId = prefs.getString("id", null)

//        username.setText(prefs.getString("username", ""))
//        email.setText(prefs.getString("email", ""))
//        firstname.setText(prefs.getString("firstname", ""))
//        val middlenameValue = prefs.getString("middlename", "")
//        lastname.setText(prefs.getString("lastname", ""))
//        val suffixValue = prefs.getString("suffix", "")
//        street.setText(prefs.getString("street", ""))
//        barangay.setText(prefs.getString("barangay", ""))
//        municipality.setText(prefs.getString("municipality", ""))
//        province.setText(prefs.getString("province", ""))
//        zipcode.setText(prefs.getString("zipcode", ""))

//        if (middlenameValue.isNullOrBlank()) {
//            middlename.hint = null
//            middlename.setText("")
//        } else {
//            middlename.setText(middlenameValue)
//        }
//
//        if (suffixValue.isNullOrBlank()) {
//            suffix.hint = null
//            suffix.setText("")
//        } else {
//            suffix.setText(suffixValue)
//        }

//        lifecycleScope.launch {
//            try {
//                val response = withContext(Dispatchers.IO) {
//                    instance.getUserById(userId!!)
//                }
//
//                if (response.isSuccessful && response.body()?.success == true) {
//                    val user = response.body()!!.user
//                    // Update UI safely on main thread
//                    username.setText(user.username)
//                    email.setText(user.email)
//                    firstname.setText(user.firstName)
//                    middlename.setText(user.middleName)
//                    lastname.setText(user.lastName)
//                    suffix.setText(user.suffix)
//                    street.setText(user.street)
//                    barangay.setText(user.barangay)
//                    municipality.setText(user.municipality)
//                    province.setText(user.province)
//                    zipcode.setText(user.zipCode)
//                } else {
//                    Log.e("GET_USER", "Failed to load user data")
//                }
//            } catch (e: Exception) {
//                Log.e("GET_USER", "Exception: ${e.message}")
//            }
//        }

//        button_edit.setOnClickListener {
//            val updatedUser = mutableMapOf<String, String>()
//
//            updatedUser["firstName"] = firstname.text.toString()
//            updatedUser["middleName"] = middlename.text.toString()
//            updatedUser["lastName"] = lastname.text.toString()
//            updatedUser["suffix"] = suffix.text.toString()
//            updatedUser["street"] = street.text.toString()
//            updatedUser["barangay"] = barangay.text.toString()
//            updatedUser["municipality"] = municipality.text.toString()
//            updatedUser["province"] = province.text.toString()
//            updatedUser["zipCode"] = zipcode.text.toString()
//
//            lifecycleScope.launch {
//                try {
//                    val response = withContext(Dispatchers.IO) {
//                        instance.updateUser(userId, updatedUser)
//                    }
//
//                    if (response.isSuccessful) {
//                        Log.d("UPDATE_USER", "Successfully updated user.")
//                        // You can show a toast or finish activity
//                    } else {
//                        Log.e("UPDATE_USER", "Failed: ${response.code()} ${response.message()}")
//                    }
//                } catch (e: Exception) {
//                    Log.e("UPDATE_USER", "Exception: ${e.message}")
//                }
//            }
//        }

        val bearerToken = "Bearer $token"

        RetrofitClient.instance.getUserById( bearerToken, userId!!).enqueue(object : Callback<UserResponse> {
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

        button_edit.setOnClickListener {
            val updatedUser = mutableMapOf<String, String>()

            updatedUser["firstName"] = firstname.text.toString()
            updatedUser["middleName"] = middlename.text.toString()
            updatedUser["lastName"] = lastname.text.toString()
            updatedUser["suffix"] = suffix.text.toString()
            updatedUser["street"] = street.text.toString()
            updatedUser["barangay"] = barangay.text.toString()
            updatedUser["municipality"] = municipality.text.toString()
            updatedUser["province"] = province.text.toString()
            updatedUser["zipCode"] = zipcode.text.toString()

            val call = RetrofitClient.instance.updateUser( bearerToken, userId, updatedUser)

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d("UPDATE_USER", "Successfully updated user.")
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                        // ✅ Update SharedPreferences with latest values
                        val editor = prefs.edit()
                        editor.putString("firstname", firstname.text.toString())
                        editor.putString("middlename", middlename.text.toString())
                        editor.putString("lastname", lastname.text.toString())
                        editor.putString("suffix", suffix.text.toString())
                        editor.putString("street", street.text.toString())
                        editor.putString("barangay", barangay.text.toString())
                        editor.putString("municipality", municipality.text.toString())
                        editor.putString("province", province.text.toString())
                        editor.putString("zipcode", zipcode.text.toString())
                        editor.apply()

                        val intent = Intent(this@EditProfileActivity, DashboardActivity::class.java)
                        intent.putExtra("navigateTo", "profile")
                        startActivity(intent)
                        finish()
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

        cancel.setOnClickListener {
            val intent = Intent(this@EditProfileActivity, DashboardActivity::class.java)
            intent.putExtra("navigateTo", "profile")
            startActivity(intent)
            finish()
        }
    }

}