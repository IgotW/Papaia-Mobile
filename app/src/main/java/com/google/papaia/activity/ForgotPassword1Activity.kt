package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.papaia.R
import com.google.papaia.request.ForgotPassword1Request
import com.google.papaia.response.ApiResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backArrow = findViewById<ImageView>(R.id.fg1_back_arrow)
        val editTextEmail = findViewById<TextInputEditText>(R.id.edittext_fg1_email)
        val buttonSend = findViewById<MaterialButton>(R.id.button_fg1_send)
        val tvSignIn = findViewById<TextView>(R.id.tv_sign_in)

        buttonSend.setOnClickListener {
            val email = editTextEmail.text.toString().trim()

            // Validate email
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button during API call
            buttonSend.isEnabled = false
            buttonSend.text = "Sending..."

            val request = ForgotPassword1Request(email)
            RetrofitClient.instance.sendOtp(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    // Re-enable button
                    buttonSend.isEnabled = true
                    buttonSend.text = "Send Reset Link"

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ForgotPassword1Activity, "Reset code sent to your email", Toast.LENGTH_SHORT).show()

                        startActivity(
                            Intent(this@ForgotPassword1Activity, ForgotPassword2Activity::class.java).apply {
                                putExtra("email", email)
                            }
                        )
                    } else {
                        val errorMessage = response.body()?.message ?: "Failed to send reset code"
                        Toast.makeText(this@ForgotPassword1Activity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // Re-enable button
                    buttonSend.isEnabled = true
                    buttonSend.text = "Send Reset Link"

                    Log.e("ForgotPassword", "Error: ${t.message}")
                    Toast.makeText(this@ForgotPassword1Activity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        backArrow.setOnClickListener {
            finish()
        }

        // Handle Sign In link
        tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}