// SettingsActivity.kt
package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
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
import com.google.papaia.utils.SecurePrefsHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var button_back: ImageView
    private lateinit var cardEditProfile: CardView
    private lateinit var cardChangePassword: CardView
    private lateinit var cardViewPlan: CardView
    private lateinit var cardManageBilling: CardView

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
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }

        cardManageBilling.setOnClickListener {
            showToast("Manage Billing clicked")
            // Navigate to BillingActivity
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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