package com.internshala.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.internshala.foodrunner.R
import com.internshala.foodrunner.fragment.*
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.android.synthetic.main.drawer_header.view.*

class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var sharedPreferences: SharedPreferences

    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frame)
        navigationView = findViewById(R.id.navigationView)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        val navView = navigationView.getHeaderView(0)
        navView.txtName.text = sharedPreferences.getString("name", "Your name")
        navView.txtMobileNo.text = sharedPreferences.getString("mobile_number", "Your mobile number")

        setUpToolbar()

        openHome()

        val actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener{

            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            when (it.itemId) {
                R.id.home -> {
                    openHome()
                    drawerLayout.closeDrawers()
                }
                R.id.favourites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FavouritesFragment()
                        )
                        .commit()
                    supportActionBar?.title = "Favourites"
                    drawerLayout.closeDrawers()
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            ProfileFragment()
                        )
                        .commit()
                    supportActionBar?.title = "Profile"
                    drawerLayout.closeDrawers()
                }
                R.id.orders -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            OrdersFragment()
                        )
                        .commit()
                    supportActionBar?.title = "Order History"
                    drawerLayout.closeDrawers()
                }
                R.id.faq -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FaqFragment()
                        )
                        .commit()
                    supportActionBar?.title = "FAQs"
                    drawerLayout.closeDrawers()
                }
                R.id.logout -> {
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Confirmation")
                    dialog.setMessage("Are you sure you want to log out?")
                    dialog.setPositiveButton("Yes") {text, listener ->
                        sharedPreferences.edit().clear().apply()
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    dialog.setNegativeButton("No") {dialog, which ->
                        dialog.dismiss()
                    }
                    dialog.create()
                    dialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    fun openHome() {
        val fragment = HomeFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)

        when(frag){
            !is HomeFragment -> openHome()
            else -> finishAffinity()
        }
    }
}
