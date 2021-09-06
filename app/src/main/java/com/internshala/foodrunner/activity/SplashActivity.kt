package com.internshala.foodrunner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.internshala.foodrunner.R

class SplashActivity : AppCompatActivity() {

    lateinit var topAnim: Animation
    lateinit var txtAppName: TextView
    lateinit var imgAppIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        topAnim = AnimationUtils.loadAnimation(this, R.anim.animation_top)
        txtAppName = findViewById(R.id.txtAppName)
        imgAppIcon = findViewById(R.id.imgAppIcon)

        txtAppName.animation = topAnim
        imgAppIcon.animation = topAnim

        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }, 2000)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
