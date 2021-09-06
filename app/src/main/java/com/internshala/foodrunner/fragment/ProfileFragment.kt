package com.internshala.foodrunner.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.internshala.foodrunner.R

class ProfileFragment : Fragment() {

    private lateinit var txtName: TextView
    private lateinit var txtMobNo: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAddress: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        txtAddress = view.findViewById(R.id.txtAddress)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtMobNo = view.findViewById(R.id.txtMobNo)
        txtName = view.findViewById(R.id.txtName)

        val sharedPreferences = context?.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        if (sharedPreferences != null) {
            txtName.text =sharedPreferences.getString("name", "")
        }
        if (sharedPreferences != null) {
            txtMobNo.text = "+91-${sharedPreferences.getString("mobile_number", "")}"
        }
        if (sharedPreferences != null) {
            txtEmail.text = sharedPreferences.getString("email", "")
        }
        if (sharedPreferences != null) {
            txtAddress.text = sharedPreferences.getString("address", "")
        }

        return view
    }

}