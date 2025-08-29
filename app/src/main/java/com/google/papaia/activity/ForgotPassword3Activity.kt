package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.papaia.R
import com.google.papaia.request.ResetPasswordRequest
import com.google.papaia.response.ApiResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword3Activity : AppCompatActivity() {

    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var editTextNewPass: TextInputEditText
    private lateinit var editTextConfirmPass: TextInputEditText
    private lateinit var buttonReset: MaterialButton
    private lateinit var passwordStrengthBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backArrow = findViewById<ImageView>(R.id.fg3_back_arrow)
        tilNewPassword = findViewById(R.id.til_new_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        editTextNewPass = findViewById(R.id.edittext_fg3_newpass)
        editTextConfirmPass = findViewById(R.id.edittext_fg3_confirmpass)
        buttonReset = findViewById(R.id.button_fg3_reset)
        passwordStrengthBar = findViewById(R.id.password_strength_bar)

        var userId = ""
        intent?.let {
            it.getStringExtra("userId")?.let { id ->
                userId = id
            }
        }

        // Set up password validation
        setupPasswordValidation()

        buttonReset.setOnClickListener {
            val password = editTextNewPass.text.toString().trim()
            val confirmPassword = editTextConfirmPass.text.toString().trim()

            // Validate inputs
            if (!validateInputs(password, confirmPassword)) {
                return@setOnClickListener
            }

            // Disable button during API call
            buttonReset.isEnabled = false
            buttonReset.text = "Saving..."

            val request = ResetPasswordRequest(userId, password)

            RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    // Re-enable button
                    buttonReset.isEnabled = true
                    buttonReset.text = "Save New Password"

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ForgotPassword3Activity, "Password reset successfully!", Toast.LENGTH_SHORT).show()

                        // Clear the activity stack and go to login
                        val intent = Intent(this@ForgotPassword3Activity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = response.body()?.message ?: "Failed to reset password"
                        Toast.makeText(this@ForgotPassword3Activity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // Re-enable button
                    buttonReset.isEnabled = true
                    buttonReset.text = "Save New Password"

                    Toast.makeText(this@ForgotPassword3Activity, "Network error. Please try again.", Toast.LENGTH_LONG).show()
                }
            })
        }

        backArrow.setOnClickListener {
            finish() // Just finish instead of creating new intent
        }
    }

    private fun setupPasswordValidation() {
        // Password strength validation
        editTextNewPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                updatePasswordStrength(password)

                // Clear confirm password error if passwords now match
                val confirmPassword = editTextConfirmPass.text.toString()
                if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                    tilConfirmPassword.error = null
                }
            }
        })

        // Confirm password validation
        editTextConfirmPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = editTextNewPass.text.toString()

                if (confirmPassword.isNotEmpty() && password.isNotEmpty()) {
                    if (password != confirmPassword) {
                        tilConfirmPassword.error = "Passwords do not match"
                    } else {
                        tilConfirmPassword.error = null
                    }
                }
            }
        })
    }

    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            passwordStrengthBar.visibility = View.INVISIBLE
            return
        }

        passwordStrengthBar.visibility = View.VISIBLE

        val strength = calculatePasswordStrength(password)
        val strengthDrawable = when (strength) {
            PasswordStrength.WEAK -> R.drawable.password_strength_weak
            PasswordStrength.MEDIUM -> R.drawable.password_strength_medium
            PasswordStrength.STRONG -> R.drawable.password_strength_strong
        }

        passwordStrengthBar.background = ContextCompat.getDrawable(this, strengthDrawable)
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0

        // Length check
        if (password.length >= 8) score++
        if (password.length >= 12) score++

        // Character variety checks
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score < 3 -> PasswordStrength.WEAK
            score < 5 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    private fun validateInputs(password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Clear previous errors
        tilNewPassword.error = null
        tilConfirmPassword.error = null

        // Check if fields are empty
        if (password.isEmpty()) {
            tilNewPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            tilNewPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}