package com.google.papaia.activity

import android.content.Intent
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
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.request.OtpRequest
import com.google.papaia.response.OtpResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back_arrow = findViewById<ImageView>(R.id.fg2_back_arrow)
        val edittext_code1 = findViewById<EditText>(R.id.edittext_code_1)
        val edittext_code2 = findViewById<EditText>(R.id.edittext_code_2)
        val edittext_code3 = findViewById<EditText>(R.id.edittext_code_3)
        val edittext_code4 = findViewById<EditText>(R.id.edittext_code_4)
        val button_verify = findViewById<MaterialButton>(R.id.button_fg2_verify)
        var email = ""

        intent?.let {
            it.getStringExtra("email")?.let {mail ->
                email = mail
            }
        }

        button_verify.setOnClickListener {
            val otp = edittext_code1.text.toString() +
                    edittext_code2.text.toString() +
                    edittext_code3.text.toString() +
                    edittext_code4.text.toString()

            if (otp.length != 4 || email.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter the 4-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = OtpRequest(email, otp)

            RetrofitClient.instance.verifyOtp(request).enqueue(object : Callback<OtpResponse> {
                override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val userId = response.body()!!.userId
                        Log.d("OTP", "OTP verified. User ID: $userId")

                        startActivity(
                            Intent(this@ForgotPassword2Activity, ForgotPassword3Activity::class.java).apply {
                                putExtra("userId", userId)
                            }
                        )

                    } else {
                        Log.e("OTP", "Verification failed: ${response.body()?.message}")
                        Toast.makeText(this@ForgotPassword2Activity, "Invalid or expired code", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                    Log.e("OTP", "Network error: ${t.message}")
                    Toast.makeText(this@ForgotPassword2Activity, "Failed to verify code: ${t.message}", Toast.LENGTH_SHORT).show()

                }
            })
        }
        back_arrow.setOnClickListener {
            startActivity(
                Intent(this, ForgotPassword1Activity::class.java)
            )
        }
    }
}