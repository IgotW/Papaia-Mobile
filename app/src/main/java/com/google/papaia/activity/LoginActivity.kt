package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.messaging.FirebaseMessaging
import com.google.papaia.MyApp
import com.google.papaia.R
import com.google.papaia.request.LoginRequest
import com.google.papaia.request.UpdateFcmRequest
import com.google.papaia.response.FcmResponse
import com.google.papaia.response.LoginResponse
import com.google.papaia.utils.RetrofitClient
import com.google.papaia.utils.SecurePrefsHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerLink: TextView
    private lateinit var forgotPasswordLink: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loginIcon: ImageView

    private var loginSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Auto-login if token exists
        val token = SecurePrefsHelper.getToken(this)
        if (!token.isNullOrEmpty()) {
            goToDashboard()
            return
        }

        setContentView(R.layout.activity_login)
        setupInsets()
        initViews()
        setupListeners()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        usernameField = findViewById(R.id.login_edittext_username)
        passwordField = findViewById(R.id.login_edittext_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.txtview_register)
        forgotPasswordLink = findViewById(R.id.txtview_login_forgotpass)
        progressBar = findViewById(R.id.login_progress_bar)
        loginIcon = findViewById(R.id.login_icon)

    }

    private fun setupListeners() {
        loginButton.setOnClickListener { attemptLogin() }
        registerLink.setOnClickListener {
            startActivity(Intent(this, Register0Activity::class.java))
        }
        forgotPasswordLink.setOnClickListener {
            startActivity(Intent(this, ForgotPassword1Activity::class.java))
        }
    }

    private fun attemptLogin() {
        val username = usernameField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        if (username.isEmpty()) {
            usernameField.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            passwordField.error = "Password is required"
            return
        }

        loginSuccess = false
        setLoading(true)

        val loginRequest = LoginRequest(username, password)
        RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                if (response.isSuccessful && response.body() != null) {
//                    handleLoginSuccess(response.body()!!)
                    loginSuccess = true
                    handleLoginSuccess(response.body()!!)
                } else {
//                    handleLoginError(response)
                    loginSuccess = false
                    handleLoginError(response)
                }
                setLoading(false)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("LOGIN_FAILURE", t.message ?: "Unknown error")
            }
        })
    }

//    private fun setLoading(isLoading: Boolean) {
//        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        loginButton.text = if (isLoading) "" else getString(R.string.login)
//        loginButton.isEnabled = !isLoading
//    }

