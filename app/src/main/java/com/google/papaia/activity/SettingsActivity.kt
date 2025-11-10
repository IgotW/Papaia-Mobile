// SettingsActivity.kt
package com.google.papaia.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.response.ApiResponse
import com.google.papaia.utils.RetrofitClient
import com.google.papaia.utils.SecurePrefsHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var button_back: ImageView
    private lateinit var cardEditProfile: CardView
    private lateinit var cardChangePassword: CardView
    private lateinit var cardViewPlan: CardView
    private lateinit var cardManageBilling: CardView
    private lateinit var buttonDeactivate: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        setupInit()
        setupClickListeners()
    }

    private fun setupInit(){
        button_back = findViewById(R.id.btn_back)
        cardEditProfile = findViewById(R.id.card_edit_profile)
        cardChangePassword =  findViewById(R.id.card_change_password)
        cardViewPlan = findViewById(R.id.card_view_plan)
        cardManageBilling = findViewById(R.id.card_manage_billing)
        buttonDeactivate = findViewById(R.id.button_deactivate)
    }

    private fun setupClickListeners() {

        button_back.setOnClickListener {
            onBackPressed()
//            val intent = Intent(this, DashboardActivity::class.java)
//            intent.putExtra("navigateTo", "profile")
//            startActivity(intent)
//            finish()
        }

        // Profile & Account
        cardEditProfile.setOnClickListener {
            showToast("Edit Profile clicked")
            startActivity(
                Intent(this, EditProfileActivity::class.java)
            )
            // Navigate to EditProfileActivity
        }

        cardChangePassword.setOnClickListener {
            showToast("Change Password clicked")
            startActivity(
                Intent(this, ChangePasswordActivity::class.java)
            )
            // Navigate to ChangePasswordActivity
        }

        // Notifications
//        findViewById<CardView>(R.id.card_disease_alerts).setOnClickListener {
//            showToast("Disease Alerts clicked")
//            // Navigate to NotificationSettingsActivity
//        }

        // Subscription
        cardViewPlan.setOnClickListener {
            showToast("View Plan clicked")
            startActivity(Intent(this, PlansActivity::class.java))
        }

        cardManageBilling.setOnClickListener {
            showToast("Manage Billing clicked")
            // Navigate to BillingActivity
        }

        buttonDeactivate.setOnClickListener {
            showDeactivateDialog()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDeactivateDialog() {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_deactivate, null)
        val btnYes = dialogView.findViewById<MaterialButton>(R.id.button_yes)
        val btnNo = dialogView.findViewById<MaterialButton>(R.id.button_no)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // ❌ NO Button: just close the dialog
        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        // ✅ YES Button: call the deactivate API
        btnYes.setOnClickListener {
            dialog.dismiss()
            deactivateAccount() // 👈 calls the function below
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun deactivateAccount() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(this@SettingsActivity, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val bearerToken = "Bearer $token"

        RetrofitClient.instance.deactivateAccount(bearerToken)
            .enqueue(object : retrofit2.Callback<ApiResponse> {
                override fun onResponse(
                    call: retrofit2.Call<ApiResponse>,
                    response: retrofit2.Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@SettingsActivity,
                            "Account deactivated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ Clear user session and redirect to login
                        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()
                        SecurePrefsHelper.clearToken(this@SettingsActivity)

                        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@SettingsActivity,
                            "Failed to deactivate account",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("DEACTIVATE", "Response error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("DEACTIVATE", "Network error: ${t.message}")
                }
            })
    }


    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Handle logout logic
                showToast("Logged out successfully")
                // Clear SecurePrefsHelper (the saved JWT token)
                SecurePrefsHelper.clearToken(this@SettingsActivity)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}