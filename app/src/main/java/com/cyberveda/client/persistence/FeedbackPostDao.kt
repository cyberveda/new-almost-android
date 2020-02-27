package com.cyberveda.client.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.util.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface FeedbackPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feedbackPost: FeedbackPost): Long

    @Delete
    suspend fun deleteFeedbackPost(feedbackPost: FeedbackPost)

    @Query("""
        UPDATE feedback_post SET title = :title, body = :body, image = :image 
        WHERE pk = :pk
        """)

    fun updateFeedbackPost(pk: Int, title: String, body: String, image: String)

    @Query("""
        SELECT * FROM feedback_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        LIMIT (:page * :pageSize)
        """)
    fun getAllFeedbackPosts(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<FeedbackPost>>

    @Query("""
        SELECT * FROM feedback_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY date_updated DESC LIMIT (:page * :pageSize)
        """)
    fun searchFeedbackPostsOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<FeedbackPost>>

    @Query("""
        SELECT * FROM feedback_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY date_updated  ASC LIMIT (:page * :pageSize)""")
    fun searchFeedbackPostsOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<FeedbackPost>>

    @Query("""
        SELECT * FROM feedback_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY username DESC LIMIT (:page * :pageSize)""")
    fun searchFeedbackPostsOrderByAuthorDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<FeedbackPost>>

    @Query("""
        SELECT * FROM feedback_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY username  ASC LIMIT (:page * :pageSize)
        """)
    fun searchFeedbackPostsOrderByAuthorASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<FeedbackPost>>


}






