package com.internshala.foodrunner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.internshala.foodrunner.R

class PlaceOrderActivity : AppCompatActivity() {

    lateinit var btnOk: Button
    lateinit var txtPlaced: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_order)

        btnOk = findViewById(R.id.btnOk)
        txtPlaced = findViewById(R.id.txtOrderPlaced)

        btnOk.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onPause() {
        super.onPause()
        startActivity(Intent(this, MainActivity::class.java))
    }
}