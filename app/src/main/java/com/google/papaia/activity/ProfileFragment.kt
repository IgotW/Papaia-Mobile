package com.google.papaia.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
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

        // Here you can safely find your button:
//        profilePic = view.findViewById(R.id.imageview_profile)
//        fullname = view.findViewById(R.id.txtview_fullname)
//        farmerId = view.findViewById(R.id.txtview_farmerid)
//        username = view.findViewById(R.id.txtview_username)
//        birthdate = view.findViewById(R.id.txtview_birthdate)
//        email = view.findViewById(R.id.txtview_email)
//        contactNumber = view.findViewById(R.id.txtview_contact)
//        farmName = view.findViewById(R.id.txtview_farmName)
//        farmLocation = view.findViewById(R.id.txtview_farmLocation)
//        settings = view.findViewById(R.id.card_settings)
//        val button_editprofile = view.findViewById<TextView>(R.id.button_editprofile)
////        val button_settings = view.findViewById<Button>(R.id.button_settings)
////        val button_changepass = view.findViewById<Button>(R.id.button_changepassword)
//        val button_logout = view.findViewById<Button>(R.id.button_logout)
//        val button_about = view.findViewById<TextView>(R.id.aboutPapalaText)
//
//        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
//        val username = prefs.getString("username", "User")
//        val firstname = prefs.getString("firstname", "User")
//        val middlename = prefs.getString("middlename", "User")
//        val lastname = prefs.getString("lastname", "User")
//        val suffix = prefs.getString("suffix", "User")
//        val id = prefs.getString("id", "User")
//
//        fullname.setText("${firstname} ${middlename} ${lastname} ${suffix}")
//        farmerId.setText("${id}")

        // Bind views
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

        // Example: set a click listener
        button_logout.setOnClickListener {

            showLogoutDialog()

//            // 1. Clear SharedPreferences
//            prefs.edit().clear().apply()
//
//            // 2. Clear SecurePrefsHelper (the saved JWT token)
//            SecurePrefsHelper.clearToken(requireContext())
//
//            // 3. Finish all activities
//            requireActivity().finishAffinity()
//
//            // 4. Start LoginActivity
//            val intent = Intent(requireContext(), LoginActivity::class.java)
//            startActivity(intent)

//            AlertDialog.Builder(requireContext())
//                .setTitle("Logout")
//                .setMessage("Are you sure you want to logout?")
//                .setPositiveButton("Yes") { _, _ ->
//                    // Handle logout logic
//                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
//                    // 1. Clear SharedPreferences
//                    prefs.edit().clear().apply()
//
//                    // 2. Clear SecurePrefsHelper (the saved JWT token)
//                    SecurePrefsHelper.clearToken(requireContext())
//
//                    // 3. Finish all activities
//                    requireActivity().finishAffinity()
//
//                    // 4. Start LoginActivity
//                    val intent = Intent(requireContext(), LoginActivity::class.java)
//                    startActivity(intent)
//                }
//                .setNegativeButton("Cancel", null)
//                .show()
            
//            val dialog = AlertDialog.Builder(requireContext())
//                .setTitle("Logout")
//                .setMessage("Are you sure you want to logout?")
//                .setPositiveButton("Yes") { _, _ ->
//                    // Handle logout logic
//                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
//                    // 1. Clear SharedPreferences
//                    prefs.edit().clear().apply()
//
//                    // 2. Clear SecurePrefsHelper (the saved JWT token)
//                    SecurePrefsHelper.clearToken(requireContext())
//
//                    // 3. Finish all activities
//                    requireActivity().finishAffinity()
//
//                    // 4. Start LoginActivity
//                    val intent = Intent(requireContext(), LoginActivity::class.java)
//                    startActivity(intent)
//                }
//                .setNegativeButton("Cancel", null)
//                .show()
//
//                // Change background color to white
//                dialog.window?.setBackgroundDrawableResource(android.R.color.white)

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
//        button_changepass.setOnClickListener {
//            startActivity(
//                Intent(requireContext(), ChangePasswordActivity::class.java)
//            )
//        }
//        button_settings.setOnClickListener {
//            startActivity(
//                Intent(requireContext(), SettingsActivity::class.java)
//            )
//        }
    }
    override fun onResume() {
        super.onResume()

        loadUserProfile()
        loadFarmDetails()

//        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
//        val token = prefs.getString("token", "")
//        val profileImage = prefs.getString("profileImage", "")
//        val userName = prefs.getString("username", "")
//        val emailAdd = prefs.getString("email", "")
//        val firstname = prefs.getString("firstname", "")
//        val middlename = prefs.getString("middlename", "")
//        val lastname = prefs.getString("lastname", "")
//        val suffix = prefs.getString("suffix", "")
//        val birthDate = prefs.getString("birthdate", "")
//        val contact = prefs.getString("contactNumber", "")
//        val street = prefs.getString("street", "")
//        val barangay = prefs.getString("barangay", "")
//        val municipality = prefs.getString("municipality", "")
//        val province = prefs.getString("province", "")
//        val zipCode = prefs.getString("zipcode", "")
//        val idNum = prefs.getString("idNumber", "")
//
//        fullname?.text = listOfNotNull(firstname, middlename, lastname, suffix)
//            .filter { it.isNotBlank() }
//            .joinToString(" ")
//
//        farmerId?.text = idNum
//        username?.text = userName
//        email?.text = emailAdd
//        birthdate?.text = birthDate
//        contactNumber?.text = contact
//
//        if (!profileImage.isNullOrEmpty()) {
//            Glide.with(this)
//                .load(profileImage)
//                .placeholder(R.drawable.ic_person) // shown while loading
//                .error(R.drawable.ic_person) // fallback if loading fails
//                .circleCrop()
//                .into(profilePic)
//        } else {
//            // keep the default icon
//            profilePic.setImageResource(R.drawable.ic_person)
//        }
//
//        if (token != null) {
//            RetrofitClient.instance.getFarmDetails("Bearer $token")
//                .enqueue(object : Callback<FarmDetailsResponse> {
//                    override fun onResponse(
//                        call: Call<FarmDetailsResponse>,
//                        response: Response<FarmDetailsResponse>
//                    ) {
//                        if (response.isSuccessful) {
//                            val farm = response.body()
//                            if (farm != null) {
//                                // Show data in TextViews
//                                farmName.text = farm.farmName
//                                farmLocation.text = farm.farmLocation
//                            }
//                        } else {
//                            Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<FarmDetailsResponse>, t: Throwable) {
//                        Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
//                    }
//                })
//        }
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
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
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
//                    if (response.isSuccessful) {
//                        response.body()?.let { farm ->
//                            farmName.text = farm.farmName
//                            farmLocation.text = farm.farmLocation
//                        }
//                    } else {
//                        Toast.makeText(
//                            context,
//                            "Error: ${response.code()}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
                    if (!isAdded) return
                    response.body()?.let { farm ->
                        view?.post {
                            farmName.text = farm.farmName
                            farmLocation.text = farm.farmLocation
                        }
                    }
                }

                override fun onFailure(call: Call<FarmDetailsResponse>, t: Throwable) {
                    Toast.makeText(context, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun showLogoutDialog() {
        // ✅ Custom title TextView (black text, left aligned)
        val titleView = TextView(requireContext()).apply {
            text = "Logout"
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            textSize = 20f
            setPadding(50, 40, 50, 20)
            gravity = Gravity.START   // keep left aligned
            setTypeface(null, Typeface.BOLD) // make bold for visibility
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setCustomTitle(titleView) // 👈 use custom title
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.white)

            // Center + color the message
            dialog.findViewById<TextView>(android.R.id.message)?.apply {
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                gravity = Gravity.CENTER
                textSize = 16f
            }

            // Buttons
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            )

        }

        dialog.show()
    }