//    private fun setLoading(isLoading: Boolean) {
//        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        loginButton.text = if (isLoading) "" else getString(R.string.login)
//        loginButton.isEnabled = !isLoading
//
//        if (isLoading) {
//            // Animate icon from left → right
//            loginIcon.animate()
//                .translationXBy(150f) // move horizontally
//                .setDuration(700)
//                .withEndAction {
//                    loginIcon.animate().translationXBy(-150f).setDuration(700).start()
//                }
//                .start()
//        } else {
//            // Reset icon position
//            loginIcon.animate().cancel()
//            loginIcon.translationX = 0f
//        }
//    }

    private fun setLoading(isLoading: Boolean) {
//        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        loginButton.text = if (isLoading) "" else getString(R.string.login)
        loginButton.isEnabled = !isLoading
        loginButton.icon = null

        if (isLoading) {
            // Reset before animating
            loginIcon.visibility = View.VISIBLE
            loginIcon.alpha = 1f
            loginIcon.translationX = 0f
            loginIcon.animate().cancel()

            // Animate icon to slowly move right
            loginButton.post {
                val endX = loginButton.width - loginIcon.width - 60f
                val duration = 2500L // slow smooth slide
                loginButton.icon?.alpha = 255

                loginIcon.animate()
                    .translationX(endX)
                    .setDuration(duration)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction { loginButton.icon = null }
                    .start()
            }

        } else {
            if (loginSuccess) {
                // If success → fade out smoothly
                loginIcon.animate()
                    .alpha(0f)
                    .translationXBy(loginButton.width.toFloat() - 200f)
                    .setDuration(600L)
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction {
                        loginIcon.visibility = View.GONE
                        loginIcon.translationX = 0f
                    }
                    .start()
            } else {
                // If failed → move back to start
                loginButton.icon = getDrawable(R.drawable.ic_farm2)
                loginButton.iconTint = getColorStateList(R.color.white)

                loginIcon.animate()
                    .translationX(0f)
                    .setDuration(800L)
                    .setInterpolator(AccelerateInterpolator())
                    .start()
            }
        }
    }


    private fun handleLoginSuccess(loginResponse: LoginResponse) {
        val token = loginResponse.token ?: ""
        val user = loginResponse.user

        // Save token securely
        SecurePrefsHelper.saveToken(this, token)

        // Save other user info in prefs
        getSharedPreferences("prefs", MODE_PRIVATE).edit().apply {
            putString("token", token)
            putString("role", user?.role)
            putString("id", user?.id)
            putString("idNumber", user?.idNumber)
            putString("profileImage", user?.profilePicture)
            putString("email", user?.email)
            putString("username", user?.username)
            putString("firstname", user?.firstName)
            putString("middlename", user?.middleName)
            putString("lastname", user?.lastName)
            putString("birthdate", user?.birthDate)
            putString("contactNumber", user?.contactNumber)
            putString("suffix", user?.suffix)
            putString("street", user?.street)
            putString("barangay", user?.barangay)
            putString("municipality", user?.municipality)
            putString("province", user?.province)
            putString("zipcode", user?.zipCode)
            apply()
        }


        // ✅ Get FCM token and send to backend
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("FCM", "Fetching FCM token failed", task.exception)
//                return@addOnCompleteListener
//            }
//            val fcmToken = task.result
//            Log.d("FCM", "Device FCM token: $fcmToken")
//
//            getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("fcmToken", fcmToken).apply()
//
//            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
//            val latF = prefs.getFloat("last_lat", 0f)
//            val lonF = prefs.getFloat("last_lon", 0f)
//            val lat: Double? = if (latF != 0f) latF.toDouble() else null
//            val lon: Double? = if (lonF != 0f) lonF.toDouble() else null
//
//            val jwt = SecurePrefsHelper.getToken(this)
//            val userId = user?.id
//
//            val req = UpdateFcmRequest(fcmToken, lat, lon, userId)
//
//            if (!jwt.isNullOrEmpty()) {
//                RetrofitClient.instance.updateFcmToken("Bearer $jwt", req)
//                    .enqueue(object : Callback<Void> {
//                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                            Log.d("FCM", "updateFcmToken success code=${response.code()}")
//                        }
//                        override fun onFailure(call: Call<Void>, t: Throwable) {
//                            Log.e("FCM", "updateFcmToken failed: ${t.message}")
//                        }
//                    })
//            } else if (!userId.isNullOrEmpty()) {
//                // fallback: no auth header
//                RetrofitClient.instance.updateFcmTokenNoAuth(req)
//                    .enqueue(object : Callback<Void> {
//                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                            Log.d("FCM", "updateFcmTokenNoAuth success code=${response.code()}")
//                        }
//                        override fun onFailure(call: Call<Void>, t: Throwable) {
//                            Log.e("FCM", "updateFcmTokenNoAuth failed: ${t.message}")
//                        }
//                    })
//            }
//        }

        // ✅ Send FCM token + location to backend
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result ?: return@addOnCompleteListener
            Log.d("FCM", "Device FCM token: $fcmToken")

            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            val lat = prefs.getFloat("last_lat", 0f).takeIf { it != 0f }?.toDouble()
            val lon = prefs.getFloat("last_lon", 0f).takeIf { it != 0f }?.toDouble()

            val request = UpdateFcmRequest(user.id, fcmToken, lat ?: 0.0, lon ?: 0.0)
            if (token.isNotEmpty()) {
                RetrofitClient.instance.updateFcmToken("Bearer $token", request)
                    .enqueue(object : Callback<FcmResponse> {
                        override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
                            if (response.isSuccessful) {
                                Log.d("FCM", "updateFcmToken success code=${response.code()}")
                            } else {
                                Log.e("FCM", "Server error: ${response.code()} - ${response.message()}")
                            }
                        }

                        override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
                            Log.e("FCM", "updateFcmToken failed: ${t.message}")
                        }
                    })
            } else if (!user?.id.isNullOrEmpty()) {
                // fallback: no auth header
                RetrofitClient.instance.updateFcmTokenNoAuth(request)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Log.d("FCM", "updateFcmTokenNoAuth success code=${response.code()}")
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("FCM", "updateFcmTokenNoAuth failed: ${t.message}")
                        }
                    })
            }
        }

        // ✅ Schedule daily tip worker after login
//        MyApp.scheduleDailyTipWorker(this)

        // Navigate to proper dashboard
        if (user?.role == "farmer") {
            startActivity(Intent(this, DashboardActivity::class.java))
            Toast.makeText(this, "Welcome ${user?.username}", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Not a farmer → reject login
            Toast.makeText(this, "Access denied. Only farmers can log in.", Toast.LENGTH_LONG).show()

            // Optional: clear stored token and prefs so they can’t auto-login
            SecurePrefsHelper.clearToken(this)
            getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()

            val intent = Intent(this, OwnerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun handleLoginError(response: Response<LoginResponse>) {
        val errorJson = response.errorBody()?.string()
        val errorMessage = try {
            org.json.JSONObject(errorJson ?: "").getString("error")
        } catch (e: Exception) {
            "Login failed"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        Log.e("LOGIN_ERROR", errorMessage)
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
