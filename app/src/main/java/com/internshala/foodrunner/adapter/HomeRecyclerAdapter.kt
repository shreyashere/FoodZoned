package com.internshala.foodrunner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.internshala.foodrunner.R
import com.internshala.foodrunner.activity.RestaurantActivity
import com.internshala.foodrunner.database.RestaurantDatabase
import com.internshala.foodrunner.database.RestaurantEntity
import com.internshala.foodrunner.model.Restaurant
import com.squareup.picasso.Picasso

class HomeRecyclerAdapter(
    val context: Context,
    private var itemList: ArrayList<Restaurant>): RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {

    class HomeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtResName: TextView = view.findViewById(R.id.txtResName)
        val txtCost: TextView = view.findViewById(R.id.txtCost)
        val txtFav: TextView = view.findViewById(R.id.txtFav)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val imgRes: ImageView = view.findViewById(R.id.imgRes)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row, parent, false)
        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun filterList(filteredList: ArrayList<Restaurant>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val restaurant = itemList[position]
        val restaurantEntity = RestaurantEntity(restaurant.resId, restaurant.resName)

        holder.txtResName.text = restaurant.resName
        holder.txtRating.text = restaurant.resRating
        holder.txtCost.text = "${restaurant.resCost}/person"
        Picasso.get().load(restaurant.resImage).error(R.drawable.foodicon).into(holder.imgRes)

        holder.llContent.setOnClickListener {
            val intent = Intent(context, RestaurantActivity::class.java)
            intent.putExtra("resId", restaurant.resId)
            intent.putExtra("resName", restaurant.resName)
            context.startActivity(intent)
        }
        holder.txtFav.setOnClickListener {
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                if (DBAsyncTask(context, restaurantEntity, 2).execute().get()) {
                    Toast.makeText(context, "${restaurant.resName} added to Favourites", Toast.LENGTH_SHORT).show()
                    holder.txtFav.tag = "liked"
                    holder.txtFav.background = ContextCompat.getDrawable(context, R.drawable.ic_fav_fill)
                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (DBAsyncTask(context, restaurantEntity, 3).execute().get()) {
                    Toast.makeText(context, "${restaurant.resName} removed from Favourites", Toast.LENGTH_SHORT).show()
                    holder.txtFav.tag = "disliked"
                    holder.txtFav.background = ContextCompat.getDrawable(context, R.drawable.ic_fav_outline)
                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
            holder.txtFav.tag = "liked"
            holder.txtFav.background = ContextCompat.getDrawable(context, R.drawable.ic_fav_fill)
        } else {
            holder.txtFav.tag = "disliked"
            holder.txtFav.background = ContextCompat.getDrawable(context, R.drawable.ic_fav_outline)
        }

    }

    class DBAsyncTask(context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int): AsyncTask<Void, Void, Boolean>() {

        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg params: Void): Boolean {

            when(mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao().getRestaurantById(restaurantEntity.resId)
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}