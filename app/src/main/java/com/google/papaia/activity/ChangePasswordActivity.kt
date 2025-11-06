package com.google.papaia.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.papaia.R
import com.google.papaia.activity.Register1Activity.PasswordStrength
import com.google.papaia.request.PasswordChangeRequest
import com.google.papaia.response.ChangePasswordResponse
import com.google.papaia.utils.RetrofitClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var oldPass: EditText
    private lateinit var newPass: EditText
    private lateinit var confirmNewPass: EditText
    private lateinit var tilConfirmPass: TextInputLayout
    private lateinit var buttonReset: MaterialButton
    private lateinit var buttonBack: ImageView
    private lateinit var passwordStrengthBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        oldPass = findViewById(R.id.edittext_cp_olpass)
        newPass = findViewById(R.id.edittext_cp_newpass)
        confirmNewPass = findViewById(R.id.edittext_cp_confirmpass)
        tilConfirmPass = findViewById(R.id.til_confirm_password)
        buttonReset = findViewById(R.id.button_fg3_reset)
        buttonBack = findViewById(R.id.cp_btn_back)

        buttonReset.setOnClickListener {
            val oldPassword = oldPass.text.toString().trim()
            val newPassword = newPass.text.toString().trim()
            val confirmPassword = confirmNewPass.text.toString().trim()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)

            if (token == null) {
                Toast.makeText(this, "Authentication token not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = PasswordChangeRequest(password = oldPassword, newPassword = newPassword)

            RetrofitClient.instance.changePassword("Bearer $token", request)
                .enqueue(object : Callback<ChangePasswordResponse> {
                    override fun onResponse(
                        call: Call<ChangePasswordResponse>,
                        response: Response<ChangePasswordResponse>
                    ) {
                        if (response.isSuccessful) {
                            val res = response.body()
                            Toast.makeText(this@ChangePasswordActivity, res?.message ?: "Password changed successfully", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMsg = JSONObject(errorBody ?: "{}").optString("error", "Failed to change password")
                            Toast.makeText(this@ChangePasswordActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                        Toast.makeText(this@ChangePasswordActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        buttonBack.setOnClickListener {
            Log.d("Change Password", "Back to Settings")
            val intent = Intent()
            intent.putExtra("navigateTo", "profile")
            setResult(RESULT_OK, intent)
            finish()
        }

    }

    private fun setupPasswordValidation() {
        // Password strength validation
        newPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                updatePasswordStrength(password)

                // Clear confirm password error if passwords now match
                val confirmPassword = confirmNewPass.text.toString()
                if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                    tilConfirmPass.error = null
                }
            }
        })

        // Confirm password validation
        confirmNewPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = newPass.text.toString()

                if (confirmPassword.isNotEmpty() && password.isNotEmpty()) {
                    if (password != confirmPassword) {
                        tilConfirmPass.error = "Passwords do not match"
                    } else {
                        tilConfirmPass.error = null
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
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}