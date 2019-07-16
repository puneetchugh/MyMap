package com.example.mymap.di

import com.example.mymap.ui.MainActivity
import com.example.mymap.ui.PinsListActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector()
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector()
    abstract fun bindPinsListActivity(): PinsListActivity

}