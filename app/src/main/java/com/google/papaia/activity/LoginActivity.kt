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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.messaging.FirebaseMessaging
import com.google.papaia.MyApp
import com.google.papaia.R
import com.google.papaia.model.User
import com.google.papaia.request.LoginRequest
import com.google.papaia.request.UpdateFcmRequest
import com.google.papaia.response.ApiResponse
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


//    private fun handleLoginSuccess(loginResponse: LoginResponse) {
//        val token = loginResponse.token ?: ""
//        val user = loginResponse.user
//
//        // Save token securely
//        SecurePrefsHelper.saveToken(this, token)
//
//        // Save other user info in prefs
//        getSharedPreferences("prefs", MODE_PRIVATE).edit().apply {
//            putString("token", token)
//            putString("role", user?.role)
//            putString("id", user?.id)
//            putString("idNumber", user?.idNumber)
//            putString("profileImage", user?.profilePicture)
//            putString("email", user?.email)
//            putString("username", user?.username)
//            putString("firstname", user?.firstName)
//            putString("middlename", user?.middleName)
//            putString("lastname", user?.lastName)
//            putString("birthdate", user?.birthDate)
//            putString("contactNumber", user?.contactNumber)
//            putString("suffix", user?.suffix)
//            putString("street", user?.street)
//            putString("barangay", user?.barangay)
//            putString("municipality", user?.municipality)
//            putString("province", user?.province)
//            putString("zipcode", user?.zipCode)
//            apply()
//        }
//
//        // ✅ Send FCM token + location to backend
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("FCM", "Fetching FCM token failed", task.exception)
//                return@addOnCompleteListener
//            }
//
//            val fcmToken = task.result ?: return@addOnCompleteListener
//            Log.d("FCM", "Device FCM token: $fcmToken")
//
//            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
//            val lat = prefs.getFloat("last_lat", 0f).takeIf { it != 0f }?.toDouble()
//            val lon = prefs.getFloat("last_lon", 0f).takeIf { it != 0f }?.toDouble()
//
//            val request = UpdateFcmRequest(user.id, fcmToken, lat ?: 0.0, lon ?: 0.0)
//            if (token.isNotEmpty()) {
//                RetrofitClient.instance.updateFcmToken("Bearer $token", request)
//                    .enqueue(object : Callback<FcmResponse> {
//                        override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
//                            if (response.isSuccessful) {
//                                Log.d("FCM", "updateFcmToken success code=${response.code()}")
//                            } else {
//                                Log.e("FCM", "Server error: ${response.code()} - ${response.message()}")
//                            }
//                        }
//
//                        override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
//                            Log.e("FCM", "updateFcmToken failed: ${t.message}")
//                        }
//                    })
//            } else if (!user?.id.isNullOrEmpty()) {
//                // fallback: no auth header
//                RetrofitClient.instance.updateFcmTokenNoAuth(request)
//                    .enqueue(object : Callback<Void> {
//                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                            Log.d("FCM", "updateFcmTokenNoAuth success code=${response.code()}")
//                        }
//
//                        override fun onFailure(call: Call<Void>, t: Throwable) {
//                            Log.e("FCM", "updateFcmTokenNoAuth failed: ${t.message}")
//                        }
//                    })
//            }
//        }
//
//        // ✅ Schedule daily tip worker after login
////        MyApp.scheduleDailyTipWorker(this)
//
//        // Navigate to proper dashboard
//        if (user?.role == "farmer") {
//            startActivity(Intent(this, DashboardActivity::class.java))
//            Toast.makeText(this, "Welcome ${user?.username}", Toast.LENGTH_SHORT).show()
//            finish()
//        } else {
//            // Not a farmer → reject login
//            Toast.makeText(this, "Access denied. Only farmers can log in.", Toast.LENGTH_LONG).show()
//
//            // Optional: clear stored token and prefs so they can’t auto-login
//            SecurePrefsHelper.clearToken(this)
//            getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()
//
//            val intent = Intent(this, OwnerActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }

    private fun handleLoginSuccess(loginResponse: LoginResponse) {
        val token = loginResponse.token ?: ""
        val user = loginResponse.user

        if (user == null) {
            Toast.makeText(this, "Invalid user data.", Toast.LENGTH_SHORT).show()
            return
        }

        // ⚠️ Check account status before proceeding
        if (user.status == "deactivate") {
            showReactivateDialog(token)
            return
        }

        // ✅ Continue normal login process
        completeLoginProcess(user, token)
    }

    private fun showReactivateDialog(token: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reactivate, null)
        val btnYes = dialogView.findViewById<MaterialButton>(R.id.button_yes)
        val btnNo = dialogView.findViewById<MaterialButton>(R.id.button_no)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnNo.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Login canceled.", Toast.LENGTH_SHORT).show()
        }

        btnYes.setOnClickListener {
            btnYes.isEnabled = false
            reactivateAccount(token, dialog)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }

    private fun reactivateAccount(token: String, dialog: AlertDialog) {
        RetrofitClient.instance.reactivateAccount("Bearer $token")
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@LoginActivity,
                            response.body()?.message ?: "Account reactivated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        attemptLogin() // retry login automatically
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Failed to reactivate account.",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            })
    }

    private fun completeLoginProcess(user: User,token: String) {
        SecurePrefsHelper.saveToken(this, token)

//        getSharedPreferences("prefs", MODE_PRIVATE).edit().apply {
//            putString("token", token)
//            putString("role", user.role)
//            putString("id", user.id)
//            putString("username", user.username)
//            putString("email", user.email)
//            putString("status", user.status)
//            apply()
//        }

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

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result ?: return@addOnCompleteListener
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            val lat = prefs.getFloat("last_lat", 0f).takeIf { it != 0f }?.toDouble()
            val lon = prefs.getFloat("last_lon", 0f).takeIf { it != 0f }?.toDouble()

            val request = UpdateFcmRequest(user.id, fcmToken, lat ?: 0.0, lon ?: 0.0)
            RetrofitClient.instance.updateFcmToken("Bearer $token", request)
                .enqueue(object : Callback<FcmResponse> {
                    override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
                        Log.d("FCM", "FCM updated: ${response.code()}")
                    }

                    override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
                        Log.e("FCM", "update failed: ${t.message}")
                    }
                })
        }

        if (user.role == "farmer") {
            startActivity(Intent(this, DashboardActivity::class.java))
            Toast.makeText(this, "Welcome ${user.username}", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Access denied. Only farmers can log in.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, OwnerActivity::class.java))
            SecurePrefsHelper.clearToken(this)
            getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()
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
