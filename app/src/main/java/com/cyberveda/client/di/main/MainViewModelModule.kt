package com.cyberveda.client.di.main

import androidx.lifecycle.ViewModel
import com.cyberveda.client.di.ViewModelKey
import com.cyberveda.client.ui.main.account.AccountViewModel
import com.cyberveda.client.ui.main.blog.viewmodel.BlogViewModel
import com.cyberveda.client.ui.main.create_blog.CreateBlogViewModel
import com.cyberveda.client.ui.main.create_feedback.CreateFeedbackViewModel
import com.cyberveda.client.ui.main.feedback.viewmodel.FeedbackViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accoutViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(blogViewModel: BlogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedbackViewModel::class)
    abstract fun bindFeedbackViewModel(feedbackViewModel: FeedbackViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateBlogViewModel::class)
    abstract fun bindCreateBlogViewModel(createBlogViewModel: CreateBlogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateFeedbackViewModel::class)
    abstract fun bindCreateFeedbackViewModel(createFeedbackViewModel: CreateFeedbackViewModel): ViewModel
}








