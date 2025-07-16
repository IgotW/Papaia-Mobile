package com.google.papaia.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R
import jp.wasabeef.blurry.Blurry

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val edit_profilepic = findViewById<ImageView>(R.id.imageview_edit_profilepic)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.userprofile)

        Blurry.with(this) // Use 'this' instead of 'context'
            .radius(15)
            .from(bitmap)
            .into(edit_profilepic) // Use your variable name here
    }
}