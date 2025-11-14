package com.google.papaia.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.papaia.R
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
        passwordStrengthBar = findViewById(R.id.password_strength_bar)

        setupPasswordValidation() // ✅ initialize validation listeners

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

            val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("token", null)
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
                            showSuccessDialog()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMsg = JSONObject(errorBody ?: "{}")
                                .optString("error", "Failed to change password")
                            showErrorDialog(errorMsg)
                        }
                    }

                    override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                        showErrorDialog("Error: ${t.message}")
                    }
                })
        }

        buttonBack.setOnClickListener {
            val intent = Intent()
            intent.putExtra("navigateTo", "profile")
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun setupPasswordValidation() {
        newPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                updatePasswordStrength(password)
                val confirmPassword = confirmNewPass.text.toString()
                if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                    tilConfirmPass.error = null
                }
            }
        })

        confirmNewPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = newPass.text.toString()
                if (confirmPassword.isNotEmpty() && password.isNotEmpty()) {
                    tilConfirmPass.error = if (password != confirmPassword) "Passwords do not match" else null
                }
            }
        })
    }

    /** ✅ Success Dialog */
    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success_change_password, null)
        val btnOk = dialogView.findViewById<MaterialButton>(R.id.button_ok)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        dialog.show()
    }

    /** ❌ Error Dialog */
    private fun showErrorDialog(errorMessage: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_error_change_password, null)
        val btnOk = dialogView.findViewById<MaterialButton>(R.id.button_ok)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogMessage)
        messageText.text = errorMessage

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
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
        if (password.length >= 8) score++
        if (password.length >= 12) score++
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

    enum class PasswordStrength { WEAK, MEDIUM, STRONG }
}
