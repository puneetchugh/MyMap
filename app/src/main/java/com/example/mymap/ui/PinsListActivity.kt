package com.example.mymap.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymap.R
import com.example.mymap.constant.isNetworkConnected
import com.example.mymap.model.data.model
import com.example.mymap.ui.adapter.PinListAdapter
import com.example.mymap.viewmodels.PinsListActivityViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_pins_list.*
import kotlinx.android.synthetic.main.activity_pins_list.status_message
import javax.inject.Inject

class PinsListActivity : AppCompatActivity() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var pinsListActivityViewModel: PinsListActivityViewModel

    val pinsObserver = Observer<List<model.Pin>>{ list ->
        list?.let {
            showPinsList(it) }
    }

    val errorObserver = Observer<String>{ value ->
        if(value != null && !value.isBlank()){
            showSnackbarMessage(this@PinsListActivity.resources.getString(R.string.no_data))
            recycler_view.visibility = View.GONE
            status_message.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pins_list)
        AndroidInjection.inject(this)

        pinsListActivityViewModel = ViewModelProviders.of(this, viewModelFactory).get(PinsListActivityViewModel::class.java)

        title = resources.getString(R.string.pins_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pinsListActivityViewModel.getAllPinsList().takeIf{!it.isEmpty() }
            ?.let {showPinsList(it) }

        pinsListActivityViewModel.getAllPinsLiveData().observe(this, pinsObserver)
        pinsListActivityViewModel.errorMessage.observe(this, errorObserver)
    }

    private fun showPinsList(pinsList : List<model.Pin>){
        if(!isNetworkConnected(this))
            showSnackbarMessage(resources.getString(R.string.network_not_avail))

        if(!pinsList.isEmpty()){
            recycler_view.visibility = View.VISIBLE
            status_message.visibility = View.GONE
            recycler_view.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager
            recycler_view.adapter = PinListAdapter(pinsList.toMutableList(), this)
        }
    }

    private fun showSnackbarMessage(message : String){
        val snack = Snackbar.make( pins_list_activity_id, message,
            Snackbar.LENGTH_LONG)
        snack.show()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()

        if(!isNetworkConnected(this))
            showSnackbarMessage(resources.getString(R.string.network_not_avail))

        pinsListActivityViewModel.getAllPinsList().takeIf{!it.isEmpty() }
            ?.let {showPinsList(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        recycler_view.adapter = null
    }
}
