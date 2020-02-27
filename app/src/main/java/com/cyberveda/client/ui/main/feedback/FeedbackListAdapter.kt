package com.cyberveda.client.ui.main.feedback

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.cyberveda.client.R
import com.cyberveda.client.models.FeedbackPost
import com.cyberveda.client.util.DateUtils
import com.cyberveda.client.util.GenericViewHolder
import kotlinx.android.synthetic.main.layout_feedback_list_item.view.*

class FeedbackListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG = "lgx_FeedbackListAdapter"
    private val NO_MORE_RESULTS = -1
    private val FEEDBACK_ITEM = 0
    private val NO_MORE_RESULTS_FEEDBACK_MARKER = FeedbackPost(
        NO_MORE_RESULTS,
        "" ,
        "",
        "",
        "",
        0,
        ""
    )

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FeedbackPost>() {

        override fun areItemsTheSame(oldItem: FeedbackPost, newItem: FeedbackPost): Boolean {
            return oldItem.pk == newItem.pk
        }

        override fun areContentsTheSame(oldItem: FeedbackPost, newItem: FeedbackPost): Boolean {
            return oldItem == newItem
        }

    }
    private val differ =
        AsyncListDiffer(
            FeedbackRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when(viewType){

            NO_MORE_RESULTS ->{
                Log.e(TAG, "onCreateViewHolder: No more results...")
                return GenericViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_no_more_results,
                        parent,
                        false
                    )
                )
            }

            FEEDBACK_ITEM ->{
                return FeedbackViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.layout_feedback_list_item,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                return FeedbackViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_feedback_list_item,
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class FeedbackRecyclerChangeCallback(
        private val adapter: FeedbackListAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FeedbackViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(differ.currentList.get(position).pk > -1){
            return FEEDBACK_ITEM
        }
        return differ.currentList.get(position).pk
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    // Prepare the images that will be displayed in the RecyclerView.
    // This also ensures if the network connection is lost, they will be in the cache
    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<FeedbackPost>
    ){
        for(feedbackPost in list){
            requestManager
                .load(feedbackPost.image)
                .preload()
        }
    }

    fun submitList(
        feedbackList: List<FeedbackPost>?,
        isQueryExhausted: Boolean
    ){
        val newList = feedbackList?.toMutableList()
        if (isQueryExhausted)
            newList?.add(NO_MORE_RESULTS_FEEDBACK_MARKER)
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        differ.submitList(newList, commitCallback)
    }

    class FeedbackViewHolder
    constructor(
        itemView: View,
        val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: FeedbackPost) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            requestManager
                .load(item.image)
                .transition(withCrossFade())
                .into(itemView.feedback_image)
            itemView.feedback_title.text = item.title
            itemView.feedback_author.text = item.username
            itemView.feedback_update_date.text = DateUtils.convertLongToStringDate(item.date_updated)
        }
    }

    interface Interaction {

        fun onItemSelected(position: Int, item: FeedbackPost)

        fun restoreListPosition()
    }
}
