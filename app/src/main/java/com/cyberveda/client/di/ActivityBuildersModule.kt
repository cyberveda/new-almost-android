package com.cyberveda.client.di

import com.cyberveda.client.di.auth.AuthFragmentBuildersModule
import com.cyberveda.client.di.auth.AuthModule
import com.cyberveda.client.di.auth.AuthScope
import com.cyberveda.client.di.auth.AuthViewModelModule
import com.cyberveda.client.di.main.MainFragmentBuildersModule
import com.cyberveda.client.di.main.MainModule
import com.cyberveda.client.di.main.MainScope
import com.cyberveda.client.di.main.MainViewModelModule
import com.cyberveda.client.ui.auth.AuthActivity
import com.cyberveda.client.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity
}