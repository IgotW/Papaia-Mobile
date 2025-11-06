package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.request.OtpRequest
import com.google.papaia.response.OtpResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword2Activity : AppCompatActivity() {

    private lateinit var editTextFields: List<EditText>
    private lateinit var buttonVerify: MaterialButton
    private lateinit var backArrow: ImageView
    private lateinit var editTextCode1: EditText
    private lateinit var editTextCode2: EditText
    private lateinit var editTextCode3: EditText
    private lateinit var editTextCode4: EditText
    private lateinit var tvResendCode: TextView
    private lateinit var tvTimer: TextView

    private var countDownTimer: CountDownTimer? = null
    private val COUNTDOWN_TIME = 120000L // 2 minutes in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backArrow = findViewById(R.id.fg2_back_arrow)
        editTextCode1 = findViewById(R.id.edittext_code_1)
        editTextCode2 = findViewById(R.id.edittext_code_2)
        editTextCode3 = findViewById(R.id.edittext_code_3)
        editTextCode4 = findViewById(R.id.edittext_code_4)
        buttonVerify = findViewById(R.id.button_fg2_verify)
        tvResendCode = findViewById(R.id.tv_resend_code)
        tvTimer = findViewById(R.id.tv_timer)

        // Store edit text fields for easy access
        editTextFields = listOf(editTextCode1, editTextCode2, editTextCode3, editTextCode4)

        var email = ""
        intent?.let {
            it.getStringExtra("email")?.let { mail ->
                email = mail
            }
        }

        // Set up auto-focus and validation
        setupOtpInputs()

        buttonVerify.setOnClickListener {
            val otp = editTextFields.joinToString("") { it.text.toString() }

            if (otp.length != 4 || email.isEmpty()) {
                Toast.makeText(this, "Please enter the 4-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button during verification
            buttonVerify.isEnabled = false
            buttonVerify.text = "Verifying..."

            val request = OtpRequest(email, otp)

            RetrofitClient.instance.verifyOtp(request).enqueue(object : Callback<OtpResponse> {
                override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                    // Re-enable button
                    buttonVerify.isEnabled = true
                    buttonVerify.text = "Verify Code"

                    if (response.isSuccessful && response.body()?.success == true) {
                        val userId = response.body()!!.userId
                        Log.d("OTP", "OTP verified. User ID: $userId")

                        startActivity(
                            Intent(this@ForgotPassword2Activity, ForgotPassword3Activity::class.java).apply {
                                putExtra("userId", userId)
                            }
                        )
                        finish() // Close this activity

                    } else {
                        Log.e("OTP", "Verification failed: ${response.body()?.message}")
                        Toast.makeText(this@ForgotPassword2Activity, "Invalid or expired code", Toast.LENGTH_SHORT).show()
                        // Clear all fields on error
                        editTextFields.forEach { it.text.clear() }
                        editTextFields.first().requestFocus()
                    }
                }

                override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                    // Re-enable button
                    buttonVerify.isEnabled = true
                    buttonVerify.text = "Verify Code"

                    Log.e("OTP", "Network error: ${t.message}")
                    Toast.makeText(this@ForgotPassword2Activity, "Failed to verify code: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        backArrow.setOnClickListener {
            finish()
        }

        tvResendCode.setOnClickListener {
            if (tvResendCode.isEnabled) {
                Toast.makeText(this, "Resending code...", Toast.LENGTH_SHORT).show()
                // TODO: Add actual resend code API call here
                startCountdown()
            }
        }
    }

    private fun startCountdown() {
        // Disable resend code button
        tvResendCode.isEnabled = false
        tvResendCode.alpha = 0.5f

        // Cancel any existing timer
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(COUNTDOWN_TIME, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                tvTimer.text = "00:00"
                tvResendCode.isEnabled = true
                tvResendCode.alpha = 1.0f
            }
        }.start()
    }

    private fun setupOtpInputs() {
        editTextFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty() && s.length == 1) {
                        // Move to next field if available
                        if (index < editTextFields.size - 1) {
                            editTextFields[index + 1].requestFocus()
                        }
                    }

                    updateButtonState()
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        editTextFields[index - 1].requestFocus()
                        editTextFields[index - 1].text.clear()
                    }
                }
                false
            }
        }

        editTextFields.first().requestFocus()
    }

    private fun updateButtonState() {
        val allFieldsFilled = editTextFields.all { it.text.toString().isNotEmpty() }

        buttonVerify.isEnabled = allFieldsFilled
        buttonVerify.backgroundTintList = if (allFieldsFilled) {
            ContextCompat.getColorStateList(this, R.color.tertiary)
        } else {
            ContextCompat.getColorStateList(this, R.color.disabled_gray)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel timer to prevent memory leaks
        countDownTimer?.cancel()
    }
}