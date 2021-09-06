package com.internshala.foodrunner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.internshala.foodrunner.R
import com.internshala.foodrunner.model.Cart

class CartRecyclerAdapter(
    val context: Context,
    private val itemList: ArrayList<Cart>): RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {

    class CartViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtFoodName: TextView = view.findViewById(R.id.txtFoodName)
        val txtFoodCost: TextView = view.findViewById(R.id.txtFoodCost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_cart_single_row, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cart = itemList[position]
        holder.txtFoodName.text = cart.foodName
        holder.txtFoodCost.text = "Rs. ${cart.foodCost}"
    }
}
