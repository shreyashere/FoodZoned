package com.internshala.foodrunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @ColumnInfo(name = "res_id") @PrimaryKey val resId: String,
    @ColumnInfo(name = "res_name") val resName: String
)
