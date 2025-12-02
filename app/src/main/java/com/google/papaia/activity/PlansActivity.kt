package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R

class PlansActivity : AppCompatActivity() {
    private lateinit var btn_back: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plans)
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
    }
    private fun setupClickListener(){
        btn_back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("navigateTo", "profile")
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}