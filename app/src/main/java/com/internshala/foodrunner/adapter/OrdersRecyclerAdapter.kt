package com.internshala.foodrunner.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.model.Cart
import com.internshala.foodrunner.model.Orders
import com.internshala.foodrunner.util.ConnectionManager

class OrdersRecyclerAdapter(val context: Context, private val ordersList: ArrayList<Orders>, private val userId: String?): RecyclerView.Adapter<OrdersRecyclerAdapter.OrdersViewHolder>() {

    class OrdersViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtRes: TextView = view.findViewById(R.id.txtRes)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val recyclerItems: RecyclerView = view.findViewById(R.id.recyclerItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_orders_single_row, parent, false)
        return OrdersViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val resObject = ordersList[position]
        holder.txtRes.text = "${resObject.orderResName} (Rs. ${resObject.orderCost})"
        var formatdate = resObject.orderDate
        formatdate = formatdate.replace("-","/")
        formatdate = formatdate.substring(0,6) + "20" + formatdate.substring(6,8)
        holder.txtDate.text = formatdate

        val layoutManager = LinearLayoutManager(context)
        var itemsAdapter: CartRecyclerAdapter

        if (ConnectionManager().checkConnectivity(context)!!) {
            val cartItems = ArrayList<Cart>()
            val queue = Volley.newRequestQueue(context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/${userId}"

            val jsonObjectRequest = object: JsonObjectRequest(Method.GET, url, null,
                Response.Listener {
                    val response = it.getJSONObject("data")
                    val success = response.getBoolean("success")
                    if (success) {
                        val data = response.getJSONArray("data")
                        val objRetrieved = data.getJSONObject(position)
                        cartItems.clear()
                        val foodItems = objRetrieved.getJSONArray("food_items")

                        for (j in 0 until foodItems.length()) {
                            val singleItem = foodItems.getJSONObject(j)
                            val itemObj = Cart(
                                singleItem.getString("food_item_id"),
                                singleItem.getString("name"),
                                singleItem.getString("cost"),
                                "000"
                            )
                            cartItems.add(itemObj)
                        }
                        itemsAdapter = CartRecyclerAdapter(context, cartItems)
                        holder.recyclerItems.adapter = itemsAdapter
                        holder.recyclerItems.layoutManager = layoutManager
                    }

                }, Response.ErrorListener {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                })
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] ="application/json"
                    headers["token"] = "bc7d33ad852f66"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("Error")
            dialog.setMessage("Your device is not connected to the internet")
            dialog.setPositiveButton("Open Settings") {_, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(context, settingsIntent, null)
            }
            dialog.setNegativeButton("Exit") {_, _ ->
                ActivityCompat.finishAffinity(context as Activity)
            }
            dialog.create()
            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return  ordersList.size
    }
}