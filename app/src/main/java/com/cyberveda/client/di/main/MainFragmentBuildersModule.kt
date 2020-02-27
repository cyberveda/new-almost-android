package com.cyberveda.client.di.main

import com.cyberveda.client.ui.main.account.AccountFragment
import com.cyberveda.client.ui.main.account.ChangePasswordFragment
import com.cyberveda.client.ui.main.account.UpdateAccountFragment
import com.cyberveda.client.ui.main.blog.BlogFragment
import com.cyberveda.client.ui.main.blog.UpdateBlogFragment
import com.cyberveda.client.ui.main.blog.ViewBlogFragment
import com.cyberveda.client.ui.main.create_blog.CreateBlogFragment
import com.cyberveda.client.ui.main.create_feedback.CreateFeedbackFragment
import com.cyberveda.client.ui.main.feedback.FeedbackFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeBlogFragment(): BlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeFeedbackFragment(): FeedbackFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateBlogFragment(): CreateBlogFragment


    @ContributesAndroidInjector()
    abstract fun contributeCreateFeedbackFragment(): CreateFeedbackFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateBlogFragment(): UpdateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewBlogFragment(): ViewBlogFragment



    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}