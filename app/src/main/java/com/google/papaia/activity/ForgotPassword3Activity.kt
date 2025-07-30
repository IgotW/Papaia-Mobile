package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
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
import com.google.papaia.request.ResetPasswordRequest
import com.google.papaia.response.ApiResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPassword3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back_arrow = findViewById<ImageView>(R.id.fg3_back_arrow)
        val edittext_newpass = findViewById<EditText>(R.id.edittext_fg3_newpass)
        val edittext_confirmpass = findViewById<EditText>(R.id.edittext_fg3_confirmpass)
        val button_reset = findViewById<MaterialButton>(R.id.button_fg3_reset)
        var userId = ""
        intent?.let {
            it.getStringExtra("userId")?.let { id ->
                userId = id
            }
        }

        button_reset.setOnClickListener {
            val password = edittext_newpass.text.toString()
            val confirmPassword = edittext_confirmpass.text.toString()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ResetPasswordRequest(userId, password)

            RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ForgotPassword3Activity, "Password reset successfully", Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(this@ForgotPassword3Activity, LoginActivity::class.java)
                        )
                        finish()
                    } else {
                        Toast.makeText(this@ForgotPassword3Activity, "Failed to reset password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@ForgotPassword3Activity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
        back_arrow.setOnClickListener {
            startActivity(
                Intent(this, ForgotPassword2Activity::class.java)
            )
        }
    }
}