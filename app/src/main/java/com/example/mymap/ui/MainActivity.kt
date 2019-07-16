package com.example.mymap.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.mymap.R
import com.example.mymap.constant.*
import com.example.mymap.model.data.model
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import com.example.mymap.viewmodels.MainActivityViewModel
import java.lang.Exception
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
                     OnMapReadyCallback,
                     LocationListener{

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var mainActivityViewModel: MainActivityViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var map: GoogleMap

    val pinsObserver = Observer<List<model.Pin>>{ list ->
        list?.takeIf { !it.isEmpty() }
            ?.let {markMapPins(it) }
    }

    val errorObserver = Observer<String>{ value ->
        if(value != null && !value.isBlank()){
            showSnackbarMessage(this@MainActivity.resources.getString(R.string.no_data))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = resources.getString(R.string.locations)
        AndroidInjection.inject(this)

        mainActivityViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)

        val mapFragment : SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)

        checkLocationPermission()

        mainActivityViewModel.getAllPinsList().takeIf{!it.isEmpty() }
            ?.let {markMapPins(it) }

        mainActivityViewModel.errorMessage.observe(this, errorObserver)
        mainActivityViewModel.getAllPinsLiveData().observe(this, pinsObserver)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if(googleMap == null)
            Log.e(LOG_TAG, "googleMap is null")
        map = googleMap ?: return
    }

    private fun isAboveMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun markMapPins(pinsList : List<model.Pin>) {
        var counter = 0
        if (::map.isInitialized) {
            pinsList.forEach {
                map.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)).title(it.name))
                if (counter == 0)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude),
                        MAP_ZOOM
                    ))
                Log.d(LOG_TAG, "location : " + it.name)
                counter++
            }
        }
    }

    //suppressing missing permission as this method is only called
    //if the app has location permission granted
    @SuppressLint("MissingPermission")
    private fun getUserLoc() {
        Log.d(LOG_TAG, "getUserLoc() called")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                location?.let { onLocationChanged(it) }
            }


        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria : Criteria = Criteria()

        val providerString = locationManager.getBestProvider(criteria, false)

        if(providerString != null && !providerString.isBlank() && isLocationPermissionEnabled()){
            val location = locationManager.getLastKnownLocation(providerString)
            locationManager.requestLocationUpdates(providerString, MIN_TIME, MIN_DISTANCE, this)

            if(location != null) onLocationChanged(location) else return
        }
    }

    private fun checkLocationPermission(){
        if(isAboveMarshmallow()){
            when{
                isLocationPermissionEnabled() -> getUserLoc()
                shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) -> displayRationale()
                else -> {
                    requestLocationPermission()
                }
            }
        }
        else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION
        )
    }

    private fun displayRationale() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_string))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ -> requestLocationPermission() }
            .setNegativeButton(getString(R.string.cancel)
            ) { _, _ -> }
            .show()
    }

    private fun isLocationPermissionEnabled(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting location permission.")
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLoc()
                } else {
                    showSnackbarMessage(resources.getString(R.string.enable_loc_perm))
                }
            }
        }
    }

    override fun onLocationChanged(location: Location?) {
        Log.d(LOG_TAG, String.format("New loc : (%f,%f)",location?.latitude, location?.longitude))
        location?.let { selfLocationMarker(it) }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun showSnackbarMessage(message : String){
        val snack = Snackbar.make( main_activity_id, message,
            Snackbar.LENGTH_LONG)
        snack.show()
    }

    private fun selfLocationMarker(location: Location?){
        Log.d(LOG_TAG, "selfLocationMarker() called")
        try {
            map.addMarker(MarkerOptions().position(LatLng(location!!.latitude, location!!.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)).title(resources.getString(
                    R.string.your_location
                )))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude),
                MAP_ZOOM
            ))
        }
        catch (exception : Exception){}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.select_teams) {
            val intent = Intent(this, PinsListActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
