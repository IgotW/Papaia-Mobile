package com.google.papaia.activity

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import com.google.papaia.R
import com.google.papaia.response.FarmDetailsResponse
import com.google.papaia.utils.RetrofitClient
import com.google.papaia.utils.SecurePrefsHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var profilePic: ImageView
    private lateinit var fullname: TextView
    private lateinit var farmerId: TextView
    private lateinit var username: TextView
    private lateinit var birthdate: TextView
    private lateinit var email: TextView
    private lateinit var contactNumber: TextView
    private lateinit var farmName: TextView
    private lateinit var farmLocation: TextView
    private lateinit var settings: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profilePic = view.findViewById(R.id.imageview_profile)
        fullname = view.findViewById(R.id.txtview_fullname)
        farmerId = view.findViewById(R.id.txtview_farmerid)
        username = view.findViewById(R.id.txtview_username)
        birthdate = view.findViewById(R.id.txtview_birthdate)
        email = view.findViewById(R.id.txtview_email)
        contactNumber = view.findViewById(R.id.txtview_contact)
        farmName = view.findViewById(R.id.txtview_farmName)
        farmLocation = view.findViewById(R.id.txtview_farmLocation)
        settings = view.findViewById(R.id.card_settings)

        val button_editprofile = view.findViewById<TextView>(R.id.button_editprofile)
        val button_logout = view.findViewById<Button>(R.id.button_logout)
        val button_about = view.findViewById<TextView>(R.id.aboutPapalaText)

        button_logout.setOnClickListener {
            showLogoutDialog()
        }
        button_editprofile.setOnClickListener {
            startActivity(
                Intent(requireContext(), EditProfileActivity::class.java)
            )
        }
        button_about.setOnClickListener {
            startActivity(
                Intent(requireContext(), AboutActivity::class.java)
            )
        }
        settings.setOnClickListener {
            startActivity(
                Intent(requireContext(), SettingsActivity::class.java)
            )
        }
    }
    override fun onResume() {
        super.onResume()

        loadUserProfile()
        loadFarmDetails()

        val prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val url = prefs.getString("profileImageUrl", null)
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_profile)
                .circleCrop()
                .into(profilePic)
        }
    }

    private fun loadUserProfile() {
        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)

        val firstName = prefs.getString("firstname", "").orEmpty()
        val middleName = prefs.getString("middlename", "").orEmpty()
        val lastName = prefs.getString("lastname", "").orEmpty()
        val suffix = prefs.getString("suffix", "").orEmpty()
        val idNum = prefs.getString("idNumber", "").orEmpty()

        fullname.text = listOf(firstName, middleName, lastName, suffix)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        farmerId.text = idNum
        username.text = prefs.getString("username", "")
        email.text = prefs.getString("email", "")
        birthdate.text = prefs.getString("birthdate", "")
        contactNumber.text = prefs.getString("contactNumber", "")

        val profileImage = prefs.getString("profileImage", "")

        if (!profileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileImage)
                .placeholder(R.drawable.ic_person) // shown while loading
                .error(R.drawable.ic_person)       // fallback if URL fails
                .circleCrop()                      // round profile
                .into(profilePic)
        } else {
            profilePic.setImageResource(R.drawable.ic_person)
        }
    }

    private fun loadFarmDetails() {
        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: return

        RetrofitClient.instance.getFarmDetails("Bearer $token")
            .enqueue(object : Callback<FarmDetailsResponse> {
                override fun onResponse(
                    call: Call<FarmDetailsResponse>,
                    response: Response<FarmDetailsResponse>
                ) {
                    if (!isAdded) return
                    response.body()?.let { farm ->
                        view?.post {
                            farmName.text = farm.farmName
                            farmLocation.text = farm.farmLocation
                        }
                    }
                }

                override fun onFailure(call: Call<FarmDetailsResponse>, t: Throwable) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("API_ERROR", "Failure: ${t.message}", t)
                }
            })
    }
//    private fun showLogoutDialog() {
//        // ✅ Custom title TextView (black text, left aligned)
//        val titleView = TextView(requireContext()).apply {
//            text = "Logout"
//            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
//            textSize = 20f
//            setPadding(50, 40, 50, 20)
//            gravity = Gravity.START   // keep left aligned
//            setTypeface(null, Typeface.BOLD) // make bold for visibility
//        }
//
//        val dialog = AlertDialog.Builder(requireContext())
//            .setCustomTitle(titleView) // 👈 use custom title
//            .setMessage("Are you sure you want to logout?")
//            .setPositiveButton("Yes") { _, _ ->
//                performLogout()
//            }
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.setOnShowListener {
//            dialog.window?.setBackgroundDrawableResource(android.R.color.white)
//
//            // Center + color the message
//            dialog.findViewById<TextView>(android.R.id.message)?.apply {
//                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
//                gravity = Gravity.CENTER
//                textSize = 16f
//            }
//
//            // Buttons
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
//                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
//            )
//            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
//                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
//            )
//
//        }
//
//        dialog.show()
//    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout, null)

        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_cancel)
        val btnConfirm = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_confirm)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Make background transparent for rounded corners to show
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    private fun performLogout() {
        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        prefs.edit().clear().apply()
        SecurePrefsHelper.clearToken(requireContext())

        //for testing
        // 🔥 Delete FCM token so it stops receiving push notifications
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Logout", "FCM token deleted successfully")
                } else {
                    Log.e("Logout", "Failed to delete FCM token", task.exception)
                }
            }

        requireActivity().finishAffinity()
        startActivity(Intent(requireContext(), LoginActivity::class.java))

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}