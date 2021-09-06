package com.internshala.foodrunner.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.adapter.HomeRecyclerAdapter
import com.internshala.foodrunner.adapter.ResRecyclerAdapter
import com.internshala.foodrunner.database.RestaurantEntity
import com.internshala.foodrunner.model.Food
import com.internshala.foodrunner.util.ConnectionManager
import org.json.JSONException

class RestaurantActivity : AppCompatActivity() {

    lateinit var recyclerRes: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: ResRecyclerAdapter
    lateinit var btnProceed: Button
    lateinit var proceedToCartLayout: RelativeLayout
    lateinit var progressLayout: RelativeLayout
    private lateinit var toolbar: Toolbar
    lateinit var resId: String
    private lateinit var txtFav: TextView
    lateinit var resName: String
    var foodList = arrayListOf<Food>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        resId = intent.getStringExtra("resId")!!
        resName = intent.getStringExtra("resName")!!

        btnProceed = findViewById(R.id.btnProceed)
        txtFav = findViewById(R.id.txtFav)
        proceedToCartLayout = findViewById(R.id.relativeLayoutProceedToCart)
        progressLayout = findViewById(R.id.resProgressLayout)

        recyclerRes = findViewById(R.id.recyclerRes)
        layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resName

        val restaurantEntity = RestaurantEntity(resId, resName)

        txtFav.setOnClickListener {
            if (!HomeRecyclerAdapter.DBAsyncTask(this, restaurantEntity, 1).execute().get()) {
                if (HomeRecyclerAdapter.DBAsyncTask(this, restaurantEntity, 2).execute().get()) {
                    txtFav.tag = "liked"
                    txtFav.background = ContextCompat.getDrawable(this, R.drawable.ic_fav_fill)
                } else {
                    Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (HomeRecyclerAdapter.DBAsyncTask(this, restaurantEntity, 3).execute().get()) {
                    txtFav.tag = "disliked"
                    txtFav.background = ContextCompat.getDrawable(this, R.drawable.ic_fav_outline)
                } else {
                    Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (HomeRecyclerAdapter.DBAsyncTask(this, restaurantEntity, 1).execute().get()) {
            txtFav.tag = "liked"
            txtFav.background = ContextCompat.getDrawable(this, R.drawable.ic_fav_fill)
        } else {
            txtFav.tag = "disliked"
            txtFav.background = ContextCompat.getDrawable(this, R.drawable.ic_fav_outline)
        }

    }

    private fun fetchData() {
        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"

        if (ConnectionManager().checkConnectivity(this)!!) {
            val jsonObjectRequest = object: JsonObjectRequest(Method.GET, url, null, Response.Listener {
                try {
                    progressLayout.visibility = View.VISIBLE
                    val data1 = it.getJSONObject("data")
                    val success = data1.getBoolean("success")
                    if (success) {
                        val data = data1.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val foodJsonObject = data.getJSONObject(i)
                            val foodObject = Food(
                                foodJsonObject.getString("id"),
                                foodJsonObject.getString("name"),
                                foodJsonObject.getString("cost_for_one")
                            )
                            foodList.add(foodObject)
                            recyclerAdapter =
                                ResRecyclerAdapter(this, foodList, proceedToCartLayout, btnProceed, resId, resName)
                            recyclerRes.adapter = recyclerAdapter
                            recyclerRes.layoutManager = layoutManager
                            recyclerAdapter.notifyDataSetChanged()

                        }
                    }
                    progressLayout.visibility = View.GONE

                } catch (e: JSONException) {
                    Toast.makeText(this, "Some unexpected error occurred! $e", Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
            })
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "bc7d33ad852f66"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)

        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Error")
            dialog.setMessage("Your device is not connected to the internet")
            dialog.setPositiveButton("Open Settings") {text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(settingsIntent)
            }
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(this)
            }
            dialog.create()
            dialog.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(this)!!) {
            if (foodList.isEmpty())
                fetchData()
        }
        super.onResume()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}

