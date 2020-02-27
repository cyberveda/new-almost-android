package com.cyberveda.client.di.auth

import com.cyberveda.client.ui.auth.ForgotPasswordFragment
import com.cyberveda.client.ui.auth.LauncherFragment
import com.cyberveda.client.ui.auth.LoginFragment
import com.cyberveda.client.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment



}