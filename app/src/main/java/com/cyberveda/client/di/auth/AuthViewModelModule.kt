package com.cyberveda.client.di.auth

import androidx.lifecycle.ViewModel
import com.cyberveda.client.di.ViewModelKey
import com.cyberveda.client.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}