package com.google.papaia.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.papaia.R
import com.google.papaia.request.RegisterRequest
import com.google.papaia.response.RegisterResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Register1Activity : AppCompatActivity() {
    private lateinit var edittext_username: TextInputEditText
    private lateinit var edittext_email: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var edittext_password: TextInputEditText
    private lateinit var tilConfirmPass: TextInputLayout
    private lateinit var edittext_confirmPassword: TextInputEditText
    private lateinit var edittext_firstname: TextInputEditText
    private lateinit var edittext_middlename: TextInputEditText
    private lateinit var edittext_lastname: TextInputEditText
    private lateinit var edittext_suffix: AutoCompleteTextView
    private lateinit var edittext_birthdate: TextInputEditText
    private lateinit var edittext_contactnumber: TextInputEditText
    private lateinit var edittext_street: TextInputEditText
    private lateinit var edittext_barangay: TextInputEditText
    private lateinit var edittext_municipality: TextInputEditText
    private lateinit var edittext_province: AutoCompleteTextView
    private lateinit var edittext_zipCode: TextInputEditText
    private lateinit var cbTerms: CheckBox
    private lateinit var button_signup: Button
    private lateinit var button_back: ImageView
    private lateinit var passwordStrengthBar: View

    // Role variable
    private lateinit var role: String

    // Date formatter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register1)

        // TextInputLayouts
        edittext_username = findViewById(R.id.reg_edittext_username)
        edittext_email = findViewById(R.id.reg_edittext_email)
        tilPassword = findViewById(R.id.til_password)
        edittext_password = findViewById(R.id.reg_edittext_password)
        tilConfirmPass = findViewById(R.id.til_confirm_password)
        edittext_confirmPassword = findViewById(R.id.reg_edittext_confirm_password)
        edittext_firstname = findViewById(R.id.reg_edittext_firstname)
        edittext_middlename = findViewById(R.id.reg_edittext_middlename)
        edittext_lastname = findViewById(R.id.reg_edittext_lastname)
        edittext_birthdate = findViewById(R.id.reg_edittext_birthdate)
        edittext_contactnumber = findViewById(R.id.reg_edittext_contactnumber)
        edittext_street = findViewById(R.id.reg_edittext_street)
        edittext_barangay = findViewById(R.id.reg_edittext_barangay)
        edittext_municipality = findViewById(R.id.reg_edittext_municipality)
        edittext_zipCode = findViewById(R.id.reg_edittext_zipcode)

        val login = findViewById<TextView>(R.id.tvLogin)

        // AutoCompleteTextViews
        edittext_suffix = findViewById(R.id.reg_suffix)
        edittext_province = findViewById(R.id.reg_province)

        // Checkbox and Button
        cbTerms = findViewById(R.id.cbTerms)
        button_signup = findViewById(R.id.button_signup)
        button_back = findViewById(R.id.btn_back)
        passwordStrengthBar = findViewById(R.id.password_strength_bar)

        setupDropdowns()
        setupDatePicker()
        setupPasswordValidation()

        // Initialize role from intent
        role = intent?.getStringExtra("role") ?: ""

        button_signup.setOnClickListener {
            Log.d("REGISTER_DEBUG", "Sign up button clicked")
            if (validateForm()) {
                Log.d("REGISTER_DEBUG", "Form validated successfully")
                registerUser()
            } else {
                Log.d("REGISTER_DEBUG", "Form validation failed")
            }
        }
        login.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }
        button_back.setOnClickListener {
            startActivity(
                Intent(this, Register0Activity::class.java)
            )
        }
    }

    private fun setupDatePicker() {
        edittext_birthdate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)

                    // Set display text (user-friendly format)
                    edittext_birthdate.setText(displayDateFormat.format(selectedDate.time))

                    // Store the date in API format (yyyy-MM-dd) as tag
                    edittext_birthdate.tag = dateFormat.format(selectedDate.time)
                },
                year, month, day
            )

            // Set maximum date to today (can't select future dates)
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            // Set minimum date to 100 years ago (reasonable limit)
            calendar.add(Calendar.YEAR, -100)
            datePickerDialog.datePicker.minDate = calendar.timeInMillis

            datePickerDialog.show()
        }
    }

    private fun setupDropdowns() {
        // Suffix dropdown
        val suffixOptions = arrayOf("Jr.", "Sr.", "II", "III", "IV", "V")
        val suffixAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suffixOptions)
        edittext_suffix.setAdapter(suffixAdapter)

        // Philippines provinces dropdown
        val provinces = arrayOf(
            "Abra", "Agusan del Norte", "Agusan del Sur", "Aklan", "Albay", "Antique",
            "Apayao", "Aurora", "Basilan", "Bataan", "Batanes", "Batangas", "Benguet",
            "Biliran", "Bohol", "Bukidnon", "Bulacan", "Cagayan", "Camarines Norte",
            "Camarines Sur", "Camiguin", "Capiz", "Catanduanes", "Cavite", "Cebu",
            "Compostela Valley", "Cotabato", "Davao del Norte", "Davao del Sur",
            "Davao Oriental", "Dinagat Islands", "Eastern Samar", "Guimaras", "Ifugao",
            "Ilocos Norte", "Ilocos Sur", "Iloilo", "Isabela", "Kalinga", "Laguna",
            "Lanao del Norte", "Lanao del Sur", "La Union", "Leyte", "Maguindanao",
            "Marinduque", "Masbate", "Metro Manila", "Misamis Occidental", "Misamis Oriental",
            "Mountain Province", "Negros Occidental", "Negros Oriental", "Northern Samar",
            "Nueva Ecija", "Nueva Vizcaya", "Occidental Mindoro", "Oriental Mindoro",
            "Palawan", "Pampanga", "Pangasinan", "Quezon", "Quirino", "Rizal", "Romblon",
            "Samar", "Sarangani", "Siquijor", "Sorsogon", "South Cotabato", "Southern Leyte",
            "Sultan Kudarat", "Sulu", "Surigao del Norte", "Surigao del Sur", "Tarlac",
            "Tawi-Tawi", "Zambales", "Zamboanga del Norte", "Zamboanga del Sur",
            "Zamboanga Sibugay"
        )
        val provinceAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, provinces)
        edittext_province.setAdapter(provinceAdapter)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Clear previous errors
        edittext_username.error = null
        edittext_email.error = null
        edittext_password.error = null
        edittext_confirmPassword.error = null
        edittext_firstname.error = null
        edittext_lastname.error = null
        edittext_birthdate.error = null
        edittext_contactnumber.error = null
        edittext_street.error = null
        edittext_barangay.error = null
        edittext_municipality.error = null
        edittext_zipCode.error = null
        edittext_province.error = null

        // Username validation
        val usernameText = edittext_username.text.toString().trim()
        if (usernameText.isEmpty()) {
            edittext_username.error = "Username is required"
            isValid = false
        } else if (usernameText.length < 3) {
            edittext_username.error = "Username must be at least 3 characters"
            isValid = false
        }

        // Email validation
        val emailText = edittext_email.text.toString().trim()
        if (emailText.isEmpty()) {
            edittext_email.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            edittext_email.error = "Please enter a valid email address"
            isValid = false
        }

        // Password validation
        val passwordText = edittext_password.text.toString()
        if (passwordText.isEmpty()) {
            edittext_password.error = "Password is required"
            isValid = false
        } else if (passwordText.length < 8) {
            edittext_password.error = "Password must be at least 8 characters"
            isValid = false
        }

        // Confirm password validation
        val confirmPasswordText = edittext_confirmPassword.text.toString()
        if (confirmPasswordText.isEmpty()) {
            edittext_confirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (passwordText != confirmPasswordText) {
            edittext_confirmPassword.error = "Passwords do not match"
            isValid = false
        }

        // First name validation
        if (edittext_firstname.text.toString().trim().isEmpty()) {
            edittext_firstname.error = "First name is required"
            isValid = false
        }

        // Last name validation
        if (edittext_lastname.text.toString().trim().isEmpty()) {
            edittext_lastname.error = "Last name is required"
            isValid = false
        }

        // Birth date validation
        if (edittext_birthdate.text.toString().trim().isEmpty()) {
            edittext_birthdate.error = "Birth date is required"
            isValid = false
        }

        // Contact number validation
        val contactNumberText = edittext_contactnumber.text.toString().trim()
        if (contactNumberText.isEmpty()) {
            edittext_contactnumber.error = "Contact number is required"
            isValid = false
        } else if (contactNumberText.length < 10) {
            edittext_contactnumber.error = "Contact number must be at least 10 digits"
            isValid = false
        } else if (!contactNumberText.matches(Regex("^[+]?[0-9\\-\\s()]*$"))) {
            edittext_contactnumber.error = "Please enter a valid phone number"
            isValid = false
        }

        // Street address validation
        if (edittext_street.text.toString().trim().isEmpty()) {
            edittext_street.error = "Street address is required"
            isValid = false
        }

        // Barangay validation
        if (edittext_barangay.text.toString().trim().isEmpty()) {
            edittext_barangay.error = "Barangay is required"
            isValid = false
        }

        // Municipality validation
        if (edittext_municipality.text.toString().trim().isEmpty()) {
            edittext_municipality.error = "City/Municipality is required"
            isValid = false
        }

        // ZIP code validation
        val zipCodeText = edittext_zipCode.text.toString().trim()
        if (zipCodeText.isEmpty()) {
            edittext_zipCode.error = "ZIP code is required"
            isValid = false
        } else if (zipCodeText.length != 4) {
            edittext_zipCode.error = "ZIP code must be 4 digits"
            isValid = false
        }

        // Province validation
        if (edittext_province.text.toString().trim().isEmpty()) {
            edittext_province.error = "Province is required"
            isValid = false
        }

        // Terms and conditions validation
        if (!cbTerms.isChecked) {
            Toast.makeText(this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun registerUser(){
        val username = edittext_username.text.toString().trim()
        val email = edittext_email.text.toString().trim()
        val password = edittext_password.text.toString()
        val role = role
        val firstName = edittext_firstname.text.toString().trim()
        val middleName = edittext_middlename.text.toString().trim()
        val lastName = edittext_lastname.text.toString().trim()
        val suffix = edittext_suffix.text.toString().trim()
        val birthDate = edittext_birthdate.tag?.toString() ?: "" // Get the API format date from tag
        val contactNumber = edittext_contactnumber.text.toString().trim()
        val street = edittext_street.text.toString().trim()
        val barangay = edittext_barangay.text.toString().trim()
        val municipality = edittext_municipality.text.toString().trim()
        val province = edittext_province.text.toString().trim()
        val zipCode = edittext_zipCode.text.toString().trim()

        // Show loading
        button_signup.isEnabled = false
        button_signup.text = "Creating Account..."

        try {
            val register = RegisterRequest(
                username, email, password, role, firstName,
                if (middleName.isEmpty()) null else middleName,
                lastName,
                if (suffix.isEmpty()) null else suffix,
                birthDate, contactNumber, street, barangay, municipality, province, zipCode
            )

            RetrofitClient.instance.registerUser(register).enqueue(object :
                Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    // Reset button
                    button_signup.isEnabled = true
                    button_signup.text = "Sign Up"

                    if (response.isSuccessful && response.body() != null) {
                        // ✅ Registration successful
                        runOnUiThread {
                            Toast.makeText(
                                this@Register1Activity,
                                "Registered Successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("API_SUCCESS", response.body().toString())

                            startActivity(
                                Intent(this@Register1Activity, LoginActivity::class.java)
                            )
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@Register1Activity,
                                "Registration Failed: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("API_ERROR", response.errorBody()?.string().toString())
                        }
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    // Reset button
                    button_signup.isEnabled = true
                    button_signup.text = "Sign Up"

                    // Handle failure
                    runOnUiThread {
                        Toast.makeText(
                            this@Register1Activity,
                            "Network Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("API_EXCEPTION", t.toString())
                    }
                }
            })
        } catch (e: Exception) {
            // Reset button
            button_signup.isEnabled = true
            button_signup.text = "Sign Up"

            runOnUiThread {
                Toast.makeText(
                    this@Register1Activity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("API_EXCEPTION", e.toString())
            }
        }
    }

    private fun setupPasswordValidation() {
        // Password strength validation
        edittext_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                updatePasswordStrength(password)

                // Clear confirm password error if passwords now match
                val confirmPassword = edittext_confirmPassword.text.toString()
                if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                    tilConfirmPass.error = null
                }
            }
        })

        // Confirm password validation
        edittext_confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = edittext_password.text.toString()

                if (confirmPassword.isNotEmpty() && password.isNotEmpty()) {
                    if (password != confirmPassword) {
                        tilConfirmPass.error = "Passwords do not match"
                    } else {
                        tilConfirmPass.error = null
                    }
                }
            }
        })
    }

    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            passwordStrengthBar.visibility = View.INVISIBLE
            return
        }

        passwordStrengthBar.visibility = View.VISIBLE

        val strength = calculatePasswordStrength(password)
        val strengthDrawable = when (strength) {
            PasswordStrength.WEAK -> R.drawable.password_strength_weak
            PasswordStrength.MEDIUM -> R.drawable.password_strength_medium
            PasswordStrength.STRONG -> R.drawable.password_strength_strong
        }

        passwordStrengthBar.background = ContextCompat.getDrawable(this, strengthDrawable)
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0

        // Length check
        if (password.length >= 8) score++
        if (password.length >= 12) score++

        // Character variety checks
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score < 3 -> PasswordStrength.WEAK
            score < 5 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    private fun validateInputs(password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Clear previous errors
        tilPassword.error = null
        tilConfirmPass.error = null

        // Check if fields are empty
        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            tilPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPass.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPass.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}