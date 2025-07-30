package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.papaia.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val account = findViewById<LinearLayout>(R.id.ly_account)
        val subscription = findViewById<LinearLayout>(R.id.ly_subscription)
        val contact = findViewById<LinearLayout>(R.id.ly_contact)
        val terms = findViewById<LinearLayout>(R.id.ly_terms)
        val privacy = findViewById<LinearLayout>(R.id.ly_privacy)
        val about = findViewById<LinearLayout>(R.id.ly_about)
        val button_deactivate = findViewById<MaterialButton>(R.id.button_deactivate)

        about.setOnClickListener {
            startActivity(
                Intent(this, AboutActivity::class.java)
            )
        }
        contact.setOnClickListener {
            startActivity(
                Intent(this, ContactUsActivity::class.java)
            )
        }
        privacy.setOnClickListener {
            startActivity(
                Intent(this, PrivacyPolicyActivity::class.java)
            )
        }
        terms.setOnClickListener {
            startActivity(
                Intent(this, TermsConditionActivity::class.java)
            )
        }
        subscription.setOnClickListener {
            startActivity(
                Intent(this, SubscriptionActivity::class.java)
            )
        }
    }
}