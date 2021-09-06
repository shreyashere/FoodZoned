package com.internshala.foodrunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.R
import com.internshala.foodrunner.activity.MainActivity
import com.internshala.foodrunner.adapter.HomeRecyclerAdapter
import com.internshala.foodrunner.model.Restaurant
import com.internshala.foodrunner.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONException
import java.nio.file.Files.size
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    lateinit var recyclerHome: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HomeRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var etSearch: EditText
    lateinit var cantFind: RelativeLayout

    val resList = arrayListOf<Restaurant>()

    var costComp = Comparator<Restaurant> {res1, res2 ->
        res1.resCost.compareTo(res2.resCost, true)
    }

    var ratingComp = Comparator<Restaurant> {res1, res2 ->
        if (res1.resRating.compareTo(res2.resRating, true) == 0) {
            res1.resName.compareTo(res2.resName, true)
        } else {
            res1.resRating.compareTo(res2.resRating, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setHasOptionsMenu(true)

        progressLayout = view.findViewById(R.id.progressLayout)
        etSearch = view.findViewById(R.id.etSearch)
        progressBar = view.findViewById(R.id.progressBar)

        cantFind = view.findViewById(R.id.cantFind)
        recyclerHome = view.findViewById(R.id.recyclerHome)

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().checkConnectivity(activity as Context)!!) {
            val jsonObjectRequest = object: JsonObjectRequest(Method.GET, url, null, Response.Listener {
                try {
                    progressLayout.visibility = View.VISIBLE
                    val data1 = it.getJSONObject("data")
                    val success = data1.getBoolean("success")
                    if (success) {
                        val data = data1.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val resJsonObject = data.getJSONObject(i)
                            val resObject = Restaurant(
                                resJsonObject.getString("id"),
                                resJsonObject.getString("name"),
                                resJsonObject.getString("rating"),
                                resJsonObject.getString("cost_for_one"),
                                resJsonObject.getString("image_url")
                            )
                            resList.add(resObject)
                            layoutManager = LinearLayoutManager(activity)
                            recyclerAdapter = HomeRecyclerAdapter(activity as Context, resList)
                            recyclerHome.adapter = recyclerAdapter
                            recyclerHome.layoutManager = layoutManager
                        }
                        progressLayout.visibility = View.GONE
                    } else {
                        Toast.makeText(activity as Context, "Some error occurred!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(activity as Context, "Some unexpected error occurred! $e", Toast.LENGTH_LONG).show()
                } }, Response.ErrorListener {

                if (activity != null) {
                    Toast.makeText(activity as Context, "Please try again", Toast.LENGTH_SHORT).show()
                }
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

        fun filterFun(s: String) {
            val filteredList = arrayListOf<Restaurant>()

            for (item in resList) {
                if (item.resName.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {
                    filteredList.add(item)
                }
            }
            if (filteredList.size == 0) {
                cantFind.visibility = View.VISIBLE
            } else {
                cantFind.visibility = View.INVISIBLE
            }
            recyclerAdapter.filterList(filteredList)
        }

        etSearch.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterFun(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_sort) {
            val dialog = AlertDialog.Builder(activity as Context)
            val array = arrayOf("Cost (Low to High)", "Cost (High to Low)", "Rating")
            dialog.setTitle("Sort by?")
            var checkedItem = 0
            dialog.setSingleChoiceItems(array, checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        Collections.sort(resList, costComp)
                    }
                    1 -> {
                        Collections.sort(resList, costComp)
                        resList.reverse()
                    }
                    2 -> {
                        Collections.sort(resList, ratingComp)
                        resList.reverse()
                    }
                }
                checkedItem = which
            }
            dialog.setPositiveButton("Done") { text, listener ->
                recyclerAdapter.notifyDataSetChanged()
            }
            dialog.setNegativeButton("Back") { dialog, which ->
                dialog.dismiss()
            }
            dialog.create()
            dialog.show()
        }
        return super.onOptionsItemSelected(item)
    }
}
