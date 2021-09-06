package com.internshala.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.adapter.HomeRecyclerAdapter
import com.internshala.foodrunner.database.RestaurantDatabase
import com.internshala.foodrunner.database.RestaurantEntity
import com.internshala.foodrunner.model.Restaurant
import com.internshala.foodrunner.util.ConnectionManager
import org.json.JSONException


class FavouritesFragment : Fragment() {

    lateinit var recyclerAdapter: HomeRecyclerAdapter
    private lateinit var favRecycler: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var favProgressLayout: RelativeLayout
    var dbResList = arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        favRecycler = view.findViewById(R.id.favRecycler)
        favProgressLayout = view.findViewById(R.id.favProgressLayout)
        layoutManager = LinearLayoutManager(activity as Context)

        return view
    }

    private fun fetchData() {

        if (ConnectionManager().checkConnectivity(activity as Context)!!) {
            favProgressLayout.visibility = View.VISIBLE
            try {
                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {

                        println("Response12 is  $it")
                        val response = it.getJSONObject("data")
                        val success = response.getBoolean("success")

                        if (success) {

                            dbResList.clear()
                            val data = response.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantEntity = RestaurantEntity(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name")
                                )

                                if (DBAsyncTask(activity as Context, restaurantEntity, 1).execute().get())
                                {
                                    val restaurantObject = Restaurant(
                                        restaurantJsonObject.getString("id"),
                                        restaurantJsonObject.getString("name"),
                                        restaurantJsonObject.getString("rating"),
                                        restaurantJsonObject.getString("cost_for_one"),
                                        restaurantJsonObject.getString("image_url")
                                    )

                                    dbResList.add(restaurantObject)
                                    recyclerAdapter = HomeRecyclerAdapter(activity as Context, dbResList)
                                    favRecycler.adapter = recyclerAdapter
                                    favRecycler.layoutManager = layoutManager
                                }
                            }
                            if (dbResList.size == 0) {
                                Toast.makeText(
                                    activity as Context,
                                    "Nothing is added to Favorites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        favProgressLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        println("Error12 is $it")

                        Toast.makeText(
                            activity as Context,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()

                        favProgressLayout.visibility = View.INVISIBLE
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "bc7d33ad852f66"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    activity as Context,
                    "Some Unexpected error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }

    class DBAsyncTask(context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            /*
            * Mode 1->check if restaurant is in favourites
            * Mode 2->Save the restaurant into DB as favourites
            * Mode 3-> Remove the favourite restaurant*/
            return when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.resId)
                    db.close()
                    restaurant != null
                }
                else -> false
            }
        }
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(activity as Context)!!) {
            fetchData()
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }

        super.onResume()
    }
}
