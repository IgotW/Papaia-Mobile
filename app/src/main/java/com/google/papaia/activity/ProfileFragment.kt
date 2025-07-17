package com.google.papaia.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.papaia.R
import com.google.papaia.utils.SecurePrefsHelper

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

    private lateinit var fullname: TextView
    private lateinit var farmerId: TextView

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
        fullname = view.findViewById(R.id.txtview_fullname)
        farmerId = view.findViewById(R.id.txtview_farmerid)
        val button_editprofile = view.findViewById<Button>(R.id.button_editprofile)
        val button_settings = view.findViewById<Button>(R.id.button_settings)
        val button_changepass = view.findViewById<Button>(R.id.button_changepassword)
        val button_logout = view.findViewById<Button>(R.id.button_logout)

        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
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
    }
    override fun onResume() {
        super.onResume()

        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        val firstname = prefs.getString("firstname", "User")
        val middlename = prefs.getString("middlename", "")
        val lastname = prefs.getString("lastname", "")
        val suffix = prefs.getString("suffix", "")
        val street = prefs.getString("street", "")
        val barangay = prefs.getString("barangay", "")
        val municipality = prefs.getString("municipality", "")
        val province = prefs.getString("province", "")
        val zipCode = prefs.getString("zipcode", "")
        val id = prefs.getString("id", "User")

        fullname?.text = listOfNotNull(firstname, middlename, lastname, suffix)
            .filter { it.isNotBlank() }
            .joinToString(" ")

        farmerId?.text = id
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