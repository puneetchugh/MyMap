package com.example.mymap.di.component

import android.app.Application
import com.example.mymap.di.ActivityBuilder
import com.example.mymap.di.AndroidApp
import com.example.mymap.di.module.DbModule
import com.example.mymap.model.NetworkModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, NetworkModule::class, DbModule::class, ActivityBuilder::class])

interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application : Application): Builder

        fun build(): AppComponent
    }

    fun inject(application : AndroidApp)
}