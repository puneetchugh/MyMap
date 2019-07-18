package com.example.mymap.constant

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

const val LOCATION_PERMISSION : Int = 101
const val LOG_TAG : String = "MyMap"
const val MAP_ZOOM : Double = 13.0
const val MIN_DISTANCE : Float = 1.0f
const val MIN_TIME : Long = 15000L
const val ACURACY_ALPHA = 0.6f
const val ELEVATION = 1f

fun isNetworkConnected(context : Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}
