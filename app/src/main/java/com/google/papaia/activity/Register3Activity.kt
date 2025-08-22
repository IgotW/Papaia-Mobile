package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.papaia.R
import com.google.papaia.model.User
import com.google.papaia.request.RegisterRequest
import com.google.papaia.response.ApiResponse
import com.google.papaia.response.RegisterResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Register3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val edittext_street = findViewById<EditText>(R.id.reg3_edittext_street)
        val edittext_barangay = findViewById<EditText>(R.id.reg3_edittext_barangay)
        val edittext_municipality = findViewById<EditText>(R.id.reg3_edittext_municipality)
        val edittext_province = findViewById<EditText>(R.id.reg3_edittext_province)
        val edittext_zipcode = findViewById<EditText>(R.id.reg3_edittext_zipcode)
        val button_create = findViewById<Button>(R.id.reg3_button_create)
        val back_arrow = findViewById<ImageView>(R.id.reg3_arrow_back)
        val progressBar = findViewById<ProgressBar>(R.id.reg_progress_bar)
        var username = ""
        var email = ""
        var password = ""
        var role = ""
        var firstname = ""
        var middlename = ""
        var lastname = ""
        var suffix = ""

        intent?.let {
            it.getStringExtra("username")?.let { user ->
                username = user
            }
            it.getStringExtra("email")?.let { mail ->
                email = mail
            }
            it.getStringExtra("password")?.let { pass ->
                password = pass
            }
            it.getStringExtra("role")?.let { roles ->
                role = roles
            }
            it.getStringExtra("firstname")?.let { first ->
                firstname = first
            }
            it.getStringExtra("middlename")?.let { middle ->
                middlename = middle
            }
            it.getStringExtra("lastname")?.let { last ->
                lastname = last
            }
            it.getStringExtra("suffix")?.let { suf ->
                suffix = suf
            }
        }

//        button_create.setOnClickListener {
//            val street = edittext_street.text.toString()
//            val barangay = edittext_barangay.text.toString()
//            val municipality = edittext_municipality.text.toString()
//            val province = edittext_province.text.toString()
//            val zipCode = edittext_zipcode.text.toString()
//
//            progressBar.visibility = View.VISIBLE
//            button_create.isEnabled = false
//
//            var hasError = false
//
//            if (street.isEmpty()) {
//                edittext_street.error = "Street is required"
//                hasError = true
//            }
//            if (barangay.isEmpty()) {
//                edittext_barangay.error = "Barangay is required"
//                hasError = true
//            }
//            if (municipality.isEmpty()) {
//                edittext_municipality.error = "Municipality is required"
//                hasError = true
//            }
//            if (province.isEmpty()) {
//                edittext_province.error = "Province is required"
//                hasError = true
//            }
//
//            if (hasError) return@setOnClickListener
//
//            try {
////                val register = RegisterRequest(
////                    username, email, password, role, firstname, middlename,
////                    lastname, suffix, street, barangay, municipality, province, zipCode
////                )
//                RetrofitClient.instance.registerUser(register).enqueue(object : Callback<RegisterResponse> {
//                    override fun onResponse(
//                        call: Call<RegisterResponse>,
//                        response: Response<RegisterResponse>
//                    ) {
//                        // Hide spinner
//                        progressBar.visibility = View.GONE
//                        button_create.isEnabled = true
//
//                        if (response.isSuccessful && response.body() != null) {
//                                // ✅ Registration successful
//                                runOnUiThread {
//                                    Toast.makeText(
//                                        this@Register3Activity,
//                                        "Registered Successfully",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                    Log.d("API_SUCCESS", response.body().toString())
//
//                                    startActivity(
//                                        Intent(this@Register3Activity, LoginActivity::class.java)
//                                    )
//                                }
//                        } else {
//                            runOnUiThread {
//                                Toast.makeText(
//                                    this@Register3Activity,
//                                    "Registration Failed",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                Log.e("API_ERROR", response.errorBody()?.string().toString())
//                            }
//                        }
//                    }
//
//                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
//                        progressBar.visibility = View.GONE
//                        button_create.isEnabled = true
//                        // Handle failure
//                        runOnUiThread {
//                            Toast.makeText(
//                                this@Register3Activity,
//                                "Error: ${t.message}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            Log.e("API_EXCEPTION", t.toString())
//                        }
//                    }
//                })
//            } catch (e: Exception) {
//                runOnUiThread {
//                    Toast.makeText(
//                        this@Register3Activity,
//                        "Error: ${e.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.e("API_EXCEPTION", e.toString())
//                }
//            }
//
//        }
        back_arrow.setOnClickListener {
            startActivity(
                Intent(this, Register2Activity::class.java)
            )
        }
    }
}