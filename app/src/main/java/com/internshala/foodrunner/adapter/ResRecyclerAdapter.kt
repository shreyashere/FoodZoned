package com.internshala.foodrunner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.internshala.foodrunner.activity.CartActivity
import com.internshala.foodrunner.R
import com.internshala.foodrunner.model.Cart
import com.internshala.foodrunner.model.Food

class ResRecyclerAdapter(
    val context: Context,
    private val itemList: ArrayList<Food>,
    private val proceedToCartPassed: RelativeLayout,
    private val btnProceed: Button,
    private val resId: String?,
    private val resName: String?
): RecyclerView.Adapter<ResRecyclerAdapter.RestaurantViewHolder>() {

    var foodSelected = arrayListOf<String>()
    var itemSelectedCount = 0
    lateinit var proceedToCart: RelativeLayout

    class RestaurantViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtFoodId: TextView = view.findViewById(R.id.txtFoodId)
        val txtFood: TextView = view.findViewById(R.id.txtFood)
        val txtFoodCost: TextView = view.findViewById(R.id.txtFoodCost)
        val btnAdd: Button = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_res_single_row, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val food = itemList[position]
        proceedToCart = proceedToCartPassed
        holder.txtFoodId.text = (position+1).toString()
        holder.txtFood.text = food.foodName
        holder.txtFoodCost.text = "Rs. ${food.foodCost}"
        holder.btnAdd.tag = food.foodId

        holder.btnAdd.setOnClickListener {
            if (holder.btnAdd.text == "Add") {
                itemSelectedCount++
                foodSelected.add(holder.btnAdd.tag.toString())
                holder.btnAdd.text = "Remove"
                holder.btnAdd.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow))

            } else {
                itemSelectedCount--
                foodSelected.remove(holder.btnAdd.tag.toString())
                holder.btnAdd.text = "Add"
                holder.btnAdd.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            }
        }
        btnProceed.setOnClickListener {
            val intent = Intent(context, CartActivity::class.java)
            intent.putExtra("selectedFoodId", foodSelected)
            intent.putExtra("resId", resId)
            intent.putExtra("resName", resName)
            context.startActivity(intent)
        }
    }
    fun getSelectedItemCount(): Int {
        return itemSelectedCount
    }
}
