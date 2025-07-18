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

        val back_arrow = findViewById<ImageView>(R.id.fg1_back_arrow)
        val edittext_email = findViewById<EditText>(R.id.edittext_fg1_email)
        val button_send = findViewById<Button>(R.id.button_fg1_send)

        button_send.setOnClickListener {
            val email = edittext_email.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                val request = ForgotPassword1Request(email)
                RetrofitClient.instance.sendOtp(request).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@ForgotPassword1Activity, "OTP sent to email", Toast.LENGTH_SHORT).show()

                            startActivity(
                                Intent(this@ForgotPassword1Activity, ForgotPassword2Activity::class.java).apply {
                                    putExtra("email", email)
                                }
                            )
                        } else {
                            Toast.makeText(this@ForgotPassword1Activity, "Failed: ${response.body()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("ForgotPassword", "Error: ${t.message}")
                        Toast.makeText(this@ForgotPassword1Activity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
        back_arrow.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }

    }
}