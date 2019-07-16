package com.example.mymap.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object model{

    @Entity(tableName = "all_pins")
    data class Pin(@PrimaryKey val id : Int, val name : String, val latitude : Double, val longitude : Double, val description : String)
}