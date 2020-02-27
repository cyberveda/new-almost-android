package com.cyberveda.client.persistence

import androidx.lifecycle.LiveData
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.ORDER_BY_ASC_USERNAME
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.ORDER_BY_DESC_DATE_UPDATED
import com.cyberveda.client.persistence.FeedbackQueryUtils.Companion.ORDER_BY_DESC_USERNAME

class FeedbackQueryUtils {


    companion object{
        private val TAG = "lgx_FeedbackQueryUtils"
        // values
        const val FEEDBACK_ORDER_ASC: String = ""
        const val FEEDBACK_ORDER_DESC: String = "-"
        const val FEEDBACK_FILTER_USERNAME = "username"
        const val FEEDBACK_FILTER_DATE_UPDATED = "date_updated"

        val ORDER_BY_ASC_DATE_UPDATED = FEEDBACK_ORDER_ASC + FEEDBACK_FILTER_DATE_UPDATED
        val ORDER_BY_DESC_DATE_UPDATED = FEEDBACK_ORDER_DESC + FEEDBACK_FILTER_DATE_UPDATED
        val ORDER_BY_ASC_USERNAME = FEEDBACK_ORDER_ASC + FEEDBACK_FILTER_USERNAME
        val ORDER_BY_DESC_USERNAME = FEEDBACK_ORDER_DESC + FEEDBACK_FILTER_USERNAME
    }
}


fun FeedbackPostDao.returnOrderedFeedbackQuery(
    query: String,
    filterAndOrder: String,
    page: Int
): LiveData<List<FeedbackPost>> {

    when{

        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) ->{
            return searchFeedbackPostsOrderByDateDESC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) ->{
            return searchFeedbackPostsOrderByDateASC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_DESC_USERNAME) ->{
            return searchFeedbackPostsOrderByAuthorDESC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_USERNAME) ->{
            return searchFeedbackPostsOrderByAuthorASC(
                query = query,
                page = page)
        }
        else ->
            return searchFeedbackPostsOrderByDateASC(
                query = query,
                page = page
            )
    }
}
