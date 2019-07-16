package com.example.mymap.model

import com.example.mymap.model.data.model
import io.reactivex.Observable
import retrofit2.http.GET

interface ApiService{
    @GET("/development/scripts/get_map_pins.php")
    fun getPinList() : Observable<List<model.Pin>>
}