package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.papaia.R

class BillingActivity : AppCompatActivity() {
    private lateinit var btn_back: ImageView
    private lateinit var btn_add: MaterialButton
    private lateinit var btn_change_plan: MaterialButton
    private lateinit var btn_update_payment: MaterialButton
    private lateinit var btn_remove_payment: MaterialButton
    private lateinit var switch_auto_renew: SwitchCompat
    private lateinit var download_invoice_card: androidx.cardview.widget.CardView
    private lateinit var cancel_subscription_card: androidx.cardview.widget.CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_billing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupInit()
        setupClickListener()
    }
    private fun setupInit(){
        btn_back = findViewById(R.id.btn_back)
        btn_add = findViewById(R.id.btn_add_payment)
        btn_change_plan = findViewById(R.id.btn_change_plan)
        btn_update_payment = findViewById(R.id.btn_update_payment)
        btn_remove_payment = findViewById(R.id.btn_remove_payment)
        switch_auto_renew = findViewById(R.id.switch_auto_renew)
        download_invoice_card = findViewById(R.id.download_invoice_card)
        cancel_subscription_card = findViewById(R.id.cancel_subscription_card)
    }
    private fun setupClickListener(){
        btn_back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("navigateTo", "profile")
            setResult(RESULT_OK, intent)
            finish()
        }
        btn_add.setOnClickListener {
            Toast.makeText(this, "Add button clicked", Toast.LENGTH_SHORT).show()
        }

        btn_change_plan.setOnClickListener {
            Toast.makeText(this, "Change Plan clicked", Toast.LENGTH_SHORT).show()
        }

        btn_update_payment.setOnClickListener {
            Toast.makeText(this, "Update Payment Method clicked", Toast.LENGTH_SHORT).show()
        }

        btn_remove_payment.setOnClickListener {
            Toast.makeText(this, "Remove Payment Method clicked", Toast.LENGTH_SHORT).show()
        }

        switch_auto_renew.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Auto-renew enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Auto-renew disabled", Toast.LENGTH_SHORT).show()
            }
        }

        download_invoice_card.setOnClickListener {
            Toast.makeText(this, "Download Invoice clicked", Toast.LENGTH_SHORT).show()
        }

        cancel_subscription_card.setOnClickListener {
            Toast.makeText(this, "Cancel Subscription clicked", Toast.LENGTH_SHORT).show()
        }
    }
}