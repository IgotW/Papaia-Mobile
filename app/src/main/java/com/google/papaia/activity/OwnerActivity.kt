package com.google.papaia.activity


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.utils.SecurePrefsHelper

class OwnerActivity : AppCompatActivity() {

    companion object {
        private const val OWNER_WEBSITE_URL = "https://papaia-web.vercel.app/"
    }

    private lateinit var buttonOpenWebsite: MaterialButton
    private lateinit var buttonCopyUrl: MaterialButton
    private lateinit var buttonOwnerBack: MaterialButton
    private lateinit var imageviewCopy: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_owner)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        buttonOpenWebsite = findViewById(R.id.button_open_website)
        buttonCopyUrl = findViewById(R.id.button_copy_url)
        buttonOwnerBack = findViewById(R.id.button_owner_back)
        imageviewCopy = findViewById(R.id.imageview_copy)
    }

    private fun setupClickListeners() {
        // Open website in browser
        buttonOpenWebsite.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OWNER_WEBSITE_URL))
                startActivity(intent)
                showToast("Opening owner portal...")
            } catch (e: Exception) {
                showToast("Unable to open browser. Please check if you have a browser installed.")
            }
        }

        // Copy URL to clipboard
        buttonCopyUrl.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Owner Portal URL", OWNER_WEBSITE_URL)
            clipboard.setPrimaryClip(clip)
            showToast("URL copied to clipboard!")
        }

        imageviewCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Owner Portal URL", OWNER_WEBSITE_URL)
            clipboard.setPrimaryClip(clip)
            showToast("URL copied to clipboard!")
        }

        // Back to previous screen
        buttonOwnerBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}