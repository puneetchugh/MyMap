package com.example.mymap.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.mymap.R
import com.example.mymap.constant.ACURACY_ALPHA
import com.example.mymap.constant.ELEVATION
import com.example.mymap.constant.LOG_TAG
import com.example.mymap.constant.MAP_ZOOM
import com.example.mymap.model.data.model
import com.example.mymap.viewmodels.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
                     OnMapReadyCallback,
                     PermissionsListener {


    private var permisionsManager : PermissionsManager = PermissionsManager(this)
    lateinit var map : MapboxMap
    lateinit var markerViewManager : MarkerViewManager
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var mainActivityViewModel: MainActivityViewModel

    //observer for observing database changes and then reflecting
    //those changes on the UI
    val pinsObserver = Observer<List<model.Pin>>{ list ->
        list?.takeIf { !it.isEmpty() && ::map.isInitialized }
            ?.let (::displayPinLocations) }

    val errorObserver = Observer<String>{ value ->
        if(value != null && !value.isBlank()){
            showSnackbarMessage(this@MainActivity.resources.getString(R.string.no_data))
            mapView?.visibility = View.GONE
            status_message.visibility = View.VISIBLE
        } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this,"pk.eyJ1IjoicHVuZWV0Y2h1Z2giLCJhIjoiY2p5NXp0aWVrMDhlejNicGpidXhlbzg0dyJ9.32BqnT0YInxYk4uUfA9VNQ")
        setContentView(R.layout.activity_main)
        title = resources.getString(R.string.locations)
        AndroidInjection.inject(this)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        mainActivityViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)

        mainActivityViewModel.errorMessage.observe(this, errorObserver)
        mainActivityViewModel.getAllPinsLiveData().observe(this, pinsObserver)

    }

    override fun onMapReady(mapBox : MapboxMap) {
        map = if (mapBox != null) mapBox else return

        //map.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/cjerxnqt3cgvp2rmyuxbeqme7")){
        map.setStyle(Style.MAPBOX_STREETS){
            enableLocationComponent(it)
        }
        Log.d(LOG_TAG, "Inside onMapReady() .... map : "+map)
        markerViewManager = MarkerViewManager(mapView, map)
        mainActivityViewModel.getAllPinsList().takeIf{!it.isEmpty() && ::map.isInitialized }
            ?.let (::displayPinLocations)
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

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        Log.d(LOG_TAG, "onStart() called ..")
        mainActivityViewModel.getAllPinsList()
            .takeIf{!it.isEmpty() && ::map.isInitialized }
            ?.let (::displayPinLocations)
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    @SuppressWarnings("MissingPermission")
    private fun enableLocationComponent(style: Style) {

        if(PermissionsManager.areLocationPermissionsGranted(this)){

            if (!::map.isInitialized || map.style == null){
                Log.e(LOG_TAG, "map isn't initialized yet..returning")
                showSnackbarMessage(resources.getString(R.string.issue_with_map))
                return
            }

            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .foregroundDrawable(R.drawable.mapbox_mylocation_icon_bearing)
                .trackingGesturesManagement(true)
                .elevation(ELEVATION)
                .accuracyAlpha(ACURACY_ALPHA)
                .accuracyColor(Color.GRAY)
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, style)
                .locationComponentOptions(customLocationComponentOptions)
                .useDefaultLocationEngine(true)
                .build()

            map.locationComponent.apply{
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
        } else {
            permisionsManager = PermissionsManager(this)
            permisionsManager.requestLocationPermissions(this)
        }
    }


    private fun displayPinLocations(pinsList : List<model.Pin>){
        mapView?.visibility = View.VISIBLE
        status_message.visibility = View.GONE
        Log.d(LOG_TAG, "displayPinLocations() called, with pinsList : "+pinsList)
       pinsList.forEach {pin ->
            val imageView =  ImageView(this@MainActivity)
            imageView.setImageResource(R.drawable.mapbox_markerview_icon_default)
            imageView.layoutParams = FrameLayout.LayoutParams(128, 128)

            val cameraPosition = com.mapbox.mapboxsdk.camera.CameraPosition.Builder()
                .target(com.mapbox.mapboxsdk.geometry.LatLng(pin.latitude, pin.longitude))
                .zoom(MAP_ZOOM.toDouble())
                .build()
            TooltipCompat.setTooltipText(imageView, pin.name)
            markerViewManager?.addMarker(MarkerView(com.mapbox.mapboxsdk.geometry.LatLng(pin.latitude, pin.longitude),imageView))
            map.cameraPosition = cameraPosition }
    }

    private fun isAboveMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun displayRationale() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_string))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ -> run{
                permisionsManager = PermissionsManager(this)
                permisionsManager.requestLocationPermissions(this)
            } }
            .show()
    }

    private fun showSnackbarMessage(message : String){
        val snack = Snackbar.make( main_activity_id, message,
            Snackbar.LENGTH_LONG)
        snack.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permisionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        showSnackbarMessage(resources.getString(R.string.location_permission_explanation))
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(LOG_TAG, "onPermissionResult called ... with permission granted ? "+granted)
        if(granted)
            enableLocationComponent(map.style!!)
        else
            showSnackbarMessage(String.format("%s.\n%s",resources.getString(R.string.location_permission_not_granted)
                    ,resources.getString(R.string.location_permission_explanation)))
    }
}
