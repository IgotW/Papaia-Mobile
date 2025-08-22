package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.papaia.R

class Register0Activity : AppCompatActivity() {
    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register0)

        setupUI()
        setupClickListeners()

    }
    private fun setupUI() {
        val continueButton = findViewById<Button>(R.id.btnContinue)

        // Initially disable continue button
        continueButton.isEnabled = false
        continueButton.alpha = 0.5f

        // Set initial card states
        resetCardSelection()
    }

    private fun setupClickListeners() {
        val cardFarmer = findViewById<MaterialCardView>(R.id.cardFarmer)
        val cardFarmOwner = findViewById<MaterialCardView>(R.id.cardFarmOwner)
        val continueButton = findViewById<Button>(R.id.btnContinue)
        val loginText = findViewById<TextView>(R.id.tvLogIn)

        // Farmer card click
        cardFarmer.setOnClickListener {
            selectRole("farmer")
        }

        // Farm Owner card click
        cardFarmOwner.setOnClickListener {
            selectRole("owner")
        }

        // Continue button click
        continueButton.setOnClickListener {
            if (selectedRole != null) {
                startActivity(
                    Intent(this, Register1Activity::class.java).apply {
                        putExtra("role", selectedRole)
                    }
                )
            }
        }

        // Log in text click
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    private fun selectRole(role: String) {
        selectedRole = role

        val cardFarmer = findViewById<MaterialCardView>(R.id.cardFarmer)
        val cardFarmOwner = findViewById<MaterialCardView>(R.id.cardFarmOwner)
        val continueButton = findViewById<Button>(R.id.btnContinue)

        // Reset all cards first
        resetCardSelection()

        // Highlight selected card
        when (role) {
            "farmer" -> {
                cardFarmer.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.selected_card_bg)
                )
                cardFarmer.strokeColor =
                    ContextCompat.getColor(this, R.color.primary)
                cardFarmer.strokeWidth = 4
            }
            "owner" -> {
                cardFarmOwner.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.selected_card_bg)
                )
                cardFarmOwner.strokeColor =
                    ContextCompat.getColor(this, R.color.tertiary)
                cardFarmOwner.strokeWidth = 4
            }
        }

        // Enable continue button
        continueButton.isEnabled = true
        continueButton.alpha = 1.0f
        continueButton.setBackgroundColor(
            ContextCompat.getColor(this, R.color.button_enabled)
        )
    }

    private fun resetCardSelection() {
        val cardFarmer = findViewById<MaterialCardView>(R.id.cardFarmer)
        val cardFarmOwner = findViewById<MaterialCardView>(R.id.cardFarmOwner)

        // Reset farmer card
        cardFarmer.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.white)
        )
        cardFarmer.strokeColor =
            ContextCompat.getColor(this, R.color.card_border)
        cardFarmer.strokeWidth = 1

        // Reset farm owner card
        cardFarmOwner.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.white)
        )
        cardFarmOwner.strokeColor =
            ContextCompat.getColor(this, R.color.card_border)
        cardFarmOwner.strokeWidth = 1
    }
}