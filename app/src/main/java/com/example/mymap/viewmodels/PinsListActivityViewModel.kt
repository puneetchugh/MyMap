package com.example.mymap.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mymap.constant.LOG_TAG
import com.example.mymap.model.ApiService
import com.example.mymap.model.data.model
import com.example.mymap.model.database.PinDao
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class PinsListActivityViewModel @Inject constructor(private val apiService: ApiService,
                                                private val pinDao: PinDao) : ViewModel(){

    private var pinsList: MutableLiveData<List<model.Pin>>
    var errorMessage : MutableLiveData<String> = MutableLiveData()
    init {
        pinsList = MutableLiveData()
        Log.d(LOG_TAG, "PinsListActivityViewModel init called")
    }

    fun getAllPinsLiveData(): LiveData<List<model.Pin>> {

        //val pinsLiveData = runBlocking { pinDao.getAllPinsFromDb() }
        lateinit var pinsLiveData : LiveData<List<model.Pin>>

        runBlocking {
            pinsLiveData = pinDao.getAllPinsFromDb()
        }
        return pinsLiveData
    }

    fun getAllPinsList() : List<model.Pin>{
        val pinsList = pinDao.getAllPinsListFromDb()

        if(pinsList.isEmpty()){
            apiService.getPinList().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(this::storeInDb, this::errorFound)
        }
        return pinsList
    }

    private fun storeInDb(pinList : List<model.Pin>){
        pinList.forEach{pin -> this@PinsListActivityViewModel.pinDao.insert(pin)
            Log.d(LOG_TAG, "location : "+pin.name)
        }
        errorMessage = MutableLiveData()
        pinsList.postValue(pinDao.getAllPinsFromDb().value)
    }

    fun errorFound(throwable: Throwable){
        errorMessage.postValue(throwable.message)
        Log.e(LOG_TAG, "error : throwable - "+throwable.message)
    }
}