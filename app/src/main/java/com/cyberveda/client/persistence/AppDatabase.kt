package com.cyberveda.client.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cyberveda.client.models.AccountProperties
import com.cyberveda.client.models.AuthToken
import com.cyberveda.client.models.BlogPost
import com.cyberveda.client.models.FeedbackPost

@Database(entities = [AuthToken::class, AccountProperties::class, BlogPost::class, FeedbackPost::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    abstract fun getFeedbackPostDao(): FeedbackPostDao


    companion object{
        val DATABASE_NAME: String = "app_db"
    }


}








