package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val btn_back = findViewById<ImageView>(R.id.btn_back)

        btn_back.setOnClickListener {
            Log.d("About Activity", "Go back to profile page")
            val intent = Intent()
            intent.putExtra("navigateTo", "profile")
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}