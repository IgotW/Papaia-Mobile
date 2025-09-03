package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.request.LoginRequest
import com.google.papaia.response.LoginResponse
import com.google.papaia.utils.RetrofitClient
import com.google.papaia.utils.SecurePrefsHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Auto-login
        val token = SecurePrefsHelper.getToken(this)
        if (!token.isNullOrEmpty()) {
            // You could also decode the JWT to get the role if you want
            // For now, assume role stored separately or saved in SharedPreferences if needed
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val edittext_username = findViewById<EditText>(R.id.login_edittext_username)
        val edittext_password = findViewById<EditText>(R.id.login_edittext_password)
        val button_login = findViewById<MaterialButton>(R.id.button_login)
        val txtview_register = findViewById<TextView>(R.id.txtview_register)
        val progressBar = findViewById<ProgressBar>(R.id.login_progress_bar)
        val forgotPassword = findViewById<TextView>(R.id.txtview_login_forgotpass)

        button_login.setOnClickListener{
            val username = edittext_username.text.toString()
            val password = edittext_password.text.toString()

            if (username.isEmpty()) {
                edittext_username.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                edittext_password.error = "Password is required"
                return@setOnClickListener
            }

            // Show loading inside button
            progressBar.visibility = View.VISIBLE
            button_login.text = ""  // hide text
            button_login.isEnabled = false

            val loginRequest = LoginRequest(username, password)

            RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    // Reset button state
                    progressBar.visibility = View.GONE
                    button_login.text = "Login"
                    button_login.isEnabled = true

                    if (response.isSuccessful  && response.body() != null) {
                        val loginResponse = response.body()
                        val token = loginResponse?.token
                        val user = loginResponse?.user

                        // Save token securely (e.g., EncryptedSharedPreferences)
                        SecurePrefsHelper.saveToken(this@LoginActivity, token ?: "")
                        getSharedPreferences("prefs", MODE_PRIVATE).edit()
                            .putString("token", loginResponse?.token)
                            .putString("role", user?.role)
                            .putString("id", user?.id)
                            .putString("idNumber", user?.idNumber)
                            .putString("profileImage", user?.profilePicture)
                            .putString("email", user?.email)
                            .putString("username", user?.username)
                            .putString("firstname", user?.firstName)
                            .putString("middlename", user?.middleName)
                            .putString("lastname", user?.lastName)
                            .putString("birthdate", user?.birthDate)
                            .putString("contactNumber", user?.contactNumber)
                            .putString("suffix", user?.suffix)
                            .putString("street", user?.street)
                            .putString("barangay", user?.barangay)
                            .putString("municipality", user?.municipality)
                            .putString("province", user?.province)
                            .putString("zipcode", user?.zipCode)
                            .apply()

                        // Proceed to next screen
                        if(user?.role == "farmer"){
                            startActivity(
                                Intent(this@LoginActivity, DashboardActivity::class.java)
                            )
                            Toast.makeText(
                                this@LoginActivity,
                                "Welcome ${user?.username}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            startActivity(
                                Intent(this@LoginActivity, OwnerActivity::class.java)
                            )
                        }
                        finish()
                    } else {
                        val errorJson = response.errorBody()?.string()
                        val errorMessage = try {
                            org.json.JSONObject(errorJson).getString("error")
                        } catch (e: Exception) {
                            "Login failed"
                        }
                        Toast.makeText(
                            this@LoginActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("LOGIN_ERROR", "Invalid Password")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // Hide spinner
                    progressBar.visibility = View.GONE
                    button_login.text = "Login"
                    button_login.isEnabled = true

                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LOGIN_FAILURE", t.message ?: "Unknown error")
                }
            })
        }
        txtview_register.setOnClickListener {
            startActivity(
                Intent(this, Register0Activity::class.java)
            )
        }
        forgotPassword.setOnClickListener {
            startActivity(
                Intent(this, ForgotPassword1Activity::class.java)
            )
        }
    }
}