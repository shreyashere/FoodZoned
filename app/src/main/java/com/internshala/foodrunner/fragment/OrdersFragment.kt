package com.internshala.foodrunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.adapter.OrdersRecyclerAdapter
import com.internshala.foodrunner.model.Orders
import com.internshala.foodrunner.util.ConnectionManager
import org.json.JSONException


class OrdersFragment : Fragment() {

    lateinit var recyclerRestaurant: RecyclerView
    lateinit var linearLayout: LinearLayoutManager
    lateinit var recyclerAdapter: OrdersRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    private lateinit var noOrders: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_orders, container, false)

        val ordersList = ArrayList<Orders>()
        val sharedPreferences = context?.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("user_id", "00")

        recyclerRestaurant = view.findViewById(R.id.recyclerRestaurant)
        linearLayout = LinearLayoutManager(activity as Context)
        progressLayout = view.findViewById(R.id.progressLayout)
        noOrders = view.findViewById(R.id.noOrders)

        if (ConnectionManager().checkConnectivity(activity as Context)!!) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/${userId}"

            val jsonObjectRequest = object: JsonObjectRequest(Method.GET, url, null,
                Response.Listener {
                                  try {
                                      progressLayout.visibility = View.VISIBLE
                                      val response = it.getJSONObject("data")
                                      val success = response.getBoolean("success")
                                      if (success) {
                                          val data = response.getJSONArray("data")
                                          if (data.length() == 0) {
                                              Toast.makeText(activity as Context, "No orders placed yet", Toast.LENGTH_SHORT).show()
                                              noOrders.visibility = View.VISIBLE
                                          } else {
                                              noOrders.visibility = View.GONE
                                              for (i in 0 until data.length()) {
                                                  val orderItem = data.getJSONObject(i)
                                                  val resObj = Orders(
                                                      orderItem.getString("order_id"),
                                                      orderItem.getString("restaurant_name"),
                                                      orderItem.getString("total_cost"),
                                                      orderItem.getString("order_placed_at").substring(0,10)
                                                  )

                                                  ordersList.add(resObj)
                                                  recyclerAdapter = OrdersRecyclerAdapter(activity as Context, ordersList, userId)
                                                  recyclerRestaurant.adapter = recyclerAdapter
                                                  recyclerRestaurant.layoutManager = linearLayout
                                              }
                                          }
                                      }
                                      progressLayout.visibility = View.GONE
                                  } catch (e: JSONException) {
                                      Toast.makeText(activity as Context, "Some error occurred", Toast.LENGTH_SHORT).show()
                                  }
                }, Response.ErrorListener {
                    Toast.makeText(activity as Context, "Some error occurred", Toast.LENGTH_SHORT).show()
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
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Your device is not connected to the internet")
            dialog.setPositiveButton("Open Settings") {text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(settingsIntent)
            }
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }

        return view
    }

}