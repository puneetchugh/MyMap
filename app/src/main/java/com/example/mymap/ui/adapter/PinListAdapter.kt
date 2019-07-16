package com.example.mymap.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymap.R
import com.example.mymap.model.data.model
import kotlinx.android.synthetic.main.pin_layout.view.*

class PinListAdapter(val pinsList : MutableList<model.Pin>,
                     val context : Context) : RecyclerView.Adapter<PinListAdapter.ViewHolder>() {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val pinName = view.name_id
        val pinDesc = view.desc_id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.pin_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder?.pinName.text = pinsList[position].name
        holder?.pinDesc.text = pinsList[position].description
    }

    override fun getItemCount(): Int {
        return pinsList.size
    }
}