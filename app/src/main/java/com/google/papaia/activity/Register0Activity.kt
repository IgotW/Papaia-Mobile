package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R

class Register0Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register0)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button_farmer = findViewById<Button>(R.id.reg0_button_farmer)
        val button_owner = findViewById<Button>(R.id.reg0_button_owner)
        val back_arrow = findViewById<ImageView>(R.id.reg0_arrow_back)

        button_farmer.setOnClickListener {
            val role = "farmer"
            startActivity(
                Intent(this, Register1Activity::class.java).apply {
                    putExtra("role", role)
                }
            )

        }

        button_owner.setOnClickListener {
            val role = "owner"
            startActivity(
                Intent(this, Register1Activity::class.java).apply {
                    putExtra("role", role)
                }
            )
        }

        back_arrow.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }
    }
}