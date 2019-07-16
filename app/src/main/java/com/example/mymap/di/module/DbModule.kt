package com.example.mymap.di.module

import android.app.Application
import androidx.room.Room
import com.example.mymap.model.database.PinDao
import com.example.mymap.model.database.PinsDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DbModule{

    @Provides
    @Singleton
    internal fun providesDatabase(application: Application): PinsDb{
        return Room.databaseBuilder(
            application, PinsDb::class.java, "Pins.db")
            .allowMainThreadQueries().build()

    }

    @Provides
    @Singleton
    internal fun providePinDao(pinsDb : PinsDb):PinDao{
        return pinsDb.pinDao()
    }
}