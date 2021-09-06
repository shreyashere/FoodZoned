package com.internshala.foodrunner.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_reset_password.*
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var etOTP: EditText
    lateinit var etNewPass: EditText
    lateinit var etConfNewPass: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val mobileNo = intent.getStringExtra("mobileNo")

        etOTP = findViewById(R.id.etOTP)
        etNewPass = findViewById(R.id.etNewPass)
        etConfNewPass = findViewById(R.id.etConfNewPass)

        btnSubmit.setOnClickListener {

            val otp = etOTP.text.toString()
            val newPass = etNewPass.text.toString()
            val confNewPass = etConfNewPass.text.toString()

            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/reset_password/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNo)
            jsonParams.put("password", newPass)
            jsonParams.put("otp", otp)

            if (ConnectionManager().checkConnectivity(this)!!) {
                val jsonRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            if (newPass == confNewPass) {
                                Toast.makeText(this, "Password updated successfully! Please log in again", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Some error occurred $data", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(this, "Couldn't connect to the server, please try again", Toast.LENGTH_LONG).show()
                })
                {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "bc7d33ad852f66"
                        return headers
                    }
                }
                queue.add(jsonRequest)
            } else {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Error")
                dialog.setMessage("Your device is not connected to the internet")
                dialog.setPositiveButton("Open Settings") {text, listener ->
                    val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit") {text, listener ->
                    ActivityCompat.finishAffinity(this)
                }
                dialog.create()
                dialog.show()
            }
        }
    }
}
