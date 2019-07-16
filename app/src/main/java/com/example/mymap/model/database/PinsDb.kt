package com.example.mymap.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mymap.model.data.model

@Database(entities = [model.Pin::class], version = 1)
abstract class PinsDb : RoomDatabase(){
    abstract fun pinDao() : PinDao
}