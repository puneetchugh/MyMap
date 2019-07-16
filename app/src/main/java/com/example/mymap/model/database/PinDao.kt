package com.example.mymap.model.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mymap.model.data.model
import io.reactivex.Observable

@Dao
interface PinDao{

    @Query("SELECT * FROM all_pins")
    fun getAllPinsFromDb() : LiveData<List<model.Pin>>

    @Query("SELECT * FROM all_pins")
    fun getAllPinsListFromDb() : List<model.Pin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pin : model.Pin)

    @Delete
    fun remove(pin: model.Pin)
}