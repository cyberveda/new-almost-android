package com.cyberveda.client.di.main

import com.cyberveda.client.api.main.OpenApiMainService
import com.cyberveda.client.persistence.AccountPropertiesDao
import com.cyberveda.client.persistence.AppDatabase
import com.cyberveda.client.persistence.BlogPostDao
import com.cyberveda.client.persistence.FeedbackPostDao
import com.cyberveda.client.repository.main.*
import com.cyberveda.client.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder
            .build()
            .create(OpenApiMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
        openApiMainService: OpenApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(openApiMainService, accountPropertiesDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    
    
    @MainScope
    @Provides
    fun provideBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(openApiMainService, blogPostDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(openApiMainService, blogPostDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideCreateFeedbackRepository(
        openApiMainService: OpenApiMainService,
        feedbackPostDao: FeedbackPostDao,
        sessionManager: SessionManager
    ): CreateFeedbackRepository {
        return CreateFeedbackRepository(openApiMainService, feedbackPostDao, sessionManager)
    }


    @MainScope
    @Provides
    fun provideFeedbackPostDao(db: AppDatabase): FeedbackPostDao {
        return db.getFeedbackPostDao()
    }



    @MainScope
    @Provides
    fun provideFeedbackRepository(
        openApiMainService: OpenApiMainService,
        feedbackPostDao: FeedbackPostDao,
        sessionManager: SessionManager
    ): FeedbackRepository {
        return FeedbackRepository(openApiMainService, feedbackPostDao, sessionManager)
    }
}

















