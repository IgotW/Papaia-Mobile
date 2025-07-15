package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R

class Register1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edittext_username = findViewById<EditText>(R.id.reg_edittext_username)
        val edittext_email = findViewById<EditText>(R.id.reg_edittext_email)
        val edittext_password = findViewById<EditText>(R.id.reg_edittext_password)
        val edittext_confirm_password = findViewById<EditText>(R.id.reg_edittext_confirm_password)
        val reg1_button_next = findViewById<Button>(R.id.reg1_button_next)
        val reg1_back = findViewById<ImageView>(R.id.reg1_back)
        var role = ""

        intent?.let {
            it.getStringExtra("role")?.let { roles ->
                role = roles
            }
        }

        reg1_button_next.setOnClickListener {
            val username = edittext_username.text.toString()
            val email = edittext_email.text.toString()
            val password = edittext_password.text.toString()
            val confirmPassword = edittext_confirm_password.text.toString()

            var hasError = false

            if(username.isEmpty()){
                edittext_username.error ="Username is required"
                hasError = true
            }
            if(email.isEmpty()){
                edittext_email.error ="Email is required"
                hasError = true
            }
            if(password.isEmpty()){
                edittext_password.error ="Password is required"
                hasError = true
            }
            if(confirmPassword.isEmpty()){
                edittext_confirm_password.error ="Please confirm your password"
                hasError = true
            }
            if (password != confirmPassword){
                edittext_confirm_password.error ="Passwords do not match"
                hasError = true
            }

            if (hasError) return@setOnClickListener

            startActivity(
                Intent(this, Register2Activity::class.java).apply {
                    putExtra("username", username)
                    putExtra("email", email)
                    putExtra("password", password)
                    putExtra("role", role)
                }
            )
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        reg1_back.setOnClickListener {
            startActivity(
                Intent(this, Register0Activity::class.java)
            )
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }
}