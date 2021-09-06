package com.internshala.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNo: EditText
    lateinit var etPassword: EditText
    lateinit var btnLogin: Button
    lateinit var txtForgotPassword: TextView
    lateinit var txtSignUp: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        setContentView(R.layout.activity_login)

        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        etMobileNo = findViewById(R.id.etMobileNo)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtSignUp = findViewById(R.id.txtSignUp)

        txtSignUp.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        txtForgotPassword.setOnClickListener{
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val mobileNo = etMobileNo.text.toString()
            val password = etPassword.text.toString()

            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/login/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNo)
            jsonParams.put("password", password)

            if (ConnectionManager().checkConnectivity(this)!!) {
                val jsonRequest = object: JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val userJsonObject = data.getJSONObject("data")
                            val txtId = userJsonObject.getString("user_id")
                            val txtName = userJsonObject.getString("name")
                            val txtEmail = userJsonObject.getString("email")
                            val txtMobileNo = userJsonObject.getString("mobile_number")
                            val txtAddress = userJsonObject.getString("address")

                            savePreferences(txtId, txtName, txtEmail, txtMobileNo, txtAddress)

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show()
                        }
                    } catch (e:Exception) {
                        Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(this, "Couldn't connect to servers, please try again", Toast.LENGTH_LONG).show()
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

    fun savePreferences(id: String, name: String, email: String, mobile_number: String, address: String) {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("user_id", id).apply()
        sharedPreferences.edit().putString("name", name).apply()
        sharedPreferences.edit().putString("email", email).apply()
        sharedPreferences.edit().putString("mobile_number", mobile_number).apply()
        sharedPreferences.edit().putString("address", address).apply()
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
