package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R

class Register2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edittext_firstname = findViewById<EditText>(R.id.reg2_edittext_firstname)
        val edittext_middlename = findViewById<EditText>(R.id.reg2_edittext_middlename)
        val edittext_lastname = findViewById<EditText>(R.id.reg2_edittext_lastname)
        val edittext_suffix = findViewById<EditText>(R.id.reg2_edittext_suffix)
        val button_next = findViewById<Button>(R.id.reg2_button_next)
        val arrow_back = findViewById<ImageView>(R.id.reg2_arrow_back)
        var username = ""
        var email = ""
        var password = ""
        var role = ""

        intent?.let{
            it.getStringExtra("username")?.let { user->
                username = user
            }
            it.getStringExtra("email")?.let{mail ->
                email = mail
            }
            it.getStringExtra("password")?.let { pass ->
                password = pass
            }
            it.getStringExtra("role")?.let { roles ->
                role = roles
            }
        }

        button_next.setOnClickListener {
            val firstname = edittext_firstname.text.toString()
            val middlename = edittext_middlename.text.toString()
            val lastname = edittext_lastname.text.toString()
            val suffix = edittext_suffix.text.toString()

            var hasError = false

            if(firstname.isEmpty()){
                edittext_firstname.error = "First Name is required"
                hasError = true
            }
            if (lastname.isEmpty()){
                edittext_lastname.error = "Last Name is required"
                hasError = true
            }

            if (hasError) return@setOnClickListener

            startActivity(
                Intent(this, Register3Activity::class.java).apply {
                    putExtra("username", username)
                    putExtra("email", email)
                    putExtra("password", password)
                    putExtra("role", role)
                    putExtra("firstname", firstname)
                    putExtra("middlename", middlename)
                    putExtra("lastname", lastname)
                    putExtra("suffix", suffix)
                }
            )
        }

        arrow_back.setOnClickListener {
            startActivity(
                Intent(this, Register1Activity::class.java)
            )
        }

    }
}