//    private fun showLogoutDialog() {
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle("Logout")
//            .setMessage("Are you sure you want to logout?")
//            .setPositiveButton("Yes") { _, _ ->
//                performLogout()
//            }
//            .setNegativeButton("Cancel", null)
//            .create()
//
//        dialog.setOnShowListener {
//            // Change background to white
//            dialog.window?.setBackgroundDrawableResource(android.R.color.white)
//
//            // Change title text color
//            val titleId = dialog.context.resources.getIdentifier("alertTitle", "id", "android")
//            val titleView = dialog.findViewById<TextView>(titleId)
//            titleView?.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
//
//            // Change message text color + center
//            dialog.findViewById<TextView>(android.R.id.message)?.apply {
//                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
//                gravity = Gravity.CENTER   // ✅ Center text
//            }
//
//            // Optionally change button text colors
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
//                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
//            )
//            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
//                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
//            )
//
//            // ✅ Adjust dialog width & height
//            dialog.window?.setLayout(
//                (resources.displayMetrics.widthPixels * 0.75).toInt(), // 75% of screen width
//                WindowManager.LayoutParams.WRAP_CONTENT                // height auto adjusts
//            )
//        }
//
//        dialog.show()
//    }

    private fun performLogout() {
        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        prefs.edit().clear().apply()
        SecurePrefsHelper.clearToken(requireContext())

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