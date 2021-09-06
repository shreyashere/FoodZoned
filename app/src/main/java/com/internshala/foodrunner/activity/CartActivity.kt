package com.internshala.foodrunner.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.adapter.CartRecyclerAdapter
import com.internshala.foodrunner.model.Cart
import com.internshala.foodrunner.util.ConnectionManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.MutableMap
import kotlin.collections.arrayListOf
import kotlin.collections.set

class CartActivity : AppCompatActivity() {

    lateinit var recyclerCart: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: CartRecyclerAdapter
    lateinit var btnOrder: Button
    lateinit var toolbar: Toolbar
    lateinit var txtView: TextView
    lateinit var foodSelected: ArrayList<String>
    lateinit var resName: String
    lateinit var resId: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    var totalAmount = 0
    val cartList = arrayListOf<Cart>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "0")

        resName = intent.getStringExtra("resName")!!
        foodSelected = intent.getStringArrayListExtra("selectedFoodId")!!
        resId = intent.getStringExtra("resId")!!

        btnOrder = findViewById(R.id.btnOrder)
        txtView = findViewById(R.id.txtView)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE
        recyclerCart = findViewById(R.id.recyclerCart)
        layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Cart"

        txtView.text = "Ordering from: $resName"

        btnOrder.setOnClickListener {
            if (ConnectionManager().checkConnectivity(this)!!) {
                progressLayout.visibility = View.VISIBLE
                val queue = Volley.newRequestQueue(this)
                val url = "http://13.235.250.119/v2/place_order/fetch_result/"

                val food = JSONArray()
                for (item in foodSelected) {
                    val selected = JSONObject()
                    selected.put("food_item_id", item)
                    food.put(selected)
                }
                val jsonParams = JSONObject()
                jsonParams.put("user_id", userId)
                jsonParams.put("restaurant_id", resId)
                jsonParams.put("total_cost", totalAmount)
                jsonParams.put("food", food)

                val jsonRequest = object: JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            startActivity(Intent(this, PlaceOrderActivity::class.java))
                            createNotification()
                            Toast.makeText(this, "Order Placed!", Toast.LENGTH_LONG).show()
                        } else {
                            val message = data.getString("errorMessage")
                            Toast.makeText(this, message.toString() , Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show()
                    }
                    progressLayout.visibility = View.GONE

                }, Response.ErrorListener {
                    Toast.makeText(this, "Couldn't connect to servers, Please try again", Toast.LENGTH_LONG).show()
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
    private fun fetchData() {
        if (ConnectionManager().checkConnectivity(this)!!) {

            progressLayout.visibility = View.VISIBLE
            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"

            try {

                val jsonObjectRequest = @SuppressLint("SetTextI18n")
                    object: JsonObjectRequest(Method.GET, url, null, Response.Listener {
                        val data1 = it.getJSONObject("data")
                        val success = data1.getBoolean("success")
                        if (success) {
                            val data = data1.getJSONArray("data")
                            cartList.clear()
                            totalAmount = 0

                            for (i in 0 until data.length()) {
                                val cartItem = data.getJSONObject(i)
                                if (foodSelected.contains(cartItem.getString("id"))) {
                                    val foodObject = Cart(
                                        cartItem.getString("id"),
                                        cartItem.getString("name"),
                                        cartItem.getString("cost_for_one"),
                                        cartItem.getString("restaurant_id")
                                    )
                                    totalAmount += cartItem.getString("cost_for_one").toString().toInt()
                                    cartList.add(foodObject)
                                }
                                recyclerAdapter = CartRecyclerAdapter(this, cartList)
                                recyclerCart.layoutManager = layoutManager
                                recyclerCart.adapter = recyclerAdapter
                            }
                            btnOrder.text = "Place Order (Rs. $totalAmount)"
                        }
                        progressLayout.visibility = View.GONE

            }, Response.ErrorListener {
                Toast.makeText(this, "Request timed out. Please try again!", Toast.LENGTH_LONG).show()
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
        } catch (e: JSONException) {
            Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show()
        }

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

    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Alert")
        dialog.setMessage("Going back will empty the cart contents")
        dialog.setPositiveButton("OK") {text, listener ->
            val intent = Intent(this, RestaurantActivity::class.java)
            startActivity(intent)
            finish()
        }
        dialog.setNegativeButton("No") {text, listener ->
        }
        dialog.create()
        dialog.show()
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(this)!!){
            fetchData()
        }
        super.onResume()
    }

    fun createNotification() {
        val notificationId = 1
        val channelId = "personal_notification"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        notificationBuilder.setSmallIcon(R.mipmap.app_icon_round)
        notificationBuilder.setContentTitle("Order Placed")
        notificationBuilder.setContentText("Your order has been successfully placed!")
        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS)
        notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText("Ordered from ${resName}. Please pay Rs. ${totalAmount}"))

        notificationBuilder.priority =NotificationCompat.PRIORITY_DEFAULT
        val notiManagerCompat = NotificationManagerCompat.from(this)
        notiManagerCompat.notify(notificationId, notificationBuilder.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Order Placed"
            val description = "Your order has been successfully placed!"
            val importance =NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description

            val notificationManager = (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
