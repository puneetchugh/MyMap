package com.example.mymap.di_viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymap.viewmodels.MainActivityViewModel
import com.example.mymap.viewmodels.PinsListActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(PinsListActivityViewModel::class)
    protected abstract fun pinsListActivityViewModel(pinsListActivityViewModel: PinsListActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    protected abstract fun mainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel
}