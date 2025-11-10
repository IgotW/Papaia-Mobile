package com.google.papaia.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.utils.SecurePrefsHelper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check token first
//        val token = SecurePrefsHelper.getToken(this)
//        if (!token.isNullOrEmpty()) {
//            // User is already logged in, skip Splash/Login
//            val intent = Intent(this, DashboardActivity::class.java)
//            startActivity(intent)
//            finish()
//            return
//        }

//        if (token != null && role != null) {
//            if (role.equals("farmer", ignoreCase = true)) {
//                // ✅ Auto-login farmer
//                startActivity(Intent(this, DashboardActivity::class.java))
//                finish()
//            } else {
//                // 🚫 Non-farmer should go to login (no auto-login)
//                startActivity(Intent(this, LoginActivity::class.java))
//                finish()
//            }
//        } else {
//            // 🚪 No saved login → Go to login
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }

        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val role = prefs.getString("role", null)

        if (token != null && role != null) {
            // User already logged in, skip splash UI
            if (role.equals("farmer", ignoreCase = true)) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        } else {
            // First time user - show the splash button
            val btnStart = findViewById<MaterialButton>(R.id.button_start)
            btnStart.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        var btnStart = findViewById<MaterialButton>(R.id.button_start)

        btnStart.setOnClickListener{
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }
    }
}