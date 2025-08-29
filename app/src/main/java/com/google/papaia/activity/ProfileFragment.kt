package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        profilePic = view.findViewById(R.id.imageview_profile)
        fullname = view.findViewById(R.id.txtview_fullname)
        farmerId = view.findViewById(R.id.txtview_farmerid)
        username = view.findViewById(R.id.txtview_username)
        birthdate = view.findViewById(R.id.txtview_birthdate)
        email = view.findViewById(R.id.txtview_email)
        contactNumber = view.findViewById(R.id.txtview_contact)
        farmName = view.findViewById(R.id.txtview_farmName)
        farmLocation = view.findViewById(R.id.txtview_farmLocation)
        val button_editprofile = view.findViewById<TextView>(R.id.button_editprofile)
//        val button_settings = view.findViewById<Button>(R.id.button_settings)
//        val button_changepass = view.findViewById<Button>(R.id.button_changepassword)
        val button_logout = view.findViewById<Button>(R.id.button_logout)
        val button_about = view.findViewById<TextView>(R.id.aboutPapalaText)

        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        val username = prefs.getString("username", "User")
        val firstname = prefs.getString("firstname", "User")
        val middlename = prefs.getString("middlename", "User")
        val lastname = prefs.getString("lastname", "User")
        val suffix = prefs.getString("suffix", "User")
        val id = prefs.getString("id", "User")

        fullname.setText("${firstname} ${middlename} ${lastname} ${suffix}")
        farmerId.setText("${id}")
        // Example: set a click listener
        button_logout.setOnClickListener {
            // 1. Clear SharedPreferences
            prefs.edit().clear().apply()

            // 2. Clear SecurePrefsHelper (the saved JWT token)
            SecurePrefsHelper.clearToken(requireContext())

            // 3. Finish all activities
            requireActivity().finishAffinity()

            // 4. Start LoginActivity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
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

        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        val token = prefs.getString("token", "")
        val profileImage = prefs.getString("profileImage", "")
        val userName = prefs.getString("username", "")
        val emailAdd = prefs.getString("email", "")
        val firstname = prefs.getString("firstname", "")
        val middlename = prefs.getString("middlename", "")
        val lastname = prefs.getString("lastname", "")
        val suffix = prefs.getString("suffix", "")
        val birthDate = prefs.getString("birthdate", "")
        val contact = prefs.getString("contactNumber", "")
        val street = prefs.getString("street", "")
        val barangay = prefs.getString("barangay", "")
        val municipality = prefs.getString("municipality", "")
        val province = prefs.getString("province", "")
        val zipCode = prefs.getString("zipcode", "")
        val idNum = prefs.getString("idNumber", "")

        fullname?.text = listOfNotNull(firstname, middlename, lastname, suffix)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        farmerId?.text = idNum
        username?.text = userName
        email?.text = emailAdd
        birthdate?.text = birthDate
        contactNumber?.text = contact

        if (!profileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileImage)
                .placeholder(R.drawable.ic_person) // shown while loading
                .error(R.drawable.ic_person) // fallback if loading fails
                .circleCrop()
                .into(profilePic)
        } else {
            // keep the default icon
            profilePic.setImageResource(R.drawable.ic_person)
        }

        if (token != null) {
            RetrofitClient.instance.getFarmDetails("Bearer $token")
                .enqueue(object : Callback<FarmDetailsResponse> {
                    override fun onResponse(
                        call: Call<FarmDetailsResponse>,
                        response: Response<FarmDetailsResponse>
                    ) {
                        if (response.isSuccessful) {
                            val farm = response.body()
                            if (farm != null) {
                                // Show data in TextViews
                                farmName.text = farm.farmName
                                farmLocation.text = farm.farmLocation
                            }
                        } else {
                            Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<FarmDetailsResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
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