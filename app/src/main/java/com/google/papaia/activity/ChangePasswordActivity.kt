package com.google.papaia.activity

import android.content.Context
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
import com.google.papaia.request.PasswordChangeRequest
import com.google.papaia.response.ChangePasswordResponse
import com.google.papaia.utils.RetrofitClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val oldPass = findViewById<EditText>(R.id.edittext_cp_olpass)
        val newPass = findViewById<EditText>(R.id.edittext_cp_newpass)
        val confirmNewPass = findViewById<EditText>(R.id.edittext_cp_confirmpass)
        val buttonReset = findViewById<MaterialButton>(R.id.button_fg3_reset)
        val buttonBack = findViewById<ImageView>(R.id.cp_btn_back)

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
}