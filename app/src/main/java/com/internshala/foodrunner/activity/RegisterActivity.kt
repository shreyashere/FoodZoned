package com.internshala.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    lateinit var etName: EditText
    lateinit var etEmail: EditText
    lateinit var etMobileNo: EditText
    lateinit var etAddress: EditText
    lateinit var etPassword: EditText
    lateinit var etConfPassword: EditText
    lateinit var btnRegister: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etMobileNo = findViewById(R.id.etMobileNo)
        etAddress = findViewById(R.id.etAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfPassword = findViewById(R.id.etConfPassword)
        btnRegister = findViewById(R.id.btnRegister)
        toolbar = findViewById(R.id.toolbar)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnRegister.setOnClickListener {

            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val mobileNumber = etMobileNo.text.toString()
            val address = etAddress.text.toString()
            val password = etPassword.text.toString()
            val confPassword = etConfPassword.text.toString()

            val queue = Volley.newRequestQueue(this@RegisterActivity)
            val url = "http://13.235.250.119/v2/register/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("name", name)
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("password", password)
            jsonParams.put("address", address)
            jsonParams.put("email", email)

            if (ConnectionManager().checkConnectivity(this@RegisterActivity)!!) {
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

                            if (password == confPassword) {
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@RegisterActivity, "Passwords do not match", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Some error occurred $data", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Please try again $e", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(this@RegisterActivity, "Couldn't connect to server, please try again", Toast.LENGTH_SHORT).show()
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
                val dialog = AlertDialog.Builder(this@RegisterActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Your device is not connected to the internet")
                dialog.setPositiveButton("Open Settings") {text, listener ->
                    val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit") {text, listener ->
                    ActivityCompat.finishAffinity(this@RegisterActivity)
                }
                dialog.create()
                dialog.show()
            }
        }
    }

    fun savePreferences(id: String, name: String, email: String, mobile_number: String, address: String) {
        sharedPreferences.edit().putString("user_id", id).apply()
        sharedPreferences.edit().putString("name", name).apply()
        sharedPreferences.edit().putString("email", email).apply()
        sharedPreferences.edit().putString("mobile_number", mobile_number).apply()
        sharedPreferences.edit().putString("address", address).apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}


